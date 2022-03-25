package com.aylanetworks.aylasdk.connectivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.aylanetworks.aylasdk.AylaConnectivity;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.setup.AylaSetup;
import com.aylanetworks.aylasdk.util.NetworkUtils;
import com.aylanetworks.aylasdk.util.ObjectUtils;
import com.aylanetworks.aylasdk.util.SystemInfoUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 *  Ayla Connectivity Manager defines how the mobile phone is able to
 *  connect to a specified network across different Android platforms.
 *  Basically, it defines a set of uniform <code>connect</code> requests
 *  that may behave differently on different platforms, and how the
 *  <code>connect</code> result gets notified.
 */
public abstract class AylaConnectivityManager {

    public final static String TAG = "AylaConnectivityMgr";
    public final static String UNKNOWN_SSID = "<unknown ssid>";
    public final static int UNKNOWN_NET_ID = -1;
    public final static int DEFAULT_CONNECT_TIME_OUT_SECONDS = 30;

    private Handler _uiHandler;
    private Handler _connectTimeoutHandler;

    /**
     * Callback used to receive the result of the connect request.
     * Callers of the <var>connect</var> requests should register
     * this callback to be able to receive the connection result.
     * @see #connect(String, String, AylaSetup.WifiSecurityType, int)
     */
    public interface OnConnectResultCallback {

        /**
         * Called when the requested network with the specified SSID name is available.
         *
         * @param ssid the name of the Wi-Fi network the device is currently connected
         *             to. It should be the same as the requested network name specified
         *             in the <code>connect</code> requests.
         */
        void onAvailable(@NonNull String ssid);

        /**
         * Called when the requested network was not available due to an error.
         *
         * @param error the error that caused the network unavailability.
         *              Possible errors are:
         * <ol>
         *     <li>{@link com.aylanetworks.aylasdk.error.TimeoutError TimeoutError}
         *         for the connect request eventually timed out.</li>
         *     <li>{@link com.aylanetworks.aylasdk.error.AppPermissionError AppPermissionError}
         *         for lacking of necessary network permission(s).</li>
         * </ol>
         */
        void onUnavailable(@Nullable AylaError error);
    }

    private final Context _ctx;
    private final WifiManager _wm;
    private final AylaConnectivity _ac;
    private final ConnectivityManager _cm;
    private final Set<OnConnectResultCallback> _callbacks = new HashSet<>();

    protected AylaConnectivityManager(@NonNull Context context) {
        _ctx = context.getApplicationContext();
        _wm = (WifiManager) context.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        _cm = (ConnectivityManager) context.getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        _ac = new AylaConnectivity(context);
    }

    /**
     * Returns the appropriate implementation of the Ayla Connectivity Manager based
     * on the passed-in context and the Android SDK version the mobile phone is running on.
     *
     * @param context the context required to initialize the connectivity manager.
     * @param interactive set true to create a connectivity manager that allows
     *                    interactively select, from the system WiFi settings screen,
     *                    the target network to connect to.
     */
    public static AylaConnectivityManager from(@NonNull Context context, boolean interactive) {
        if (interactive) {
            return new AylaConnectivityManagerInteractiveImp(context);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                SystemInfoUtils.getTargetSdkVersion(context) >= Build.VERSION_CODES.Q) {
            return new AylaConnectivityManagerAndroid10Imp(context);
        } else {
            return new AylaConnectivityManagerPreAndroid10Imp(context);
        }
    }

    /**
     * Connects the mobile phone to the network with specified SSID. Should call
     * {@link #registerConnectResultCallback(OnConnectResultCallback)} to be able
     * to receive <var>connect</var> result.
     *
     * @param ssid     the name of the network to which the phone is to connect.
     * @param password password of the network, could be null for open network.
     */
    public void connect(@NonNull String ssid, @Nullable String password) {
        connect(ssid, password, AylaSetup.WifiSecurityType.NONE,
                DEFAULT_CONNECT_TIME_OUT_SECONDS);
    }

    /**
     * Connects the mobile phone to the network with specified SSID. Should call
     * {@link #registerConnectResultCallback(OnConnectResultCallback)} to be able
     * to receive <var>connect</var> result.
     *
     * @param ssid     the name of the network to which the phone is to connect.
     * @param password password of the network, could be null for open network.
     * @param timeoutInSeconds the time within which the connect should be done.
     */
    public void connect(@NonNull String ssid, @Nullable String password, int timeoutInSeconds) {
        connect(ssid, password, AylaSetup.WifiSecurityType.NONE, timeoutInSeconds);
    }

    /**
     * Connects the mobile phone to the network with specified SSID. Should call
     * {@link #registerConnectResultCallback(OnConnectResultCallback)} to be able
     * to receive <var>connect</var> result.
     *
     * @param ssid     the name of the network to which the phone is to connect.
     * @param password password of the network, could be null for open network.
     * @param securityType security type of the target network.
     * @param timeoutInSeconds the time within which the connect should be done.
     */
    public abstract void connect(@NonNull String ssid, @Nullable String password,
            @NonNull AylaSetup.WifiSecurityType securityType, int timeoutInSeconds);

    /**
     * Disconnects the mobile phone from the currently connected network，and release
     * any resources used in the corresponding <code>connect</code> request.
     */
    public abstract void disconnect();

    /**
     * Registers a new callback to be notified when the connect result is available.
     * @see #connect(String, String, AylaSetup.WifiSecurityType, int)
     */
    public void registerConnectResultCallback(@NonNull OnConnectResultCallback callback) {
        _callbacks.add(callback);
    }

    /**
     * Unregisters the specified connect result callback.
     */
    public void unregisterConnectResultCallback(@NonNull OnConnectResultCallback callback) {
        _callbacks.remove(callback);
    }

    /**
     * Binds current process to the specified network so that connectivity can be
     * available on Android M+ devices.
     *
     * @param network the network to bind to, pass null to unbind any bound network.
     */
    public boolean bindProcessToNetwork(@Nullable Network network) {
        // https://android-developers.googleblog.com/2016/07/
        // connecting-your-app-to-wi-fi-device.html
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ConnectivityManager cm = getConnectivityManager();
            return cm.bindProcessToNetwork(network);
        } else {
            return true;
        }
    }

    /**
     * Returns the Wifi manager associated with this manager.
     */
    public final @NonNull WifiManager getWifiManager() {
        return _wm;
    }

    /**
     * Returns the system-wide connectivity manager associated with this manager.
     */
    public final @NonNull ConnectivityManager getConnectivityManager() {
        return _cm;
    }

    /**
     * Returns the application context initialized in the constructor.
     */
    public @NonNull Context getContext() {
        return _ctx;
    }

    /**
     * Returns the Ayla Connectivity instance.
     */
    public @NonNull AylaConnectivity getConnectivity() {
        return _ac;
    }

    /**
     * Returns the name of the Wi-Fi network the device is currently connected to, or
     * returns {@link #UNKNOWN_SSID} if otherwise not available.
     */
    public @NonNull String getCurrentSSID() {
        WifiInfo currentNetworkInfo = getWifiManager().getConnectionInfo();
        String currentSSID = ObjectUtils.unquote(currentNetworkInfo.getSSID());
        return (currentSSID != null) ? currentSSID : UNKNOWN_SSID;
    }

    /**
     * Returns the IP address of the mobile device, or returns null if no valid
     * IP address is configured.
     */
    public @Nullable String getIpAddress() {
        DhcpInfo dhcpInfo = getWifiManager().getDhcpInfo();
        int address = dhcpInfo.ipAddress;
        return address == 0 ? null : NetworkUtils.getIpAddress(address);
    }

    /**
     * Returns the gateway address of the mobile device, or returns null if no valid
     * IP address is configured.
     */
    public @Nullable String getGatewayIpAddress() {
        DhcpInfo dhcpInfo = getWifiManager().getDhcpInfo();
        int address = dhcpInfo.gateway;
        return address == 0 ? null : NetworkUtils.getIpAddress(address);
    }

    /**
     * Binds the socket to the specified network so that data traffic on the socket
     * will be sent on this {@code Network}.
     *
     * @param network the network to be bound, pass null to unbind any bound network.
     * @param socket the socket to be bound, MUST not be connected.
     */
    protected void bindSocketToNetwork(@NonNull Network network, @NonNull Socket socket) {
        try {
            network.bindSocket(socket);
        } catch (IOException e) {
            AylaLog.e(TAG, "bind socket error: " + e.getMessage());
        }
    }

    /**
     * Check to see if the mobile phone has connected to the specified SSID.
     *
     * @return true if connected, otherwise returns false.
     */
    protected boolean checkHasConnectedTo(@NonNull String ssid) {
        return TextUtils.equals(ssid, getCurrentSSID());
    }

    /**
     * Launch the system Wi-fi settings screen.
     */
    public void launchWifiSettingsScreen() {
        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(intent);
    }

    protected  @NonNull Handler getUiHandler() {
        if (_uiHandler == null) {
            _uiHandler = new Handler(Looper.getMainLooper());
        }
        return _uiHandler;
    }

    protected  @NonNull Handler getConnectTimeoutHandler() {
        if (_connectTimeoutHandler == null) {
            _connectTimeoutHandler = new Handler(Looper.getMainLooper());
        }
        return _connectTimeoutHandler;
    }

    protected Set<OnConnectResultCallback> getConnectivityChangedCallbacks() {
        return new HashSet<>(_callbacks);
    }

    protected void notifyConnectivityAvailable(@NonNull String ssid) {
        getUiHandler().post(new Runnable() {
            @Override
            public void run() {
                for (OnConnectResultCallback callback : getConnectivityChangedCallbacks()) {
                    callback.onAvailable(ssid);
                }
            }
        });
    }

    protected void notifyConnectivityUnavailable(@NonNull AylaError error) {
        getUiHandler().post(new Runnable() {
            @Override
            public void run() {
                for (OnConnectResultCallback callback : getConnectivityChangedCallbacks()) {
                    callback.onUnavailable(error);
                }
            }
        });
    }
}
