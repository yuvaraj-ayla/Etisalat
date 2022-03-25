package com.aylanetworks.aylasdk.connectivity;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aylanetworks.aylasdk.AylaConnectivity;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.TimeoutError;
import com.aylanetworks.aylasdk.setup.AylaSetup;

import java.util.concurrent.TimeUnit;

/**
 * Ayla Connectivity Manager implementation that relies on the user
 * to manually choose the target network. Once received a request to
 * connect to a new network, it tries to launch the system Wi-Fi settings
 * screen and let the user to choose the target network to connect to.
 */
public class AylaConnectivityManagerInteractiveImp extends AylaConnectivityManager {

    public static final String TAG = "ConnMgrImpInteractive";

    public AylaConnectivityManagerInteractiveImp(@NonNull Context context) {
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

        Handler timeoutHandler = getConnectTimeoutHandler();
        AylaConnectivity aylaConnectivity = getConnectivity();
        AylaConnectivity.AylaConnectivityListener listener;
        listener = new AylaConnectivity.AylaConnectivityListener() {
            @Override
            public void connectivityChanged(boolean wifiEnabled, boolean cellularEnabled) {
                String currentSSID = getCurrentSSID();
                if (wifiEnabled && TextUtils.equals(ssid, currentSSID)) {
                    aylaConnectivity.stopMonitoring();
                    timeoutHandler.removeCallbacksAndMessages(TAG);
                    aylaConnectivity.unregisterListener(this);
                    notifyConnectivityAvailable(ssid);
                } else {
                    AylaLog.d(TAG, "connectivityChanged, wifiEnabled:" + wifiEnabled
                            + ", cellularEnabled:" + cellularEnabled
                            + ", current ssid:" + getCurrentSSID());
                }
            }
        };
        aylaConnectivity.registerListener(listener);
        aylaConnectivity.startMonitoring(getContext());

        long timeoutMillis = SystemClock.uptimeMillis() +
                TimeUnit.MILLISECONDS.convert(timeoutInSeconds, TimeUnit.SECONDS);
        timeoutHandler.postAtTime(() -> {
            aylaConnectivity.stopMonitoring();
            timeoutHandler.removeCallbacksAndMessages(TAG);
            aylaConnectivity.unregisterListener(listener);

            String currentSSID = getCurrentSSID();
            if (TextUtils.equals(ssid, currentSSID)) {
                AylaLog.d(TAG, "timed out and connected to: " + ssid);
                notifyConnectivityAvailable(ssid);
            } else {
                String msg = "timed out and connected to unexpected " + currentSSID;
                AylaError error = new TimeoutError(msg);
                notifyConnectivityUnavailable(error);
            }
        }, TAG, timeoutMillis);

        launchWifiSettingsScreen();
    }

    @Override
    public void disconnect() {
        getConnectTimeoutHandler().removeCallbacksAndMessages(TAG);
    }
}
