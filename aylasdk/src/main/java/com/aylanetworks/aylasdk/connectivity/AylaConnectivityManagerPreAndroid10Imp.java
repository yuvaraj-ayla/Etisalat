package com.aylanetworks.aylasdk.connectivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.aylanetworks.aylasdk.AylaConnectivity;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.NetworkError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.error.TimeoutError;
import com.aylanetworks.aylasdk.setup.AylaSetup;
import com.aylanetworks.aylasdk.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ayla Connectivity Manager implementation that is designed to connect the
 * mobile phone to a specified network on platforms pre Android 10.
 * It relies on {@link android.net.wifi.WifiConfiguration} to config and
 * connect to the target network.
 */
@SuppressWarnings("deprecation")
@SuppressLint("DefaultLocale")
public class AylaConnectivityManagerPreAndroid10Imp extends AylaConnectivityManager {

    public static final String TAG = "ConnMgrImpPreAndroid10";

    private int _addedNetworkId = UNKNOWN_NET_ID;

    public AylaConnectivityManagerPreAndroid10Imp(@NonNull Context context) {
        super(context);
    }

    @Override
    public void connect(@NonNull  String ssid,
                        @Nullable String password,
                        @NonNull  AylaSetup.WifiSecurityType securityType,
                        int timeoutInSeconds) {
        if (checkHasConnectedTo(ssid)) {
            AylaLog.d(TAG, String.format("already connected to %s", ssid));
            notifyConnectivityAvailable(ssid);
            return;
        }

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            String msg = String.format("missing permission %s", Manifest.permission.ACCESS_FINE_LOCATION);
            AylaError error = new PreconditionError(msg, null);
            AylaLog.e(TAG, error.getMessage());
            notifyConnectivityUnavailable(error);
            return;
        }

        WifiConfiguration config = null;
        List<WifiConfiguration> configs = getWifiManager().getConfiguredNetworks();
        for (WifiConfiguration c : configs) {
            if (TextUtils.equals(ssid, ObjectUtils.unquote(c.SSID))) {
                config = c;
                break;
            }
        }

        if (config == null) {
            config = new WifiConfiguration();
            config.SSID = ObjectUtils.quote(ssid);
            _addedNetworkId = UNKNOWN_NET_ID;
            AylaLog.i(TAG, String.format("add new config: %s", config));
        } else {
            _addedNetworkId = config.networkId;
            AylaLog.i(TAG, String.format("reuse existing config: %s", config));
        }

        switch (securityType){
            case WEP:
                if (!TextUtils.isEmpty(password)) {
                    if (isHexWepKey(password)) {
                        config.wepKeys[0] = password;
                    } else {
                        config.wepKeys[0] = ObjectUtils.quote(password);
                    }
                }
                config.wepTxKeyIndex = 0;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                break;

            case WPA:
            case WPA2:
            case WPA3:
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                if (    (securityType == AylaSetup.WifiSecurityType.WPA2) ||
                        (securityType == AylaSetup.WifiSecurityType.WPA3) ) {
                    config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                } else {
                    config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                };
                config.preSharedKey = ObjectUtils.quote(password);
                break;

            case NONE:
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;
        }

        connect(config, timeoutInSeconds);
    }

    @Override
    public void disconnect() {
        getConnectTimeoutHandler().removeCallbacksAndMessages(TAG);
        getWifiManager().disconnect();
        bindProcessToNetwork(null);
    }

    private void connect(@NonNull WifiConfiguration config, int timeoutInSeconds) {
        String targetSSID = ObjectUtils.unquote(config.SSID);
        Handler timeoutHandler = getConnectTimeoutHandler();
        long targetTimeoutMillis = SystemClock.uptimeMillis() + TimeUnit.MILLISECONDS.convert(
                timeoutInSeconds, TimeUnit.SECONDS);

        AtomicInteger networkChangeToleranceLimit = new AtomicInteger(1);

        AylaConnectivity aylaConnectivity = getConnectivity();
        AylaConnectivity.AylaConnectivityListener connectivityListener =
                new AylaConnectivity.AylaConnectivityListener() {
            @Override
            public void connectivityChanged(boolean wifiEnabled, boolean cellularEnabled) {
                AylaLog.d(TAG, String.format("connectivity changed, wifiEnabled: %b, " +
                        "cellularEnabled: %b", wifiEnabled, cellularEnabled));
                if (wifiEnabled) {
                    if (TextUtils.equals(targetSSID, getCurrentSSID())) {
                        AylaLog.d(TAG, String.format("has connected to: %s", targetSSID));
                        aylaConnectivity.stopMonitoring();
                        aylaConnectivity.unregisterListener(this);
                        timeoutHandler.removeCallbacksAndMessages(TAG);
                        Network activeNetwork = getActiveWifiNetwork();
                        boolean bound = bindProcessToNetwork(activeNetwork);
                        AylaLog.d(TAG, String.format("bound to %s %b", activeNetwork, bound));
                        notifyConnectivityAvailable(targetSSID);
                    } else {
                        String msg = String.format("connected to unexpected %s", getCurrentSSID());
                        if (networkChangeToleranceLimit.decrementAndGet() < 0) {
                            // On some phone models, such as Nexus 5 running Android 6.0.1,
                            // it may first report wifi available event with current network ssid,
                            // then report wifi available event with the target network ssid.
                            // Three consecutive network changed events can be observed during
                            // the network transition process. For example, suppose the device
                            // is now on QA-TPLINK and will connect to Ayla-DevKit,
                            // 1. connectivity changed, wifiEnabled: true, cellularEnabled: false  (QA-TPLINK available)
                            // 2. connectivity changed, wifiEnabled: false, cellularEnabled: false (QA-TPLINK lost)
                            // 3. connectivity changed, wifiEnabled: true, cellularEnabled: false  (Ayla-DevKit available)
                            AylaError error = new NetworkError(msg, null);
                            AylaLog.e(TAG, error.getMessage());
                            notifyConnectivityUnavailable(error);
                        }
                    }
                }
            }
        };
        aylaConnectivity.registerListener(connectivityListener);
        aylaConnectivity.startMonitoring(getContext());

        timeoutHandler.postAtTime(() -> {
            aylaConnectivity.stopMonitoring();
            timeoutHandler.removeCallbacksAndMessages(TAG);
            aylaConnectivity.unregisterListener(connectivityListener);

            if (TextUtils.equals(targetSSID, getCurrentSSID())) {
                AylaLog.d(TAG, String.format("timed out and connected to: %s", targetSSID));
                notifyConnectivityAvailable(targetSSID);
            } else {
                String msg = String.format("timed out but connected to %s", getCurrentSSID());
                AylaError error = new TimeoutError(msg);
                notifyConnectivityUnavailable(error);
            }
        }, TAG, targetTimeoutMillis);


        AylaError error = null;

        try {
            if (_addedNetworkId == UNKNOWN_NET_ID) {
                _addedNetworkId = getWifiManager().addNetwork(config);
                AylaLog.i(TAG, String.format("added new network with id: %d", _addedNetworkId));
            }

            if (_addedNetworkId != UNKNOWN_NET_ID) {
                boolean enabled = getWifiManager().enableNetwork(_addedNetworkId, true);
                AylaLog.d(TAG, String.format("enabled network %d: %b", _addedNetworkId, enabled));
                if (!enabled) {
                    error = new PreconditionError("enable network failed");
                }
            } else {
                error = new PreconditionError("add network failed");
            }
        } catch (SecurityException e) {
            error = new PreconditionError("missing permission:" + e.getMessage());
        } finally {
            if (error != null) {
                AylaLog.e(TAG, error.getMessage());
                timeoutHandler.removeCallbacksAndMessages(TAG);
                aylaConnectivity.unregisterListener(connectivityListener);
                notifyConnectivityUnavailable(error);
            } else {
                AylaLog.i(TAG, "waiting for connect result");
            }
        }
    }

    private @Nullable Network getActiveWifiNetwork() {
        ConnectivityManager cm = getConnectivityManager();
        Network[] networks = cm.getAllNetworks();
        for (Network network : networks) {
            NetworkInfo info = cm.getNetworkInfo(network);
            if (info != null
                    && info.isConnected()
                    && info.getType() == ConnectivityManager.TYPE_WIFI) {
                return network;
            }
        }

        return null;
    }

    private Boolean isHexWepKey(String wepKey) {
        final int len = wepKey.length();

        // WEP-40, WEP-104, and some vendors using 256-bit WEP (WEP-232?)
        if (len != 10 && len != 26 && len != 58) {
            return false;
        }
        return isHex(wepKey);
    }

    private Boolean isHex(String key) {
        for (int i = key.length() - 1; i >= 0; i--) {
            final char c = key.charAt(i);
            if (!(c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f')) {
                return false;
            }
        }
        return true;
    }

}
