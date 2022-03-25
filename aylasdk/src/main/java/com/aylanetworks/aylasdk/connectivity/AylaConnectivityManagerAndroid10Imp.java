package com.aylanetworks.aylasdk.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.PatternMatcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.aylanetworks.aylasdk.AylaConnectivity;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.NetworkError;
import com.aylanetworks.aylasdk.setup.AylaSetup;

import java.util.concurrent.TimeUnit;

/**
 * Ayla Connectivity Manager implementation that is designed to connect the
 * mobile phone to a specified network on Android 10+. It relies on
 * {@link ConnectivityManager#requestNetwork(NetworkRequest,
 * ConnectivityManager.NetworkCallback)} to connect to the target network.
 */
@RequiresApi(api = Build.VERSION_CODES.Q)
public class AylaConnectivityManagerAndroid10Imp extends AylaConnectivityManager {

    public static final String TAG = "ConnMgrImpOnAndroid10";

    private ConnectivityManager.NetworkCallback _networkCallback;

    public AylaConnectivityManagerAndroid10Imp(@NonNull Context context) {
        super(context);
    }

    @Override
    public void connect(@NonNull String ssid,
                        @Nullable String password,
                        @NonNull AylaSetup.WifiSecurityType securityType,
                        int timeoutInSeconds) {
        if (checkHasConnectedTo(ssid)) {
            AylaLog.d(TAG, "already connected to " + ssid);
            notifyConnectivityAvailable(ssid);
            return;
        }

        AylaLog.i(TAG, "connecting to " + ssid);

        if (getNetworkCallback() != null) {
            // Disconnect from currently connected network and wait until the system
            // auto-reconnect to a new network, which might or might not be the same
            // network we are connecting to, if it is then we are done, otherwise,
            // send a new request for the target network. This way, the network popup
            // dialog only comes up at most once, which would otherwise may appear
            // multiple times and end up with a "Something came up" failure dialog.
            getConnectivity().registerListener(new AylaConnectivity.AylaConnectivityListener() {
                @Override
                public void connectivityChanged(boolean wifiEnabled, boolean cellularEnabled) {
                    AylaLog.d(TAG, "connectivityChanged, wifiEnabled:" + wifiEnabled
                            + ", cellularEnabled: " + cellularEnabled);
                    if (wifiEnabled) {
                        getConnectivity().unregisterListener(this);
                        getConnectivity().stopMonitoring();
                        if (checkHasConnectedTo(ssid)) {
                            AylaLog.d(TAG, "system auto reconnected to " + ssid);
                            notifyConnectivityAvailable(ssid);
                        } else {
                            AylaLog.d(TAG, "current ssid is " + getCurrentSSID());
                            connect(ssid, password, new DefaultNetworkCallback(), timeoutInSeconds);
                        }
                    }
                }
            });
            getConnectivity().startMonitoring(getContext());
            disconnect();
        } else {
            connect(ssid, password, new DefaultNetworkCallback(), timeoutInSeconds);
        }
    }

    private ConnectivityManager.NetworkCallback getNetworkCallback() {
        return _networkCallback;
    }

    protected void connect(@NonNull String ssid,
                        @Nullable String password,
                        @NonNull ConnectivityManager.NetworkCallback callback,
                        int timeoutInSeconds) {
        WifiNetworkSpecifier.Builder wifiSpecifierBuilder = new WifiNetworkSpecifier.Builder();
        PatternMatcher ssidPattern = new PatternMatcher(ssid, PatternMatcher.PATTERN_PREFIX);
        wifiSpecifierBuilder.setSsidPattern(ssidPattern);
        if (password != null) {
            wifiSpecifierBuilder.setWpa2Passphrase(password);   // need to test with WPA3 Personal - should work
            // wifiSpecifierBuilder.setWpa3Passphrase(password);
        }

        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .setNetworkSpecifier(wifiSpecifierBuilder.build())
                .build();

        long timeoutMs = TimeUnit.MILLISECONDS.convert(timeoutInSeconds, TimeUnit.SECONDS);
        getConnectivityManager().requestNetwork(request, callback, (int) timeoutMs);
        _networkCallback = callback;
    }

    @Override
    public void disconnect() {
        if (getNetworkCallback() != null) {
            AylaLog.i(TAG, "disconnecting from current network: " + getCurrentSSID());
            getConnectivityManager().unregisterNetworkCallback(getNetworkCallback());
            getConnectivityManager().bindProcessToNetwork(null);
            _networkCallback = null;
        } else {
            AylaLog.i(TAG, "no network callback to disconnect from");
        }
    }

    private final class DefaultNetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            String ssid = getCurrentSSID();
            AylaLog.i(TAG, "onAvailable:" + network + " with ssid " + ssid);
            boolean bound = bindProcessToNetwork(network);
            AylaLog.d(TAG, "bound to network: " + bound);
            notifyConnectivityAvailable(ssid);
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            AylaLog.i(TAG, "onLosing:" + network + ", maxMsToLive:" + maxMsToLive);
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            AylaLog.i(TAG, "onLost:" + network);
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            Network activeNetwork = getConnectivityManager().getActiveNetwork();
            AylaLog.i(TAG, "onUnavailable， active network:" + activeNetwork);
            AylaError error = new NetworkError("network unavailable", null);
            AylaLog.d(TAG, error.getMessage());
            notifyConnectivityUnavailable(error);
            disconnect();
        }

        @Override
        public void onCapabilitiesChanged(
                @NonNull Network network,
                @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            AylaLog.i(TAG, "onCapabilitiesChanged, network:" + network
                    + ", cap:" + networkCapabilities);
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network,
                                            @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            AylaLog.i(TAG, "onLinkPropertiesChanged, network:" + network
                    + ", linkProp:" + linkProperties);
        }
    }
}
