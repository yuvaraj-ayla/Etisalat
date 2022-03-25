package com.aylanetworks.aylasdk.setup;
/*
 * Android_AylaSDK
 *
 * Copyright 2019 Ayla Networks, all rights reserved
 */

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.AylaAPIRequest.EmptyResponse;
import com.aylanetworks.aylasdk.AylaConnectivity;
import com.aylanetworks.aylasdk.AylaDSManager;
import com.aylanetworks.aylasdk.AylaDevice;
import com.aylanetworks.aylasdk.AylaDeviceManager;
import com.aylanetworks.aylasdk.AylaJsonRequest;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.AylaLogService;
import com.aylanetworks.aylasdk.AylaNetworks;
import com.aylanetworks.aylasdk.AylaSessionManager;
import com.aylanetworks.aylasdk.connectivity.AylaConnectivityManager;
import com.aylanetworks.aylasdk.error.AppPermissionError;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.InternalError;
import com.aylanetworks.aylasdk.error.InvalidArgumentError;
import com.aylanetworks.aylasdk.error.NetworkError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.error.ServerError;
import com.aylanetworks.aylasdk.error.TimeoutError;
import com.aylanetworks.aylasdk.lan.AylaHttpServer;
import com.aylanetworks.aylasdk.lan.AylaLanCommand;
import com.aylanetworks.aylasdk.lan.AylaLanConfig;
import com.aylanetworks.aylasdk.lan.AylaLanMessage.Payload;
import com.aylanetworks.aylasdk.lan.AylaLanModule;
import com.aylanetworks.aylasdk.lan.AylaLanRequest;
import com.aylanetworks.aylasdk.lan.LanCommand;
import com.aylanetworks.aylasdk.lan.StartScanCommand;
import com.aylanetworks.aylasdk.metrics.AylaMetric;
import com.aylanetworks.aylasdk.metrics.AylaMetricsManager;
import com.aylanetworks.aylasdk.metrics.AylaSetupMetric;
import com.aylanetworks.aylasdk.metrics.AylaUserDataGrant;
import com.aylanetworks.aylasdk.util.AylaPredicate;
import com.aylanetworks.aylasdk.util.EmptyListener;
import com.aylanetworks.aylasdk.util.ObjectUtils;
import com.aylanetworks.aylasdk.util.PermissionUtils;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.aylanetworks.aylasdk.util.URLHelper;
import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * The AylaSetup class is used to scan for devices that are in AP mode, or in other words have
 * not yet been configured to join the local WiFi network. Devices that are not able to join a
 * WiFi network will put themselves into AP mode and broadcast an SSID that is identifiable as an
 * Ayla device.
 * <p>
 * Once a device's AP has been discovered via a call to {@link #scanForAccessPoints}, the result
 * can be passed to {@link #connectToNewDevice} to connect the mobile device to the
 * device's AP. Once the mobile device and setup device are connected this way, the mobile device
 * asks the setup device to do its own scan for access points via
 * {@link #scanForAccessPoints}, which returns another set of access points.
 * <p>
 * At this point the user may choose an access point the device is to connect to and
 * supply the app with the WiFi password. Once this is done, a call to
 * {@link #connectDeviceToService} will provide the setup device with the AP SSID and password.
 * <p>
 * At that point, the device will attempt to connect to the specified AP using the supplied
 * password. This could possibly drop our connection with the setup device's AP, where we will
 * join the network the device was connecting to.
 * <p>
 * In order to confirm that the device has successfully connected to the WiFi network, and
 * subsequently the Ayla service, the {@link #confirmDeviceConnected} method can be called to
 * verify that the device has connected.
 * <p>
 * Once the device has been connected to the network, it may be registered to the user's account
 * if it was not already registered (e.g. WiFi password or SSID changed, etc.).
 */
public class AylaSetup {

    public final static String LOG_TAG = "AylaSetup";

    /**
     * LAN error due to LAN module is null.
     */
    public static final String LAN_PRECONDITION_ERROR = "LAN module is null";

    /**
     * Security type of a access point reported by the
     * device in an AP scan result.
     */
    public enum WifiSecurityType {
        NONE("None"),
        WEP ("WEP"),
        WPA ("WPA"),
        WPA2("WPA2-Personal"),
        WPA3("WPA3-Personal");

        WifiSecurityType(String value) {
            _stringValue = value;
        }
        public String stringValue() {
            return _stringValue;
        }
        private String _stringValue;
    }

    /**
     * Wi-Fi connection state as a result of connecting the device
     * to the network via a call to {@link #connectDeviceToService}.
     */
    public static class DeviceWifiState {
        @StringDef({UNKNOWN, DISABLED, DOWN, WIFI_CONNECTING,
                NETWORK_CONNECTING, CLOUD_CONNECTING, UP})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedState {}

        public static final String UNKNOWN            = "unknown";
        public static final String DISABLED           = "disabled";
        public static final String DOWN               = "down";
        public static final String WIFI_CONNECTING    = "wifi_connecting";
        public static final String NETWORK_CONNECTING = "network_connecting";
        public static final String CLOUD_CONNECTING   = "cloud_connecting";
        public static final String UP                 = "up";
    }

    /**
     * Class holding registration info returned from a
     * {@link #fetchRegInfo(Listener, ErrorListener)} call
     */
    public static class RegistrationInfo {
        @Expose public String regtoken;
        @Expose public int registered;
        @Expose public String registrationType;
        @Expose public String host_symname;
    }

    /**
     * DeviceWifiStateChangeListener is used to provide updates for the state of a device
     * during setup process. If no state is returned from the module, a string value "unknown"is
     * returned. State values returned from are defined in {@link AylaSetup.DeviceWifiState}.
     */
    public interface DeviceWifiStateChangeListener{
        /**
         * Called when there is a change in the device's wifi setup state.
         * @param currentState the last fetched wifi setup state, could be
         *                     one of the states defined in {@link DeviceWifiState}
         */
        void wifiStateChanged(@DeviceWifiState.AllowedState String currentState);
    }

    /**
     * Array of permissions required by setup. Methods will check
     * these to make sure they have been permitted before proceeding.
     */
    public static final String[] SETUP_REQUIRED_PERMISSIONS = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    /**
     * Default IP address of the setup device. Could be different on different
     * platforms, such as on Qualcomm it defaults to 10.231.227.81.
     */
    private final static String SETUP_DEVICE_IP = "192.168.0.1";

    /**
     * Time interval to check if the device has connected to the cloud.
     */
    private final static int DEFAULT_CONFIRM_POLL_INTERVAL = 1000;

    /**
     * Timeout value for requests to poll wifi status.
     */
    private final static int DEFAULT_POLL_WIFI_STATUS_TIMEOUT = 15000;

    /**
     * Delay time, in seconds, to fetch the discovered access points as a
     * result of a call to {@link #startDeviceScanForAccessPoints},
     * as the device might not have completed preparing the results.
     */
    private final static int DEFAULT_DELAY_FETCH_ACCESS_POINTS = 3000;

    /**
     * Default timeout value, in seconds, spent on fetching the access
     * points discovered on the device.
     */
    private final static int DEFAULT_TIMEOUT_FETCH_ACCESS_POINTS = 10;

    /**
     * The original Wi-Fi network info before the setup starts.
     * It's saved as so to be able to re-join on exit setup.
     */
    private WifiInfo _currentNetworkInfo;

    /**
     * Secure setup delivers LAN payload in encrypted LAN commands.
     */
    private boolean _isSecureSetup = false;

    /**
     * Flag that indicates if registration info has been fetched from
     * the device in a LAN request. For devices that claims to support
     * {@link AylaSetupDevice#FEATURE_REG_TOKEN} feature, the registration
     * info can be fetched from a LAN request, which means the device
     * can be locally confirmed connected thus without having to confirm
     * its online status from the cloud.
     */
    private boolean _fetchedRegInfo = false;

    private final String _setupSessionId;
    private final Context _context;
    private final AylaLogService _logService;
    private final AylaSessionManager _sessionManager;
    private final Handler _uiHandler;
    private final Set<DeviceWifiStateChangeListener> _wifiStateChangeListeners;
    private final AylaConnectivityManager _connectivityManager;
    private final ScanResultsReceiver _scanResultsReceiver;

    private AylaHttpServer _lanHttpServer;
    private AylaSetupDevice _setupDevice;
    private AylaWifiStatus _lastWifiStatus;
    private String _setupDeviceIp = SETUP_DEVICE_IP;
    private String _targetDeviceSSIDName;
    private String _targetDeviceSSIDPassword;
    private int _confirmPollInterval = DEFAULT_CONFIRM_POLL_INTERVAL;

    private long _setupStartTime;
    private long _setupFinishTime;

    /**
     * Constructor for an AylaSetup instance.
     *
     * @param context  Context used for registering receivers, etc.
     * @param sessionManager Session manager used for session management.
     * @param connectivityManager connectivity manager used for network connection.
     *
     * @throws AppPermissionError If the required permissions have not been granted
     * to perform WiFi setup operations.
     * @throws InvalidArgumentError If invalid passed-in parameters were detected.
     * @throws InternalError Should any internal error was found, such as the internal HTTP
     * server could not be created.
     */
    public AylaSetup(@NonNull Context context,
                     @NonNull AylaSessionManager sessionManager,
                     @NonNull AylaConnectivityManager connectivityManager) throws AylaError {
        try {
            Preconditions.checkNotNull(context, "context is null");
            Preconditions.checkNotNull(sessionManager, "session manager is null");
            Preconditions.checkNotNull(connectivityManager, "connection manager is null");
        } catch (NullPointerException e) {
            AylaLog.e(LOG_TAG, e.getMessage());
            throw new InvalidArgumentError(e.getMessage());
        }

        AylaError error = PermissionUtils.checkPermissions(context, SETUP_REQUIRED_PERMISSIONS);
        if (error != null) {
            AylaLog.e(LOG_TAG, error.getMessage());
            throw error;
        }

        if ((_lanHttpServer = sessionManager.getDeviceManager().getLanServer()) == null) {
            try {
                _lanHttpServer = AylaHttpServer.createDefault(null);
            } catch (IOException e) {
                error = new InternalError("failed to create LAN HTTP server", e);
                AylaLog.e(LOG_TAG, error.getMessage());
                throw error;
            }
        }

        _context = context;
        _sessionManager = sessionManager;
        _setupSessionId = UUID.randomUUID().toString();
        _connectivityManager = connectivityManager;
        _logService = new AylaLogService(sessionManager);
        _scanResultsReceiver = new ScanResultsReceiver();
        _wifiStateChangeListeners = new HashSet<>();
        _uiHandler = new Handler(Looper.getMainLooper());
        _setupStartTime = System.currentTimeMillis();
        _setupFinishTime = System.currentTimeMillis();

        AylaDSManager dsManager = sessionManager.getDSManager();
        if (dsManager != null) {
            dsManager.onPause();
        }

        AylaDeviceManager deviceManager = sessionManager.getDeviceManager();
        if (deviceManager != null) {
            deviceManager.setLanModePermitted(false);
        }

        AylaMetricsManager metricsManager = AylaNetworks.sharedInstance().getMetricsManager();
        if (metricsManager != null) {
            // Stop logs upload because there will be no internet connectivity during setup.
            metricsManager.stopMetricsUpload();
        }

        getLogService().start();
        getLogService().addLog(LOG_TAG, "Info","Starting setup");
    }

    /**
     * Constructor for an AylaSetup instance, initialized with the default
     * connectivity manager based on the mobile device SDK level. Use
     * {@link #AylaSetup(Context, AylaSessionManager, AylaConnectivityManager)}
     * instead to specify a customized {@link AylaConnectivityManager}.
     *
     * @param context  Context used for registering receivers, etc.
     * @param sessionManager Session manager used for session management.
     *
     * @throws AppPermissionError If the required permissions have not been granted
     * to perform WiFi setup operations.
     * @throws InvalidArgumentError If invalid passed-in parameters were detected.
     * @throws InternalError Should any internal error was found, such as the internal HTTP
     * server could not be created.
     */
    public AylaSetup(Context context, AylaSessionManager sessionManager) throws AylaError {
        this(context, sessionManager, AylaConnectivityManager.from(context, false));
    }

    /**
     * Scans for wireless access points visible to the mobile device and returns the list of APs.
     * This is used to find devices near the mobile device that have not yet been configured to
     * join the local WiFi network. Ayla devices have SSIDs that have recognizable names, which
     * can be used to filter the result set via a {@link AylaPredicate} filter that may be passed
     * to this method.
     * <p>
     * Once a suitable access point has been found, applications should call
     * {@link #connectToNewDevice} to connect the mobile device to the device, and then
     * have the device scan for visible WiFi access points.
     *
     * @param timeoutInSeconds Maximum time to spend scanning for APs, in seconds. If the scan
     *                         does not complete in the specified time, the caller's
     *                         errorListener will be called with a {@link TimeoutError}.
     * @param filter           An optional Predicate that can be used to filter the result set
     * @param successListener  Listener to receive the list of scan results
     * @param errorListener    Listener to receive an error should one occur.
     *                         Errors that may be passed to the errorListener include:
     * <ul>
     * <li>AppPermissionError on M and above if permissions were not granted</li>
     * <li>PreconditionError on M and above if Location Services are not turned on</li>
     * <li>TimeoutError if the scan timed out</li>
     * </ul>
     *
     * @return Returns an AylaAPIRequest which may be used to to cancel the operation, or
     * returns null should any error occur, for example, found invalid parameters.
     */
    @Nullable
    public AylaAPIRequest scanForAccessPoints(
            int timeoutInSeconds,
            @Nullable AylaPredicate<ScanResult> filter,
            @NonNull Listener<ScanResult[]> successListener,
            @NonNull ErrorListener errorListener) {

        try {
            Preconditions.checkNotNull(successListener, "success listener is null");
            Preconditions.checkNotNull(errorListener, "error listener is null");
        } catch (NullPointerException e) {
            AylaError error = new InvalidArgumentError(e.getMessage());
            AylaLog.e(LOG_TAG, error.getMessage());
            if (errorListener != null) {
                errorListener.onErrorResponse(error);
                return null;
            }
        }

        // Make sure we have necessary permissions to do this first
        String[] permissions = SETUP_REQUIRED_PERMISSIONS;
        AylaError permissionError = PermissionUtils.checkPermissions(getContext(), permissions);
        if (permissionError != null) {
            AylaLog.e(LOG_TAG, permissionError.getMessage());
            errorListener.onErrorResponse(permissionError);
            return null;
        }

        // Make sure location services are turned on on M+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            LocationManager lm = (LocationManager)
                    getContext().getSystemService(Context.LOCATION_SERVICE);
            boolean isGPSEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isNetworkEnabled && !isGPSEnabled) {
                PreconditionError error = new PreconditionError("Location services are not " +
                        "enabled. WiFi scans are not permitted if location services are not " +
                        "enabled on Android M and later.");
                AylaLog.e(LOG_TAG, error.getMessage());
                errorListener.onErrorResponse(error);
                return null;
            }
        }

        final String token = "scanForAccessPoints";

        _scanResultsReceiver.setScanResultListener(new Listener<ScanResult[]>() {
            @Override
            public void onResponse(ScanResult[] scanResults) {
                AylaLog.d(LOG_TAG, "received scan results");
                getContext().unregisterReceiver(_scanResultsReceiver);
                getUiHandler().removeCallbacksAndMessages(token);

                if (filter != null) {
                    List<ScanResult> filteredResults = new ArrayList<>();
                    for (ScanResult result : scanResults) {
                        if (filter.test(result)) {
                            filteredResults.add(result);
                        }
                    }
                    successListener.onResponse(filteredResults.toArray(
                            new ScanResult[filteredResults.size()]));
                } else {
                    successListener.onResponse(scanResults);
                }

                if (shouldSendMetrics()) {
                    AylaSetupMetric setupMetric = new AylaSetupMetric(
                            AylaMetric.LogLevel.INFO, AylaSetupMetric.MetricType.SETUP_SUCCESS,
                            token, getSetupSessionId(),
                            AylaMetric.Result.PARTIAL_SUCCESS, null);
                    sendToMetricsManager(setupMetric);
                }
            }
        });

        getContext().registerReceiver(_scanResultsReceiver,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // Set a timer to time out if necessary
        long targetTimeoutMillis = SystemClock.uptimeMillis() +
                TimeUnit.MILLISECONDS.convert(timeoutInSeconds, TimeUnit.SECONDS);
        getUiHandler().postAtTime(new Runnable() {
            @Override
            public void run() {
                String message = "scan for access points timed out";
                AylaLog.e(LOG_TAG, message);
                getContext().unregisterReceiver(_scanResultsReceiver);
                errorListener.onErrorResponse(new TimeoutError(message));

                if (shouldSendMetrics()) {
                    AylaSetupMetric setupMetric = new AylaSetupMetric(
                            AylaMetric.LogLevel.INFO,
                            AylaSetupMetric.MetricType.SETUP_FAILURE,
                            token, getSetupSessionId(),
                            AylaMetric.Result.FAILURE, message);
                    sendToMetricsManager(setupMetric);
                }
            }
        }, token, targetTimeoutMillis);

        EmptyListener<ScanResult[]> emptyListener = new EmptyListener<>();
        AylaAPIRequest<ScanResult[]> dummyRequest = new AylaAPIRequest<ScanResult[]>(
                Request.Method.GET, null, null,
                ScanResult[].class, null, emptyListener, emptyListener) {
            @Override
            public void cancel() {
                super.cancel();
                AylaLog.d(LOG_TAG, "scan for access points request cancelled");
                getUiHandler().removeCallbacksAndMessages(token);
                try {
                    getContext().unregisterReceiver(_scanResultsReceiver);
                } catch (IllegalArgumentException e) {
                    AylaLog.w(LOG_TAG, "unregister receiver error: " + e);
                }
            }
        };

        WifiManager wifiManager = getAylaConnectivityManager().getWifiManager();
        wifiManager.startScan();

        return dummyRequest;
    }

    /**
     * Scans for wireless access points visible to the mobile device and returns the list of APs.
     * This is used to find devices near the mobile device that have not yet been configured to
     * join the local WiFi network. Ayla devices have SSIDs that have recognizable names, which
     * can be used to filter the result set via a {@link AylaPredicate} filter that may be passed
     * to this method.
     * <p>
     * Once a suitable access point has been found, applications should call
     * {@link #connectToNewDevice} to connect the mobile device to the device, and then
     * have the device scan for visible WiFi access points.
     *
     * @param timeoutInSeconds Maximum time to spend scanning for APs, in seconds. If the scan
     *                         does not complete in the specified time, the caller's
     *                         errorListener will be called with a {@link TimeoutError}.
     * @param scanRegex        A Regular expression which is used to filter the result set, all wifi
     *                         ssid from returned result will match regex
     * @param successListener  Listener to receive the list of scan results
     * @param errorListener    Listener to receive an error should one occur.
     *                         Errors that may be passed to the errorListener include:
     * <ul>
     * <li>AppPermissionError on M and above if permissions were not granted</li>
     * <li>PreconditionError on M and above if Location Services are not turned on</li>
     * <li>TimeoutError if the scan timed out</li>
     * </ul>
     *
     * @return Returns an AylaAPIRequest which may be used to to cancel the operation, or
     * returns null should any error occur, for example, found invalid parameters.
     */
    @Nullable
    public AylaAPIRequest scanAPsWithRegex(int timeoutInSeconds,
                                           @Nullable String scanRegex,
                                           @NonNull  Response.Listener<ScanResult[]> successListener,
                                           @NonNull  ErrorListener errorListener) {
        return scanForAccessPoints(timeoutInSeconds, new AylaPredicate<ScanResult>() {
            @Override
            public boolean test(ScanResult scanResult) {
                return scanResult.SSID.matches(scanRegex);
            }
        }, successListener, errorListener);
    }

    /**
     * Joins the device's access point specified by the ssid and fetches information about the
     * device we just connected to. All access points are open, so no password is required.
     *
     * @param ssid             SSID of the access point to join
     * @param timeoutInSeconds Number of seconds to wait for the join to complete
     * @param successListener  Listener to be notified when the operation was successful
     * @param errorListener    Listener to receive an error should one occur
     * @return an AylaAPIRequest representing this operation
     */
    @Nullable
    public AylaAPIRequest connectToNewDevice(@NonNull String ssid,
                                             int timeoutInSeconds,
                                             @NonNull Listener<AylaSetupDevice> successListener,
                                             @NonNull ErrorListener errorListener) {
        return connectToNewDevice(ssid, null, WifiSecurityType.NONE,
                timeoutInSeconds, successListener, errorListener);
    }

    /**
     * Joins the device's access point specified by the ssid and fetches information about the
     * device we just connected to. All access points are open, so no password is required.
     *
     * @param ssid             SSID of the access point to join
     * @param password         Key to connect to the SSID
     * @param securityType     Security type for this device. see {@link WifiSecurityType} for
     *                         supported security types.
     * @param timeoutInSeconds Number of seconds to wait for the join to complete
     * @param successListener  Listener to be notified when the operation was successful
     * @param errorListener    Listener to receive an error should one occur
     * @return an AylaAPIRequest representing this operation
     */
    @Nullable
    public AylaAPIRequest connectToNewDevice(@NonNull String ssid,
                                             @Nullable String password,
                                             @NonNull  WifiSecurityType securityType,
                                             int timeoutInSeconds,
                                             @NonNull  Listener<AylaSetupDevice> successListener,
                                             @NonNull  ErrorListener errorListener) {

        // Make sure we have permission to do this first
        Context context = AylaNetworks.sharedInstance().getContext();
        AylaError permissionError = PermissionUtils.checkPermissions(context,
                SETUP_REQUIRED_PERMISSIONS);
        if (permissionError != null) {
            getLogService().addLog(LOG_TAG, "Error",
                    "PermissionError in AylaSetup.connectToNewDevice()");
            errorListener.onErrorResponse(permissionError);
            return null;
        }

        if (ssid == null) {
            errorListener.onErrorResponse(new InvalidArgumentError("ssid is null"));
            return null;
        }

        // Store current network info so as to be able to
        // reconnect to once the setup process finishes.
        WifiManager wm = getAylaConnectivityManager().getWifiManager();
        _currentNetworkInfo = wm.getConnectionInfo();

        // This request is not sent, and is used to cancel
        // the chained requests in the compound request.
        AylaAPIRequest originalRequest = AylaAPIRequest.dummyRequest(
                AylaSetupDevice.class, successListener, errorListener);

        final String token = "connectToNewDevice";

        // This errorListener will call the successListener if error
        // occurred due to mobile trying cleartext setup on a secure device.
        // In this case, _setupDevice does not have a DSN.
        final ErrorListener deviceDetailsErrorListener = new ErrorListener() {
            @Override
            public void onErrorResponse(AylaError error) {
                if (error instanceof ServerError) {
                    if (((ServerError) error).getServerResponseCode() == 404) {
                        AylaLog.d(LOG_TAG, "Got error 404. Starting secure setup");
                        _isSecureSetup = true;
                        fetchDeviceDetailsLAN(originalRequest, successListener, errorListener);

                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_SUCCESS, token,
                                    getSetupSessionId(), AylaMetric.Result.PARTIAL_SUCCESS, null);
                            setupMetric.setMetricText("Starting secure server setup");
                            setupMetric.secureSetup(isSecureSetup());
                            setupMetric.setDeviceSecurityType(securityType.stringValue());
                            sendToMetricsManager(setupMetric);
                        }
                    }
                } else {
                    _isSecureSetup = false;
                    errorListener.onErrorResponse(error);

                    if (shouldSendMetrics()) {
                        AylaSetupMetric setupMetric = new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                                AylaSetupMetric.MetricType.SETUP_FAILURE, token,
                                getSetupSessionId(), AylaMetric.Result.FAILURE, error.getMessage());
                        setupMetric.secureSetup(isSecureSetup());
                        setupMetric.setDeviceSecurityType(securityType.stringValue());
                        sendToMetricsManager(setupMetric);
                    }
                }
            }
        };

        // This successListener will return the _setupDevice object to the app, so that we can call
        // updateFrom() on this object to let the app know of any changes
        final Response.Listener<AylaSetupDevice> deviceDetailsSuccessListener =
                new Response.Listener<AylaSetupDevice>() {
                    @Override
                    public void onResponse(AylaSetupDevice response) {
                        _isSecureSetup = false;
                        successListener.onResponse(response);

                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                    token, getSetupSessionId(),
                                    AylaMetric.Result.PARTIAL_SUCCESS, null);
                            setupMetric.setMetricText("fetchDeviceDetail() was success");
                            setupMetric.setDeviceSecurityType(securityType.stringValue());
                            sendToMetricsManager(setupMetric);
                        }
                    }
                };

        AylaConnectivityManager connectivityManager = getAylaConnectivityManager();
        AylaConnectivityManager.OnConnectResultCallback connectivityChangedCallback =
                new AylaConnectivityManager.OnConnectResultCallback() {
                    @Override
                    public void onAvailable(@NonNull String ssid) {
                        AylaLog.d(LOG_TAG, "connected to setup device " + ssid);
                        connectivityManager.unregisterConnectResultCallback(this);
                        setSetupDeviceIp(connectivityManager.getGatewayIpAddress());
                        fetchDeviceDetailLan(originalRequest, deviceDetailsSuccessListener,
                                deviceDetailsErrorListener);
                    }

                    @Override
                    public void onUnavailable(@Nullable AylaError error) {
                        AylaLog.d(LOG_TAG, "unable to connect to " + ssid);
                        connectivityManager.unregisterConnectResultCallback(this);
                        errorListener.onErrorResponse(error);
                    }
                };

        connectivityManager.registerConnectResultCallback(connectivityChangedCallback);
        connectivityManager.connect(ssid, password, securityType, timeoutInSeconds);

        return originalRequest;
    }

    /**
     * Initiates a scan for WiFi access points on the setup device. If successful, this call will
     * tell the setup device to start scanning for visible WiFi access points. The results of the
     * scan can be obtained via a call to {@link #fetchDeviceAccessPoints}.
     *
     * @param successListener Success listener called if the operation is successful
     * @param errorListener   Listener called in case of an error
     * @return the AylaAPIRequest representing this request
     */
    public AylaAPIRequest startDeviceScanForAccessPoints(
            final Listener<EmptyResponse> successListener,
            final ErrorListener errorListener) {
        if (isSecureSetup()) {
            return startDeviceScanForAccessPointsLAN(successListener, errorListener);
        } else {
            return startDeviceScanForAccessPointsLan(successListener, errorListener);
        }
    }

    protected AylaAPIRequest startDeviceScanForAccessPointsLan(
            Listener<EmptyResponse> successListener, ErrorListener errorListener) {
        AylaLog.d(LOG_TAG, "start insecure scan for device access points");
        String url = formatLocalUrl("wifi_scan.json");
        final AylaAPIRequest<EmptyResponse> request = new AylaAPIRequest<EmptyResponse>(
                        Request.Method.POST, url, null, EmptyResponse.class,
                        null,
                        successListener,
                        errorListener) {
            @Override
            protected Response<EmptyResponse> parseNetworkResponse(NetworkResponse networkResponse) {
                AylaLog.d(LOG_TAG, "wifi scan response for url:" + url
                        + ", status code:" + networkResponse.statusCode
                        + ", data:" + new String(networkResponse.data));
                return Response.success(new EmptyResponse(), null);
            }
        };
        getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        return request;
    }

    protected AylaAPIRequest startDeviceScanForAccessPointsLAN(
            Listener<EmptyResponse> successListener, ErrorListener errorListener) {
        AylaLog.d(LOG_TAG, "start secure scan for device access points");
        String START_DEVICE_SCAN_FOR_ACCESS_POINTS = "startDeviceScanForAccessPoints";

        AylaSetupDevice setupDevice = getSetupDevice();
        StartScanCommand cmd = new StartScanCommand();
        AylaLanRequest request = new AylaLanRequest(setupDevice, cmd, setupDevice.getSessionManager(),
                new Listener<AylaLanRequest.LanResponse>() {
                    @Override
                    public void onResponse(AylaLanRequest.LanResponse response) {
                        AylaLog.d(LOG_TAG, "startDeviceScanForAccessPoints response");
                        AylaError error = cmd.getResponseError();
                        if (error != null) {
                            AylaLog.e(LOG_TAG, "Start scan command returned error " + error.getMessage());
                            errorListener.onErrorResponse(error);
                            return;
                        }

                        // Add a delay to wait for device to complete scan.
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                successListener.onResponse(new EmptyResponse());
                                AylaLog.d(LOG_TAG, "Start scan command sent");
                                if (shouldSendMetrics()) {
                                    AylaSetupMetric setupMetric = new AylaSetupMetric(
                                            AylaMetric.LogLevel.INFO,
                                            AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                            START_DEVICE_SCAN_FOR_ACCESS_POINTS,
                                            getSetupSessionId(),
                                            AylaMetric.Result.PARTIAL_SUCCESS, null);
                                    sendToMetricsManager(setupMetric);
                                }
                            }
                        }, DEFAULT_DELAY_FETCH_ACCESS_POINTS);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        AylaLog.e(LOG_TAG, "Error in Start scan command " + error.getMessage());
                        errorListener.onErrorResponse(error);
                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_FAILURE,
                                    START_DEVICE_SCAN_FOR_ACCESS_POINTS,
                                    getSetupSessionId(), AylaMetric.Result.FAILURE, error.getMessage());
                            sendToMetricsManager(setupMetric);
                        }
                    }
                });

        if (setupDevice.getLanModule() != null) {
            setupDevice.getLanModule().sendRequest(request);
        } else {
            errorListener.onErrorResponse(new PreconditionError(LAN_PRECONDITION_ERROR));
        }

        return request;
    }

    /**
     * Fetches the list of WiFi Access Points that were discovered by the device via a prior call
     * to {@link #startDeviceScanForAccessPoints}.
     *
     *
     * @param timeoutInSeconds the maximum allowed value before the request timed out.
     * @param filter          A Predicate used to filter the scan results returned.
     *                        May be null to return all results.
     * @param successListener Listener to receive the {@link AylaWifiScanResults} containing the
     *                        set of Access Points discovered by the setup device
     * @param errorListener   Listener to receive an error should one occur
     * @return the AylaAPIRequest for this operation, which may be canceled.
     */
    @Nullable
    public AylaAPIRequest fetchDeviceAccessPoints(
            int timeoutInSeconds,
            final AylaPredicate<AylaWifiScanResults.Result> filter,
            final Listener<AylaWifiScanResults> successListener,
            final ErrorListener errorListener) {
        if (isSecureSetup()) {
            return fetchDeviceAccessPointsLAN(timeoutInSeconds, filter, successListener, errorListener);
        } else {
            return fetchDeviceAccessPointsLan(timeoutInSeconds, filter, successListener, errorListener);
        }
    }

    protected AylaAPIRequest fetchDeviceAccessPointsLan(
            int timeoutInSeconds,
            AylaPredicate<AylaWifiScanResults.Result> filter,
            Listener<AylaWifiScanResults> successListener,
            ErrorListener errorListener) {
        AylaLog.d(LOG_TAG, "fetch device access point in insecure lan");
        String FETCH_DEVICE_ACCESS_POINTS = "fetchDeviceAccessPoints";
        String url = formatLocalUrl("wifi_scan_results.json");
        AylaAPIRequest<AylaWifiScanResults.Wrapper> request =
                new AylaAPIRequest<AylaWifiScanResults.Wrapper>(Request.Method.GET,
                        url, null, AylaWifiScanResults.Wrapper.class, null,
                        new Listener<AylaWifiScanResults.Wrapper>() {
                            @Override
                            public void onResponse(AylaWifiScanResults.Wrapper response) {
                                if (filter != null) {
                                    AylaWifiScanResults.Result[] results = response.wifi_scan.results;
                                    if (results == null) {
                                        results = new AylaWifiScanResults.Result[0];
                                    }

                                    List<AylaWifiScanResults.Result> filteredResults = new ArrayList<>();
                                    for (AylaWifiScanResults.Result result : results) {
                                        if (filter.test(result)) {
                                            filteredResults.add(result);
                                        }
                                    }
                                    response.wifi_scan.results = filteredResults.toArray(
                                            new AylaWifiScanResults.Result[filteredResults.size()]);
                                }
                                successListener.onResponse(response.wifi_scan);

                                if (shouldSendMetrics()) {
                                    AylaSetupMetric setupMetric = new AylaSetupMetric(
                                            AylaMetric.LogLevel.INFO,
                                            AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                            FETCH_DEVICE_ACCESS_POINTS,
                                            getSetupSessionId(),
                                            AylaMetric.Result.PARTIAL_SUCCESS, null);
                                    setupMetric.secureSetup(isSecureSetup());
                                    setupMetric.setMetricText("fetched scan list size "
                                            + response.wifi_scan.results.length);
                                    sendToMetricsManager(setupMetric);
                                }
                            }
                        }, errorListener) {
                    @Override
                    public void deliverError(VolleyError error) {
                        super.deliverError(error);
                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_FAILURE,
                                    FETCH_DEVICE_ACCESS_POINTS,
                                    getSetupSessionId(),
                                    AylaMetric.Result.FAILURE, error.getMessage());
                            sendToMetricsManager(setupMetric);
                        }
                    }
                };

        request.setRetryPolicy(new DefaultRetryPolicy(
                timeoutInSeconds * 1000 / 2, 2, 1.0f));
        getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        return request;
    }

    protected AylaAPIRequest fetchDeviceAccessPointsLAN(
            int timeoutInSeconds,
            AylaPredicate<AylaWifiScanResults.Result> filter,
            Listener<AylaWifiScanResults> successListener,
            ErrorListener errorListener) {
        AylaLog.d(LOG_TAG, "fetch device access points in secure LAN");
        String FETCH_DEVICE_ACCESS_POINTS = "fetchDeviceAccessPoints";
        AylaLanCommand cmd = new AylaLanCommand("GET", "wifi_scan_results.json",
                null, "/local_lan/wifi_scan_results.json");
        cmd.setRequestTimeout(timeoutInSeconds * 1000);

        AylaLanRequest request = new AylaLanRequest(getSetupDevice(), cmd,
                getSetupDevice().getSessionManager(),
                new Listener<AylaLanRequest.LanResponse>() {
                    @Override
                    public void onResponse(AylaLanRequest.LanResponse response) {
                        AylaError error = cmd.getResponseError();
                        if (error != null) {
                            AylaLog.d(LOG_TAG, "fetchDeviceScanResults error " + error.getMessage());
                            errorListener.onErrorResponse(error);
                            if (shouldSendMetrics()) {
                                AylaSetupMetric setupMetric = new AylaSetupMetric(
                                        AylaMetric.LogLevel.INFO,
                                        AylaSetupMetric.MetricType.SETUP_FAILURE,
                                        FETCH_DEVICE_ACCESS_POINTS,
                                        getSetupSessionId(),
                                        AylaMetric.Result.FAILURE, error.getMessage());
                                setupMetric.secureSetup(isSecureSetup());
                                sendToMetricsManager(setupMetric);
                            }
                            return;
                        }

                        String commandResponse = cmd.getModuleResponse();
                        AylaLog.d(LOG_TAG, "fetchDeviceAccessPointsLAN response:" + commandResponse);
                        AylaWifiScanResults.Wrapper wrapper = AylaNetworks.sharedInstance()
                                .getGson().fromJson(commandResponse, AylaWifiScanResults.Wrapper.class);

                        if (filter != null && wrapper != null) {
                            AylaWifiScanResults.Result[] results = wrapper.wifi_scan.results;
                            if (results == null) {
                                results = new AylaWifiScanResults.Result[0];
                            }

                            List<AylaWifiScanResults.Result> filteredResults = new ArrayList<>();
                            for (AylaWifiScanResults.Result result : results) {
                                if (filter.test(result)) {
                                    filteredResults.add(result);
                                }
                            }
                            wrapper.wifi_scan.results = filteredResults.toArray(
                                    new AylaWifiScanResults.Result[filteredResults.size()]);
                        }
                        successListener.onResponse(wrapper == null ? null : wrapper.wifi_scan);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        AylaLog.e(LOG_TAG, "fetchDeviceScanResults error " + error.getMessage());
                        errorListener.onErrorResponse(error);
                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_FAILURE,
                                    FETCH_DEVICE_ACCESS_POINTS,
                                    getSetupSessionId(),
                                    AylaMetric.Result.FAILURE, error.getMessage());
                            setupMetric.secureSetup(isSecureSetup());
                            sendToMetricsManager(setupMetric);
                        }
                    }
                });

        if (getSetupDevice().getLanModule() != null) {
            getSetupDevice().getLanModule().sendRequest(request);
        } else {
            errorListener.onErrorResponse(new PreconditionError(LAN_PRECONDITION_ERROR));
        }

        return request;
    }

    /**
     * Fetches the list of WiFi Access Points that were discovered by the device via a prior call
     * to {@link #startDeviceScanForAccessPoints}.
     *
     * @param filter          A Predicate used to filter the scan results returned. May be null to
     *                        return all results.
     * @param successListener Listener to receive the {@link AylaWifiScanResults} containing the
     *                        set of Access Points discovered by the setup device
     * @param errorListener   Listener to receive an error should one occur
     * @return the AylaAPIRequest for this operation, which may be canceled.
     */
    public AylaAPIRequest fetchDeviceAccessPoints(
            final AylaPredicate<AylaWifiScanResults.Result> filter,
            final Listener<AylaWifiScanResults> successListener,
            final ErrorListener errorListener) {
        return fetchDeviceAccessPoints(DEFAULT_TIMEOUT_FETCH_ACCESS_POINTS,
                filter, successListener, errorListener);
    }

    /**
     * Fetches the list of WiFi Access Points that were discovered by the device via a prior call
     * to {@link #startDeviceScanForAccessPoints}.
     *
     * @param scanRegex       A regular expression used to filter the scan results returned,
     *                        returned WiFi ssid won't match regexp
     * @param successListener Listener to receive the {@link AylaWifiScanResults} containing
     *                        the set of Access Points discovered by the setup device
     * @param errorListener   Listener to receive an error should one occur
     * @return the AylaAPIRequest for this operation, which can be used to cancel the request.
     */
    @Nullable
    public AylaAPIRequest fetchDeviceAPsWithRegex(
            final String scanRegex,
            final Response.Listener<AylaWifiScanResults> successListener,
            ErrorListener errorListener) {
        return fetchDeviceAccessPoints(new AylaPredicate<AylaWifiScanResults.Result>() {
            @Override
            public boolean test(AylaWifiScanResults.Result result) {
                return !result.ssid.matches(scanRegex);
            }
        }, successListener, errorListener);
    }

    /**
     * Sends the SSID and password of the WiFi network the setup device should join,
     * along with an optional setup token and location coordinates.
     *
     * @param ssid             SSID the setup device should attempt to join
     * @param password         Password for the WiFi network
     * @param setupToken       Optional setup token provided by the application
     * @param latitude         Optional latitude
     * @param longitude        Optional longitude
     * @param timeoutInSeconds Timeout to poll for connection
     * @param successListener  Listener called on a successful operation
     * @param errorListener    Listener called if an error occurred. The possible errors are:
     *                         <ol>
     *                           <li><code>InvalidArgumentError</code>, for invalid parameters
     *                           passed in, such as invalid ssid name or setup token. </li>
     *                           <li><code>TimeoutError</code>, for polling WiFi connect status
     *                           timed out in the specified period of time. </li>
     *                           <li><code>NetworkError</code>, for network issues while polling
     *                           WiFi connect status on the setup device, most probably due to the
     *                           connection to the device was lost. </li>
     *                           <li><code>InternalError</code>, for errors arose from the device
     *                           side, such as InvalidKey, IncorrectKey, ConnectionTimeout for
     *                           wrong password, or disallowed in setup mode, etc. Please check
     *                           {@link AylaWifiStatus.HistoryItem.Error} for more device related errors. </li>
     *                           <li><code>JsonError</code>, for invalid JSON response from the device server. </li>
     *                           <li><code>ServerError</code>, for errors responded from the device server. </li>
     *                         </ol>
     * @return the AylaAPIRequest for this operation
     */
    @Nullable
    public AylaAPIRequest connectDeviceToService(@NonNull  String ssid,
                                                 @NonNull  String password,
                                                 @Nullable String setupToken,
                                                 @Nullable Double latitude,
                                                 @Nullable Double longitude,
                                                 int timeoutInSeconds,
                                                 @NonNull Listener<AylaWifiStatus> successListener,
                                                 @NonNull ErrorListener errorListener) {
        try {
            Preconditions.checkNotNull(ssid,"ssid is null");
            Preconditions.checkNotNull(successListener,"success listener is null");
            Preconditions.checkNotNull(errorListener,"error listener is null");
        } catch (NullPointerException e) {
            AylaLog.e(LOG_TAG, e.getMessage());
            String errorMsg = e.getMessage();
            errorListener.onErrorResponse(new InvalidArgumentError(errorMsg));

            if (shouldSendMetrics()) {
                AylaSetupMetric setupMetric = new AylaSetupMetric (
                        AylaMetric.LogLevel.INFO,
                        AylaSetupMetric.MetricType.SETUP_FAILURE,
                        "connectDeviceToService",
                        getSetupSessionId(),
                        AylaMetric.Result.FAILURE,
                        errorMsg);
                setupMetric.secureSetup(isSecureSetup());
                sendToMetricsManager(setupMetric);
            }
            return null;
        }

        if (setupToken != null && setupToken.length() > 8) {
            String errorMsg = "Setup token may be 8 characters at most";
            AylaLog.e(LOG_TAG, errorMsg);
            errorListener.onErrorResponse(new InvalidArgumentError(errorMsg));
            return null;
        }

        _lastWifiStatus = null;
        _targetDeviceSSIDName = ssid;
        _targetDeviceSSIDPassword = password;

        if (isSecureSetup()) {
            return connectDeviceToServiceLAN(ssid, password, setupToken,
                    latitude, longitude, timeoutInSeconds, successListener, errorListener);
        } else {
            return connectDeviceToServiceLan(ssid, password, setupToken,
                    latitude, longitude, timeoutInSeconds, successListener, errorListener);
        }
    }

    @NonNull
    private AylaAPIRequest connectDeviceToServiceLAN(String ssid, String password,
                                                     String setupToken,
                                                     Double latitude, Double longitude,
                                                     int timeoutInSeconds,
                                                     Listener<AylaWifiStatus> successListener,
                                                     ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("ssid", ssid);
        if (password != null) {
            params.put("key", password);
        }

        if (setupToken != null) {
            params.put("setup_token", setupToken);
        }

        if (AylaNetworks.sharedInstance().getUserDataGrants().isEnabled(
                AylaUserDataGrant.AYLA_USER_DATA_GRANT_METRICS_SERVICE)) {
            if (latitude != null && longitude != null) {
                params.put("location", String.format(Locale.US, "%f,%f", latitude, longitude));
            }
        }

        String url = URLHelper.appendParameters("wifi_connect.json", params);
        AylaLanCommand command = new AylaLanCommand("POST", url, "none",
                "/local_lan/connect_status");
        command.setRequestTimeout(timeoutInSeconds * 1000);

        AylaAPIRequest originalRequest = AylaAPIRequest.dummyRequest(
                AylaLanRequest.LanResponse.class, null, null);
        AylaLanRequest connectRequest = new AylaLanRequest(getSetupDevice(), command, getSessionManager(),
                new Listener<AylaLanRequest.LanResponse>() {
                    @Override
                    public void onResponse(AylaLanRequest.LanResponse lanResponse) {
                        if (originalRequest.isCanceled()) {
                            return;
                        }

                        String response = command.getModuleResponse();
                        AylaLog.d(LOG_TAG, "connectDeviceToServiceLAN response:" + response);
                        AylaError error = parseWiFiConnectError(command);
                        if (error != null) {
                            AylaLog.d(LOG_TAG, "wifi connect error:" + error.getMessage());
                            errorListener.onErrorResponse(error);
                        } else if (getSetupDevice().hasFeature(AylaSetupDevice.FEATURE_AP_STA)) {
                            AylaLog.d(LOG_TAG, "Device supports ap-sta, polling wifi status...");
                            pollDeviceConnectToAP(originalRequest, ssid, timeoutInSeconds,
                                    successListener, errorListener);
                        } else {
                            AylaLog.d(LOG_TAG, "Device does not support ap-sta, " +
                                    "should confirm device connection from the cloud");
                            successListener.onResponse(getLastWifiStatus());
                        }
                    }
                }, errorListener);

        connectRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeoutInSeconds * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        originalRequest.setChainedRequest(connectRequest);

        getSetupDevice().getLanModule().sendRequest(connectRequest);

        return originalRequest;
    }

    @NonNull
    private AylaAPIRequest connectDeviceToServiceLan(String ssid, String password,
                                                     String setupToken,
                                                     Double latitude, Double longitude,
                                                     int timeoutInSeconds,
                                                     Listener<AylaWifiStatus> successListener,
                                                     ErrorListener errorListener) {
        Map<String, String> params = new HashMap<>();
        params.put("ssid", ssid);
        if (password != null) {
            params.put("key", password);
        }

        if (setupToken != null) {
            params.put("setup_token", setupToken);
        }

        if (AylaNetworks.sharedInstance().getUserDataGrants().isEnabled(
                AylaUserDataGrant.AYLA_USER_DATA_GRANT_METRICS_SERVICE)) {
            if (latitude != null && longitude != null) {
                params.put("location", String.format(Locale.US, "%f,%f", latitude, longitude));
            }
        }

        String url = URLHelper.appendParameters(formatLocalUrl("wifi_connect.json"), params);

        AylaAPIRequest originalRequest = AylaAPIRequest.dummyRequest(
                AylaLanRequest.LanResponse.class, null, null);
        AylaAPIRequest<AylaWifiStatus.Wrapper> connectRequest =
                new AylaAPIRequest<>(Request.Method.POST,
                        url, null, AylaWifiStatus.Wrapper.class, null,
                        new Listener<AylaWifiStatus.Wrapper>() {
                            @Override
                            public void onResponse(AylaWifiStatus.Wrapper response) {
                                if (originalRequest.isCanceled()) {
                                    return;
                                }

                                if (response == null) {
                                    AylaLog.d(LOG_TAG, "Lan wifi connect returned null");
                                    pollDeviceConnectToAP(originalRequest, ssid, timeoutInSeconds,
                                            successListener, errorListener);
                                    return;
                                }

                                AylaWifiStatus status = response.wifi_status;
                                AylaLog.d(LOG_TAG, "connectDeviceToServiceLan status:" + status);

                                if (getSetupDevice().hasFeature(AylaSetupDevice.FEATURE_AP_STA)) {
                                    AylaLog.d(LOG_TAG, "Device supports ap-sta, polling wifi status...");
                                    AylaError error = shouldPollDeviceConnectToAP(status, getLastWifiStatus());
                                    if (error != null) {
                                        AylaLog.d(LOG_TAG, "Lan wifi connect error:" + error.getMessage());
                                        errorListener.onErrorResponse(error);
                                    } else {
                                        pollDeviceConnectToAP(originalRequest, ssid, timeoutInSeconds,
                                                successListener, errorListener);
                                    }
                                    updateAndNotifyStatus(status);
                                } else {
                                    AylaLog.d(LOG_TAG, "Device does not support ap-sta, " +
                                            "should confirm device connection from the cloud");
                                    successListener.onResponse(getLastWifiStatus());
                                }
                            }
                        }, errorListener);

        connectRequest.setRetryPolicy(new DefaultRetryPolicy(
                timeoutInSeconds * 1000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        originalRequest.setChainedRequest(connectRequest);
        getSessionManager().getDeviceManager().sendDeviceServiceRequest(connectRequest);

        return originalRequest;
    }

    /**
     * Parses and returns the error that led to the wifi connect failure.
     * The possible errors are:
     * <ol>
     *    <li>LAN response error returned from {@link AylaLanCommand#getResponseError()},
     *    which was set by either {@link AylaLanCommand#setErrorResponse(AylaError)}
     *    or {@link AylaLanCommand#setModuleError(AylaError)}.</li>
     *    <li>Errors from the LAN module response, such as "disallowed in setup mode".
     *     Errors of this kind are defined in the {@link Payload.Error} construct. </li>
     *    <li>WiFi connection error set by the module and appears at the top most position
     *    in the connection history. Errors of this kind are defined in the
     *    {@link AylaWifiStatus.HistoryItem.Error} construct.</li>
     * </ol>
     * <p>
     * This utility method detects these kinds of error in turn and return the
     * specific error should one was found.
     *
     * @return Returns the possible error that caused the wifi connect error,
     * or returns null if none was found.
     */
    @Nullable
    private AylaError parseWiFiConnectError(@NonNull LanCommand command) {
        AylaError error = command.getResponseError();
        if (error == null && command.getModuleResponse() != null) {
            String moduleResponse = command.getModuleResponse();
            Gson gson = AylaNetworks.sharedInstance().getGson();
            Payload.Error payloadError = gson.fromJson(moduleResponse, Payload.Error.class);
            // {
            //     "seq_no": 2,
            //     "data": {
            //              "error": 21,
            //              "msg": "disallowed in setup mode"
            //     }
            // }
            if (payloadError != null
                    && (payloadError.error != 0 || payloadError.msg != null)) {
                error = new InternalError(String.format(Locale.US, "%s(%d)",
                        payloadError.msg, payloadError.error));
            } else {
                AylaWifiStatus.HistoryItem history = gson.fromJson(moduleResponse,
                        AylaWifiStatus.HistoryItem.class);
                if (history != null && history.error != null
                        && history.error != AylaWifiStatus.HistoryItem.Error.NoError) {
                    error = new InternalError(history.error.name());
                }
            }
        }

        return error;
    }

    /**
     * Fetches the current WiFi status from the module.
     *
     * @param successListener Listener to receive the AylaWifiStatus object if successful.
     *                        The object contains the current status as well as the history
     *                        of connections, which can be used to decide if the device is
     *                        connected to the network.
     * @param errorListener   Listener to receive an error should one occur
     * @return the AylaAPIRequest for this request, which may be canceled.
     */
    @NonNull
    public AylaAPIRequest fetchDeviceWifiStatus(
            final Listener<AylaWifiStatus> successListener,
            final ErrorListener errorListener) {
        AylaSetupDevice setupDevice = getSetupDevice();
        if (isSecureSetup()) {
            AylaLanCommand command = new AylaLanCommand("GET",
                    "wifi_status.json", null, "/local_lan/wifi_status.json");
            AylaLanRequest request = new AylaLanRequest(setupDevice, command, getSessionManager(), new
                    Listener<AylaLanRequest.LanResponse>() {
                        @Override
                        public void onResponse(AylaLanRequest.LanResponse response) {
                            AylaError error = command.getResponseError();
                            if (error != null) {
                                AylaLog.d(LOG_TAG, "LAN fetch wifi_status error: " + error.getMessage());
                                errorListener.onErrorResponse(error);
                                return;
                            }
                            String moduleResponse = command.getModuleResponse();
                            AylaLog.d(LOG_TAG, "LAN fetch wifi_status response: " + moduleResponse);
                            AylaWifiStatus.Wrapper wrapper = AylaNetworks.sharedInstance().getGson()
                                    .fromJson(moduleResponse, AylaWifiStatus.Wrapper.class);
                            successListener.onResponse(wrapper.wifi_status);
                        }
                    }, errorListener);
            setupDevice.getLanModule().sendRequest(request);
            return request;
        } else {
            AylaAPIRequest<AylaWifiStatus.Wrapper> request = new AylaAPIRequest<>(
                    Request.Method.GET, formatLocalUrl("wifi_status.json"), null,
                    AylaWifiStatus.Wrapper.class, null,
                    new Listener<AylaWifiStatus.Wrapper>() {
                        @Override
                        public void onResponse(AylaWifiStatus.Wrapper response) {
                            successListener.onResponse(response.wifi_status);
                        }
                    }, errorListener);
            setupDevice.getLanModule().sendRequest(request);
            return request;
        }
    }

    /**
     * Fetches the registration info from the module. The {@link AylaRegInfo} object
     * contains the information required to register this device, such as reg token
     * and registration type.
     *
     * @param successListener Listener to receive the {@link AylaRegInfo} object if successful
     * @param errorListener   Listener to receive an error should one occur
     * @return the AylaAPIRequest for this request, which may be canceled.
     */
    @Nullable
    protected AylaAPIRequest fetchRegInfo(final Listener<AylaRegInfo> successListener,
                                        final ErrorListener errorListener) {
        AylaLanModule lanModule = getSetupDevice().getLanModule();
        if (lanModule == null) {
            getLogService().addLog(LOG_TAG, "Error", "AylaSetup.fetchRegInfo(). " +
                    "PreconditionError. Device is not in LAN mode");
            errorListener.onErrorResponse(new PreconditionError("Device is not in LAN mode"));

            if (shouldSendMetrics()) {
                AylaSetupMetric setupMetric = new AylaSetupMetric
                        (AylaMetric.LogLevel.INFO, AylaSetupMetric.MetricType.SETUP_FAILURE,
                                "fetchRegInfo", getSetupSessionId(),
                                AylaMetric.Result.FAILURE,
                                "Device is not in LAN mode");
                setupMetric.secureSetup(isSecureSetup());
                sendToMetricsManager(setupMetric);
            }
            return null;
        }

        final String FETCH_REG_INFO = "fetchRegInfo";

        if (isSecureSetup()) {
            AylaLanCommand command = new AylaLanCommand("GET",
                    "regtoken.json", null, "/local_lan/regtoken.json");
            AylaLanRequest request = new AylaLanRequest(getSetupDevice(), command, _sessionManager, new
                    Listener<AylaLanRequest.LanResponse>() {
                        @Override
                        public void onResponse(AylaLanRequest.LanResponse response) {
                            AylaError error = command.getResponseError();
                            if (error != null) {
                                AylaLog.d(LOG_TAG, "fetch regtoken command returned error " +
                                        error.getMessage());
                                errorListener.onErrorResponse(error);
                                if (shouldSendMetrics()) {
                                    AylaSetupMetric setupMetric = new AylaSetupMetric(
                                            AylaMetric.LogLevel.INFO,
                                            AylaSetupMetric.MetricType.SETUP_FAILURE,
                                            FETCH_REG_INFO, getSetupSessionId(),
                                            AylaMetric.Result.FAILURE, error.getMessage());
                                    setupMetric.secureSetup(isSecureSetup());
                                    sendToMetricsManager(setupMetric);
                                }
                                return;
                            }

                            String moduleResponse = command.getModuleResponse();
                            AylaLog.d(LOG_TAG, "fetch regtoken response " + moduleResponse);
                            Gson gson = AylaNetworks.sharedInstance().getGson();
                            AylaRegInfo regInfo = gson.fromJson(moduleResponse, AylaRegInfo.class);
                            successListener.onResponse(regInfo);

                            if (shouldSendMetrics()) {
                                AylaSetupMetric setupMetric = new AylaSetupMetric(
                                        AylaMetric.LogLevel.INFO,
                                        AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                        FETCH_REG_INFO, getSetupSessionId(),
                                        AylaMetric.Result.PARTIAL_SUCCESS, null);
                                setupMetric.secureSetup(isSecureSetup());
                                sendToMetricsManager(setupMetric);
                            }
                        }
                    }, errorListener);
            lanModule.sendRequest(request);
            return request;
        } else {
            AylaAPIRequest<AylaRegInfo> request = new AylaAPIRequest<>(
                    Request.Method.GET, formatLocalUrl("regtoken.json"), null,
                    AylaRegInfo.class, null,
                    new Listener<AylaRegInfo>() {
                        @Override
                        public void onResponse(AylaRegInfo response) {
                            successListener.onResponse(response);
                            if (shouldSendMetrics()) {
                                AylaSetupMetric setupMetric = new AylaSetupMetric(
                                        AylaMetric.LogLevel.INFO,
                                        AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                        FETCH_REG_INFO,
                                        getSetupSessionId(),
                                        AylaMetric.Result.PARTIAL_SUCCESS,
                                                null);
                                setupMetric.secureSetup(isSecureSetup());
                                sendToMetricsManager(setupMetric);
                            }
                        }
                    }, errorListener);

            lanModule.sendRequest(request);
            return request;
        }
    }

    /**
     * Polls the device for its wifi status, looking to see whether it joins the specified ssid.
     * When the device reports that it is connected to the specified ssid, the successListener
     * will be called. If an error is returned from the module, the errorListener will be called.
     * Otherwise this method will poll until it times out.
     * <p>
     * This method is an internal part of a chained request kicked off by {@link #connectDeviceToService}.
     * The original request is passed in to this method so that they can be canceled or chained.
     *
     * @param originalRequest      Original request that kicked this off. This is from
     *                             connectDeviceToService.
     * @param ssid                 SSID the device is trying to connect to
     * @param timeoutInSeconds     Timeout to poll for connection
     * @param successListener      Listener to be notified once the AP has been joined. The
     *                             listener will be provided with the final AylaWifiStatus
     *                             received from the module.
     * @param errorListener        Listener to be notified in case of an error
     */
    private void pollDeviceConnectToAP(final AylaAPIRequest originalRequest,
                                       final String ssid,
                                       final int timeoutInSeconds,
                                       final Listener<AylaWifiStatus> successListener,
                                       final ErrorListener errorListener) {

        final Handler pollHandler = getUiHandler();
        final String POLL_DEVICE_CONNECT_TO_AP = "pollDeviceConnectToAP";

        // We also need to check for the AP going away. Sometimes the module will just
        // drop the AP while we are polling it.
        AylaConnectivity connectivity = AylaNetworks.sharedInstance().getConnectivity();
        AylaConnectivity.AylaConnectivityListener connectivityListener =
                new AylaConnectivity.AylaConnectivityListener() {
                    @Override
                    public void connectivityChanged(boolean wifiEnabled,
                                                    boolean cellularEnabled) {
                        String msg = "Network state changed while polling WiFi connect " +
                                "status on the setup device. Unable to determine if the " +
                                "device has joined the WiFi network";
                        AylaLog.d(LOG_TAG, msg);

                        // We don't care what happened- if connectivity has changed,
                        // then our polling is not going to work. We'll just consider
                        // ourselves done.
                        pollHandler.removeCallbacksAndMessages(POLL_DEVICE_CONNECT_TO_AP);
                        AylaNetworks.sharedInstance().getConnectivity().unregisterListener(this);
                        if (!originalRequest.isCanceled()) {
                            NetworkError error = new NetworkError(msg, null);
                            errorListener.onErrorResponse(error);
                        }
                    }
                };
        connectivity.registerListener(connectivityListener);

        long targetTimeoutMillis = SystemClock.uptimeMillis() + timeoutInSeconds * 1000;
        _lastWifiStatus = null;

        Runnable pollWifiStatusRunnable = new Runnable() {
            Runnable _runnable = this;
            AylaAPIRequest _fetchWifiStatusRequest;

            @Override
            public void run() {
                if (originalRequest.isCanceled()) {
                    return;
                }

                _fetchWifiStatusRequest = fetchDeviceWifiStatus(
                        new Listener<AylaWifiStatus>() {
                            @Override
                            public void onResponse(AylaWifiStatus status) {
                                if (isDeviceConnectedToAP(ssid, status)) {
                                    AylaLog.i(LOG_TAG, "device connected to " + ssid);
                                    updateAndNotifyStatus(status);
                                    connectivity.unregisterListener(connectivityListener);
                                    pollHandler.removeCallbacksAndMessages(POLL_DEVICE_CONNECT_TO_AP);
                                    successListener.onResponse(status);
                                    tryToFetchRegistrationInfo(status);
                                } else {
                                    AylaError error = shouldPollDeviceConnectToAP(status, getLastWifiStatus());
                                    if (error != null) {
                                        AylaLog.e(LOG_TAG, error.getMessage());
                                        connectivity.unregisterListener(connectivityListener);
                                        pollHandler.removeCallbacksAndMessages(POLL_DEVICE_CONNECT_TO_AP);
                                        errorListener.onErrorResponse(error);
                                    } else {
                                        if (originalRequest.isCanceled()) {
                                            AylaLog.i(LOG_TAG, "poll request was cancelled");
                                            connectivity.unregisterListener(connectivityListener);
                                            pollHandler.removeCallbacksAndMessages(POLL_DEVICE_CONNECT_TO_AP);
                                        } else {
                                            pollHandler.postDelayed(_runnable, getConfirmPollInterval());
                                        }
                                    }
                                    updateAndNotifyStatus(status);
                                }
                            }
                        },
                        new ErrorListener() {
                            @Override
                            public void onErrorResponse(AylaError error) {
                                if (error.getErrorType() == AylaError.ErrorType.Timeout
                                      && SystemClock.uptimeMillis() <= targetTimeoutMillis) {
                                    // We expect to get timeout errors on occasion when polling the
                                    // device for wifi status. It gets busy connecting to the AP and will
                                    // ignore us sometimes, so we will just try again later.
                                    AylaLog.i(LOG_TAG, "continue polling for network timeout error");
                                    pollHandler.postDelayed(_runnable, getConfirmPollInterval());
                                } else {
                                    // Pass the error back to the caller
                                    pollHandler.removeCallbacksAndMessages(POLL_DEVICE_CONNECT_TO_AP);
                                    connectivity.unregisterListener(connectivityListener);
                                    errorListener.onErrorResponse(error);
                                }
                            }
                        }
                );

                // Increase the default timeout here- the device is very busy and slow to respond
                _fetchWifiStatusRequest.setRetryPolicy(new DefaultRetryPolicy(
                        DEFAULT_POLL_WIFI_STATUS_TIMEOUT,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                originalRequest.setChainedRequest(_fetchWifiStatusRequest);
            }

            void tryToFetchRegistrationInfo(AylaWifiStatus status) {
                AylaSetupDevice setupDevice = getSetupDevice();
                boolean supportRegToken = setupDevice.hasFeature(AylaSetupDevice.FEATURE_REG_TOKEN);
                boolean deviceIsUp = DeviceWifiState.UP.equals(status.getState());
                if (supportRegToken && deviceIsUp) {
                    AylaLog.d(LOG_TAG, "device wifi state is UP, fetching reg info");
                    AylaAPIRequest request = fetchRegInfo(
                            new Listener<AylaRegInfo>() {
                                @Override
                                public void onResponse(AylaRegInfo info) {
                                    AylaLog.d(LOG_TAG, "Reg info fetched " + info);
                                    _fetchedRegInfo = info.getRegtoken() != null;
                                    setupDevice.setRegToken(info.getRegtoken());
                                    setupDevice.setRegistrationType(AylaDevice.RegistrationType
                                            .fromString(info.getRegistrationType()));
                                }
                            },
                            new ErrorListener() {
                                @Override
                                public void onErrorResponse(AylaError error) {
                                    AylaLog.d(LOG_TAG, "Error in fetching reg info:" + error);
                                    _fetchedRegInfo = false;
                                }
                            });
                    originalRequest.setChainedRequest(request);
                } else {
                    String skipReason = deviceIsUp ? "device is not up"
                            : "doesn't support reg-type feature";
                    AylaLog.i(LOG_TAG, "skip fetch reg info as " + skipReason);
                }
            }
        };

        pollHandler.post(pollWifiStatusRunnable);
        pollHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                String msg = "polling wifi status timed out";
                AylaLog.e(LOG_TAG, msg);
                originalRequest.cancel();
                pollHandler.removeCallbacksAndMessages(POLL_DEVICE_CONNECT_TO_AP);
                connectivity.unregisterListener(connectivityListener);
                errorListener.onErrorResponse(new TimeoutError(msg));
            }
        }, POLL_DEVICE_CONNECT_TO_AP, targetTimeoutMillis);
    }

    /**
     * Returns the poll interval used when polling the service to determine if a device has
     * connected to the service
     *
     * @return the poll interval in ms
     */
    public int getConfirmPollInterval() {
        return _confirmPollInterval;
    }

    /**
     * Sets the interval used to poll the device service to determine if the device has
     * successfully connected to the service after joining the WiFi network.
     *
     * @param timeInMs Time in milliseconds between requests to look for the device
     */
    public void setConfirmPollInterval(int timeInMs) {
        _confirmPollInterval = timeInMs;
    }

    /**
     * Confirms that the setup device has connected to the Ayla service. The mobile device needs
     * to be able to reach the Device service in order for this call to succeed. Make sure the
     * mobile device has called {@link #connectToNetwork(String, int, Listener, ErrorListener)}
     * before making this call to ensure we are not connected to the module's access point.
     *
     * @param timeoutInSeconds Timeout for this operation.
     * @param dsn              DSN of the device to confirm
     * @param setupToken       Setup token passed to the device in
     *                         {@link #connectDeviceToService}
     * @param successListener  Listener called when the device has been confirmed to connect to
     *                         the service. The listener will be called with an AylaSetupDevice
     *                         containing at least the LAN IP address, device type, registration
     *                         type and time the device connected.
     * @param errorListener    Listener called if an error occurs or the operation times out
     * @return the AylaAPIRequest for this request
     */
    @Nullable
    public AylaAPIRequest confirmDeviceConnected(int timeoutInSeconds,
                                                 String dsn,
                                                 String setupToken,
                                                 final Listener<AylaSetupDevice> successListener,
                                                 final ErrorListener errorListener) {
        try {
            Preconditions.checkArgument(dsn != null,
                    "DSN is required");
            Preconditions.checkState(getSessionManager() != null,
                    "SessionManager is null");
        } catch (IllegalArgumentException | IllegalStateException e) {
            errorListener.onErrorResponse(new PreconditionError(e.getMessage()));
            return null;
        }

        if (_fetchedRegInfo) {
            AylaLog.d(LOG_TAG, "already fetched reg info from the device.");
            successListener.onResponse(getSetupDevice());
            return null;
        }

        final String CONFIRM_DEVICE_CONNECTED = "confirmDeviceConnected";

        long startTime = System.currentTimeMillis();
        long timeoutInMills = timeoutInSeconds * 1000;

        List<AylaAPIRequest> compoundRequests = new ArrayList<>();

        Map<String, String> params = new HashMap<>();
        params.put("dsn", dsn);
        if (setupToken != null) {
            params.put("setup_token", setupToken);
        }
        String baseUrl = getSessionManager().getDeviceManager().deviceServiceUrl(
                "apiv1/devices/connected.json");
        String url = URLHelper.appendParameters(baseUrl, params);

        Listener<AylaDevice.Wrapper> internalSuccessListener = new Listener<AylaDevice.Wrapper>() {
            @Override
            public void onResponse(AylaDevice.Wrapper response) {
                AylaSetupDevice setupDevice = getSetupDevice();
                setupDevice.updateFrom(response.device, AylaDevice.DataSource.CLOUD);
                successListener.onResponse(setupDevice);

                _setupFinishTime = System.currentTimeMillis();
                long totalSetupTime = _setupFinishTime - _setupStartTime;
                AylaLog.d(LOG_TAG, "Setup completed. totalSetupTime: " + totalSetupTime);

                if (shouldSendMetrics()) {
                    AylaSetupMetric setupMetric = new AylaSetupMetric(
                            AylaMetric.LogLevel.INFO,
                            AylaSetupMetric.MetricType.SETUP_SUCCESS,
                            CONFIRM_DEVICE_CONNECTED,
                            getSetupSessionId(),
                            AylaMetric.Result.SUCCESS, null);
                    setupMetric.secureSetup(isSecureSetup());
                    setupMetric.setRequestTotalTime(totalSetupTime);
                    sendToMetricsManager(setupMetric);
                }
            }
        };

        ErrorListener internalErrorListener = new ErrorListener() {
            @Override
            public void onErrorResponse(AylaError error) {
                if (System.currentTimeMillis() - startTime > timeoutInMills) {
                    errorListener.onErrorResponse(error);
                    if (shouldSendMetrics()) {
                        AylaSetupMetric setupMetric = new AylaSetupMetric(
                                AylaMetric.LogLevel.INFO,
                                AylaSetupMetric.MetricType.SETUP_FAILURE,
                                CONFIRM_DEVICE_CONNECTED,
                                getSetupSessionId(),
                                AylaMetric.Result.FAILURE, error.getMessage());
                        setupMetric.secureSetup(isSecureSetup());
                        sendToMetricsManager(setupMetric);
                    }
                } else {
                    AylaLog.d(LOG_TAG, "try again to confirm device connection");
                    AylaAPIRequest<AylaDevice.Wrapper> request = new AylaAPIRequest<>(
                            Request.Method.GET,
                            url,
                            null,
                            AylaDevice.Wrapper.class,
                            getSessionManager(),
                            internalSuccessListener,this);
                    compoundRequests.add(request);

                    getUiHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
                        }
                    }, getConfirmPollInterval());
                }
            }
        };

        AylaAPIRequest<AylaDevice.Wrapper> request = new AylaAPIRequest<AylaDevice.Wrapper>(
                Request.Method.GET,
                url,
                null,
                AylaDevice.Wrapper.class,
                getSessionManager(),
                internalSuccessListener, internalErrorListener) {
                    @Override
                    public void cancel() {
                        super.cancel();
                        for (AylaAPIRequest req : compoundRequests) {
                            req.cancel();
                        }
                    }
        };

        getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        return request;
    }

    /**
     * Connects the mobile device to the given WiFi network.
     *
     * @param ssidName         WiFi Network name to connect.
     * @param timeoutInSeconds Maximum time to spend trying to reconnect. A TimeoutError will be
     *                         delivered to the ErrorListener if this time is exceeded before we
     *                         are able to join the network.
     * @param successListener  Listener called when we have successfully joined the network.
     * @param errorListener    Listener called if we fail to join the network in the specified time
     *                         period.
     * @return the AylaAPIRequest that may be canceled to stop this operation
     *
     * @deprecated For apps that targets to Android 10 and above, should instead use the appropriate
     * <code>connect</code> method in {@link AylaConnectivityManager#connect(String, String, int)}
     * to connect to a new network, as both {@link WifiManager#enableNetwork(int, boolean)}
     * and {@link WifiManager#getConfiguredNetworks()} this methods relies on have been deprecated
     * on Android 10 and above.
     */
    @Nullable
    public AylaAPIRequest connectToNetwork(final String ssidName,
                                           int   timeoutInSeconds,
                                           final Listener<EmptyResponse> successListener,
                                           final ErrorListener errorListener) {
        // Are we already on the right network?
        WifiManager wifiManager = getAylaConnectivityManager().getWifiManager();

        // Make sure we have permission to do this first
        Context context = AylaNetworks.sharedInstance().getContext();
        AylaError permissionError = PermissionUtils.checkPermissions(context,
                SETUP_REQUIRED_PERMISSIONS);
        if (permissionError != null) {
            errorListener.onErrorResponse(permissionError);
            AylaSetupMetric setupMetric = new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                    AylaSetupMetric.MetricType.SETUP_FAILURE, "reconnectToOriginalNetwork",
                    getSetupSessionId(), AylaMetric.Result.FAILURE, "Missing permissions.");
            setupMetric.secureSetup(isSecureSetup());
            sendToMetricsManager(setupMetric);
            return null;
        }

        final String unquotedNetworkSSID = ObjectUtils.unquote(ssidName);
        int netId;
        try {
            netId = getNetworkIdBySSIDName(wifiManager, unquotedNetworkSSID);
        } catch (SecurityException e) {
            errorListener.onErrorResponse(new AppPermissionError("MissingPermission"));
            return null;
        }

        if (netId == AylaConnectivityManager.UNKNOWN_NET_ID) {
            // Couldn't find the original network
            errorListener.onErrorResponse(new PreconditionError("Unable to find original network " +
                    "with SSID " + ssidName));
            AylaSetupMetric setupMetric = new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                    AylaSetupMetric.MetricType.SETUP_FAILURE, "reconnectToOriginalNetwork",
                    getSetupSessionId(), AylaMetric.Result.FAILURE, "Unable to find original network.");
            setupMetric.secureSetup(isSecureSetup());
            sendToMetricsManager(setupMetric);
            return null;
        }

        // Re-join the network
        AylaLog.d(LOG_TAG, "enableNetwork...");
        wifiManager.enableNetwork(netId, true);

        // Now monitor the network changes and find out when we're joined to the original network
        final Handler timeoutHandler = new Handler(Looper.getMainLooper());
        final AylaConnectivity connectivity = AylaNetworks.sharedInstance().getConnectivity();
        if (connectivity == null) {
            errorListener.onErrorResponse(new InternalError("Connectivity unavailable. Are we " +
                    "shutting down?"));
            return null;
        }

        // Create a listener to monitor the network configuration changes
        final AylaConnectivity.AylaConnectivityListener connectivityListener =
                new AylaConnectivity.AylaConnectivityListener() {
                    @Override
                    public void connectivityChanged(boolean wifiEnabled, boolean cellularEnabled) {
                        if (wifiEnabled) {
                            WifiInfo info = wifiManager.getConnectionInfo();
                            if (info != null && _currentNetworkInfo != null) {
                                String unquotedSSID = ObjectUtils.unquote(info.getSSID());
                                if (unquotedSSID != null && unquotedSSID.equals(unquotedNetworkSSID)) {
                                    // We're all set.
                                    timeoutHandler.removeCallbacksAndMessages(null);
                                    connectivity.unregisterListener(this);
                                    _currentNetworkInfo = null;
                                    AylaLog.d(LOG_TAG, "Connected to " + info.getSSID());

                                    // bindToNetwork is a blocking call- let's run that in the
                                    // background as this listener is on the main thread.
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final AylaError error = /*bindToNetwork()*/ null;
                                            if(error == null){
                                                new Handler(Looper.
                                                        getMainLooper()).
                                                        post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                successListener.
                                                                        onResponse(new
                                                                                EmptyResponse());
                                                                AylaSetupMetric setupMetric =
                                                                        new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                                                                                AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                                                                "reconnectToOriginalNetwork",
                                                                                getSetupSessionId(),
                                                                                AylaMetric.Result.PARTIAL_SUCCESS,
                                                                                null);
                                                                setupMetric.secureSetup(isSecureSetup());
                                                                sendToMetricsManager(setupMetric);
                                                            }
                                                        });
                                            } else{
                                                new Handler(Looper.getMainLooper()).
                                                        post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                errorListener.onErrorResponse(error);
                                                                AylaSetupMetric setupMetric =
                                                                        new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                                                                                AylaSetupMetric.MetricType.SETUP_FAILURE,
                                                                                "reconnectToOriginalNetwork",
                                                                                getSetupSessionId(),
                                                                                AylaMetric.Result.FAILURE,
                                                                                error.getMessage());
                                                                setupMetric.secureSetup(isSecureSetup());
                                                                sendToMetricsManager(setupMetric);
                                                            }
                                                        });
                                            }

                                        }
                                    }).start();
                                } else {
                                    AylaLog.d(LOG_TAG, "Connected to " + info.getSSID() +
                                            ", want to connect to " +
                                            ssidName);
                                }
                            } else {
                                AylaLog.d(LOG_TAG, "no connectionInfo");
                            }
                        }
                    }
                };

        // Set a timer to time out if we can't join the network
        timeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectivity.unregisterListener(connectivityListener);
                String result = "(no current network)";
                if (_currentNetworkInfo != null) {
                    result = ssidName;
                }

                errorListener.onErrorResponse(new TimeoutError("Timed out waiting to re-join " +
                        result));
                AylaSetupMetric setupMetric = new AylaSetupMetric(AylaMetric.LogLevel.INFO,
                        AylaSetupMetric.MetricType.SETUP_FAILURE, "reconnectToOriginalNetwork",
                        getSetupSessionId(), AylaMetric.Result.FAILURE,
                        "Timed out waiting to reconnect to network");
                setupMetric.secureSetup(isSecureSetup());
                sendToMetricsManager(setupMetric);
            }
        }, timeoutInSeconds * 1000);

        // Listen for the network state change
        connectivity.registerListener(connectivityListener);

        // Return an AylaAPIRequest that can be canceled
        return new AylaAPIRequest<EmptyResponse>(Request.Method.GET, null, null,
                EmptyResponse.class, null, successListener, errorListener) {
            @Override
            public void cancel() {
                super.cancel();
                connectivity.unregisterListener(connectivityListener);
                timeoutHandler.removeCallbacksAndMessages(null);
            }
        };
    }

    /**
     * Connects the mobile device to the WiFi network the device was trying to join in
     * via a call to {@link #connectDeviceToService(String, String, String, Double, Double,
     * int, Listener, ErrorListener)}.
     *
     * @param timeoutInSeconds Maximum time to spend trying to reconnect. A TimeoutError will be
     *                         delivered to the ErrorListener if this time is exceeded before we
     *                         are able to join the network.
     * @param successListener  Listener called when we have successfully re-joined the network
     * @param errorListener    Listener called if we fail to join the network in the specified time
     *                         period.
     * @return the AylaAPIRequest that may be canceled to stop this operation.
     *
     */
    @NonNull
    public AylaAPIRequest reconnectToOriginalNetwork(int timeoutInSeconds,
                                                     final Listener<EmptyResponse> successListener,
                                                     final ErrorListener errorListener) {

        AylaAPIRequest dummyRequest = new AylaAPIRequest<EmptyResponse>(Request.Method.GET, "",
                null, EmptyResponse.class, _sessionManager,
                successListener, errorListener) {};

        if (_targetDeviceSSIDName == null) {
            // don't have the enough info to connect to the target network,
            // just ignore the request.
            return  dummyRequest;
        }

        AylaConnectivityManager connectivityManager = getAylaConnectivityManager();
        AylaConnectivityManager.OnConnectResultCallback connectivityChangedCallback =
                new AylaConnectivityManager.OnConnectResultCallback() {
                    @Override
                    public void onAvailable(@NonNull String ssid) {
                        AylaLog.d(LOG_TAG, "reconnected to " + ssid);
                        connectivityManager.unregisterConnectResultCallback(this);
                        successListener.onResponse(new EmptyResponse());
                    }

                    @Override
                    public void onUnavailable(@Nullable AylaError error) {
                        AylaLog.d(LOG_TAG, "unable to reconnect to network "
                                + _targetDeviceSSIDName + ", due to:" + error);
                        connectivityManager.unregisterConnectResultCallback(this);
                        errorListener.onErrorResponse(error);
                    }
                };

        connectivityManager.registerConnectResultCallback(connectivityChangedCallback);
        connectivityManager.connect(_targetDeviceSSIDName, _targetDeviceSSIDPassword, timeoutInSeconds);

        return dummyRequest;
    }

    /**
     * disconnectAPMode method is used to Shut down AP Mode on the module. This method is typically
     * not used by the client apps as the AP Mode will be shut down automatically in 30 seconds
     * when the device is connected to service. Method only works when android device is
     * connected to module. On success, an HTTP status of <code>"204 No Content"</code> is returned.
     * Otherwise, an HTTP status of <code>"403 Forbidden"</code> is returned. AP mode disconnect command is
     * only accepted when module is still in AP mode, and successfully connected to
     * service in STA mode (i.e STA mode and AP mode are active at the same time). This command
     * turns off the AP mode, leaving STA mode active only.
     *
     * @param successListener Listener called When the disconnect AP Mode is success
     * @param errorListener   Listener called in case of errors
     * @return the AylaAPIRequest for this request
     */
    @Nullable
    public AylaAPIRequest disconnectAPMode(final Listener<EmptyResponse> successListener,
                                           final ErrorListener errorListener) {
        if (!getSetupDevice().hasFeature(AylaSetupDevice.FEATURE_AP_STA)) {
            AylaError error = new PreconditionError("Device does not support AP/STA Feature");
            getLogService().addLog(LOG_TAG, "Error", error.getMessage());
            if (errorListener != null) {
                errorListener.onErrorResponse(error);
            }
            return null;
        }

        final String DISCONNECT_AP_MODE = "disconnectAPMode";

        if (isSecureSetup()) {
            AylaLanModule lanModule = getSetupDevice().getLanModule();
            if (lanModule == null && errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError(LAN_PRECONDITION_ERROR));
                return null;
            }

            AylaLanCommand command = new AylaLanCommand("PUT",
                    "wifi_stop_ap.json", null, "/local_lan/wifi_stop_ap.json");
            AylaLanRequest request = new AylaLanRequest(getSetupDevice(), command, _sessionManager,
                    new Listener<AylaLanRequest.LanResponse>() {
                        @Override
                        public void onResponse(AylaLanRequest.LanResponse response) {
                            successListener.onResponse(new EmptyResponse());
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                    DISCONNECT_AP_MODE, getSetupSessionId(),
                                    AylaMetric.Result.PARTIAL_SUCCESS, null);
                            setupMetric.secureSetup(isSecureSetup());
                            sendToMetricsManager(setupMetric);
                        }
                    }, errorListener);
            lanModule.sendRequest(request);
            return request;
        } else {
            AylaAPIRequest<EmptyResponse> request = new AylaAPIRequest<>(
                    Request.Method.PUT,
                    formatLocalUrl("wifi_stop_ap.json"),
                    null,
                    EmptyResponse.class,
                    null,
                    successListener,
                    errorListener);

            getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
            return request;
        }
    }

    /**
     * Exits the setup process. and clean up any internal state information.
     * DO NOT RE-USE an AylaSetup object after its exitSetup() method is called.
     *
     * @param successListener Listener called when the operation succeeds
     * @param errorListener   Listener called if the operation encountered an error
     * @return an AylaAPIRequest that may be used to cancel this operation, or null if the
     * operation may not be canceled (e.g. we are already on the original WiFi network or the
     * original AP was not saved)
     */
    public AylaAPIRequest exitSetup(Listener<EmptyResponse> successListener,
                                    ErrorListener errorListener) {

        stopSetupDeviceLanSession();

        if (getHttpServer() != null) {
            getHttpServer().setSetupDevice(null);
        }

        getAylaConnectivityManager().disconnect();

        successListener.onResponse(new EmptyResponse());
        getLogService().sendToLogService();

        if (getSessionManager().getDeviceManager() != null) {
            getSessionManager().getDeviceManager().setLanModePermitted(true);
        }

        if (getSessionManager().getDSManager() != null) {
            getSessionManager().getDSManager().onResume();
        }

        if (AylaNetworks.sharedInstance().getMetricsManager() != null) {
            AylaNetworks.sharedInstance().getMetricsManager().onResume();
        }

        return null;
    }

    /**
     * Adds new listener to be notified of device wifi setup state changes.
     *
     * @param listener Listener to be notified
     */
    public void addListener(DeviceWifiStateChangeListener listener){
        synchronized (_wifiStateChangeListeners) {
            _wifiStateChangeListeners.add(listener);
        }
    }

    /**
     * Removes the listener registered to receive device wifi setup state changes.
     *
     * @param listener Listener to be removed.
     */
    public void removeListener(DeviceWifiStateChangeListener listener) {
        synchronized (_wifiStateChangeListeners) {
            _wifiStateChangeListeners.remove(listener);
        }
    }

    /**
     * Returns the last wifi status received from the setup device.
     * This status object is typically returned to the listeners via calls
     * to {@link #connectDeviceToService}, but may be retrieved at any
     * time from this method. If the wifi status has not been fetched
     * since the last call to {@link #connectDeviceToService}, this method
     * will return null.
     */
    @Nullable
    public AylaWifiStatus getLastWifiStatus() {
        return _lastWifiStatus;
    }

    /**
     * Returns the original Wi-Fi network info before the setup starts
     * as so to be able to re-join on exit setup.
     */
    public WifiInfo getCurrentNetworkInfo() {
        return _currentNetworkInfo;
    }

    /**
     * If the IP address of the device access point is not the default 192.168.0.1, this method
     * must be called to set the correct IP address before attempting to connect to the device.
     *
     * @param setupDeviceIp the IP address of the access point of the device
     */
    public void setSetupDeviceIp(String setupDeviceIp) {
        _setupDeviceIp = (setupDeviceIp == null) ? SETUP_DEVICE_IP : setupDeviceIp;
    }

    /**
     * Sets the time on the setup device. This is called internally after we
     * receive the device information (status.json) as part of the
     * {@link #fetchDeviceDetailLan(AylaAPIRequest, Listener, ErrorListener)} method.
     *
     * @param deviceTime Time to set on the device, or null to set the current time
     * @param successListener Listener called upon success
     * @param errorListener Listener called if an error occurs
     * @return The AylaAPIRequest for this request
     */
    @Nullable
    private AylaAPIRequest setDeviceTime(Date deviceTime,
                                         Response.Listener<EmptyResponse> successListener,
                                         ErrorListener errorListener) {
        if (deviceTime == null) {
            deviceTime = new Date();
        }

        JSONObject time = new JSONObject();
        try {
            //Send the time in seconds.
            time.put("time", (deviceTime.getTime()) / 1000);
        } catch (JSONException e) {
            AylaError error = new PreconditionError(e.getMessage());
            AylaLog.e(LOG_TAG, error.getMessage());
            if (errorListener != null) {
                errorListener.onErrorResponse(error);
            }
            return null;
        }

        String url = formatLocalUrl("time.json");
        AylaAPIRequest<EmptyResponse> request = new AylaJsonRequest<>(Request.Method.PUT,
                url, time.toString(), null, EmptyResponse.class, getSessionManager(),
                successListener, errorListener);
        getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);

        return request;
    }

    /**
     * After connecting to the device's access point, this method is called to fetch information
     * about the device. This method is the second part of a compound API call initiated by
     * {@link #connectToNewDevice}.
     *
     * In addition to retrieving the device information, this method also sets the time on the
     * device to the time on the mobile device.
     *
     * @param originalRequest Original AylaAPIRequest (the one from connectToNewDevice)
     * @param successListener Listener to receive the AylaSetupDevice on success
     * @param errorListener   Listener to receive an AylaError if an error occurred
     * @return the AylaAPIRequest for this request
     */
    private AylaAPIRequest fetchDeviceDetailLan(final AylaAPIRequest originalRequest,
                                                final Listener<AylaSetupDevice> successListener,
                                                final ErrorListener errorListener) {

        AylaLog.d(LOG_TAG, "fetchDeviceDetailLan start");
        String url = "http://" + getSetupDeviceIp() + "/status.json";
        AylaAPIRequest<AylaSetupDevice> request = new AylaAPIRequest<AylaSetupDevice>(
                Request.Method.GET,
                url,
                null,
                AylaSetupDevice.class,
                null,
                successListener,
                errorListener) {
            // Override parseNetworkResponse instead of deliverResponse so we do our key
            // generation on a background thread instead of on the UI thread.
            // It can take some time, especially on older / slower devices.
            @Override
            protected Response<AylaSetupDevice>
            parseNetworkResponse(NetworkResponse networkResponse) {
                // Get the actual response from the superclass
                Response<AylaSetupDevice> response = super.parseNetworkResponse(networkResponse);
                if (!response.isSuccess()) {
                    AylaLog.e(LOG_TAG, "Error fetching device detail: " + response.error);
                    return response;
                }

                // Save and configure the new device before we hand it back to the caller
                AylaSetupDevice setupDevice = getSetupDevice();
                setupDevice.updateFrom(response.result, AylaDevice.DataSource.LAN);

                // Now we have the new device's DSN. Set it in AylaLogService
                getLogService().setDsn(setupDevice.getDsn());

                AylaLog.d(LOG_TAG, "fetchDeviceDetail success. Starting LAN mode ");
                startSetupDeviceLanSession();

                // Set the device time to the current time. We won't fail if
                // this call fails, so we can ignore the results.
                EmptyListener<EmptyResponse> emptyListener = new EmptyListener<>();
                setDeviceTime(null, emptyListener, emptyListener);

                return response;
            }

            @Override
            protected VolleyError parseNetworkError(VolleyError error) {
                if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                    AylaLog.d(LOG_TAG, "fetchDeviceDetailLan returned " +
                            "404, Starting LAN mode key exchange");
                    startSetupDeviceLanSession();
                }
                return super.parseNetworkError(error);
            }
        };

        // This is a compound request- we need to keep the chain going so canceling the original
        // request will cancel this new request.
        if (originalRequest != null) {
            if (originalRequest.isCanceled()) {
                request.cancel();
            } else {
                originalRequest.setChainedRequest(request);
                getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
            }
        } else {
            getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        }

        return request;
    }

    /**
     * Internal method to create a URL string for the setup device in AP mode
     *
     * @param url URL path
     * @return a formatted URL string pointing to the setup device
     */
    private String formatLocalUrl(String url) {
        return "http://" + getSetupDeviceIp() + "/" + url;
    }

    /**
     * Internal class used to receive broadcast messages related to the WiFi
     * scan for access points.
     */
    private class ScanResultsReceiver extends BroadcastReceiver {

        private Listener<ScanResult[]> scanResultsListener;

        public ScanResultsReceiver() {
        }

        public void setScanResultListener(Listener<ScanResult[]> scanResultsListener) {
            this.scanResultsListener = scanResultsListener;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            AylaLog.i(LOG_TAG, "ScanReceiver onReceive: " + intent);
            if (scanResultsListener != null) {
                WifiManager wifiManager = getAylaConnectivityManager().getWifiManager();
                List<ScanResult> results = wifiManager.getScanResults();
                ScanResult[] resultArray = results.toArray(new ScanResult[results.size()]);
                scanResultsListener.onResponse(resultArray);
            } else {
                AylaLog.i(LOG_TAG, "ignore scan results due to empty results listener");
            }
        }
    }

    /**
     * Notifies listeners that the device's wifi setup status has changed.
     * @param status last fetched wifi setup status from the device.
     */
    private void updateAndNotifyStatus(AylaWifiStatus status) {
        if (status == null) {
            AylaLog.d(LOG_TAG, "ignore null status");
            return;
        }

        String newState = status.getState();
        String oldState = (getLastWifiStatus() == null) ?
                DeviceWifiState.UNKNOWN : getLastWifiStatus().getState();
        if (!TextUtils.equals(newState, oldState)) {
            AylaLog.d(LOG_TAG, "device wifi state changed:" + oldState + " -> " + newState);
            getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    synchronized (_wifiStateChangeListeners) {
                        for (DeviceWifiStateChangeListener listener : _wifiStateChangeListeners) {
                            listener.wifiStateChanged(newState);
                        }
                    }
                }
            });
        } else {
            AylaLog.i(LOG_TAG, "skip reporting same state:" + newState);
        }
        _lastWifiStatus = status;
    }

    /**
     * Fetches device details in secure LAN mode.
     */
    private AylaAPIRequest fetchDeviceDetailsLAN(
            @NonNull AylaAPIRequest originalRequest,
            @NonNull Response.Listener<AylaSetupDevice> successListener,
            @NonNull ErrorListener errorListener) {

        AylaLog.d(LOG_TAG, "fetchDeviceDetailsLAN start");

        if (originalRequest.isCanceled()) {
            return null;
        }

        AylaSetupDevice setupDevice = getSetupDevice();
        if (setupDevice.getLanIp() == null) {
            return null;
        }

        final String FETCH_DEVICE_DETAILS = "fetchDeviceDetails";

        AylaLanCommand cmd = new AylaLanCommand("GET", "status.json",
                null, "/local_lan/status.json");
        AylaLanRequest request = new AylaLanRequest(setupDevice, cmd, null,
                new Response.Listener<AylaLanRequest.LanResponse>() {
                    @Override
                    public void onResponse(AylaLanRequest.LanResponse lanResponse) {
                        AylaError error = cmd.getResponseError();
                        if (error != null) {
                            AylaLog.d(LOG_TAG, "fetch device details error " + error.getMessage());
                            errorListener.onErrorResponse(error);
                            return;
                        }

                        // Note that the setup device has been updated from the
                        // response in AylaLanModule.handleGetStatusRequest,
                        // so we can use the update value here.
                        getLogService().setDsn(setupDevice.getDsn());
                        successListener.onResponse(setupDevice);

                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_SUCCESS,
                                    FETCH_DEVICE_DETAILS, getSetupSessionId(),
                                    AylaMetric.Result.PARTIAL_SUCCESS, null);
                            setupMetric.secureSetup(isSecureSetup());
                            sendToMetricsManager(setupMetric);
                        }
                    }
                }, new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        AylaLog.e(LOG_TAG, "Error in fetching device details " + error.getMessage());
                        errorListener.onErrorResponse(error);
                        if (shouldSendMetrics()) {
                            AylaSetupMetric setupMetric = new AylaSetupMetric(
                                    AylaMetric.LogLevel.INFO,
                                    AylaSetupMetric.MetricType.SETUP_FAILURE,
                                    FETCH_DEVICE_DETAILS, getSetupSessionId(),
                                    AylaMetric.Result.PARTIAL_SUCCESS, error.getMessage());
                            setupMetric.setMetricText("Phone connected to device. fetchDeviceDetailsLAN failed ");
                            sendToMetricsManager(setupMetric);
                        }
                    }
        });

        if (originalRequest != null) {
            if (originalRequest.isCanceled()) {
                request.cancel();
                return originalRequest;
            } else {
                originalRequest.setChainedRequest(request);
            }
        }

        if (setupDevice.getLanModule() != null) {
            setupDevice.getLanModule().sendRequest(request);
        } else {
            errorListener.onErrorResponse(new PreconditionError(LAN_PRECONDITION_ERROR));
        }

        return originalRequest;
    }

    private static void sendToMetricsManager(AylaMetric logMessage){
        AylaMetricsManager metricsManager = AylaNetworks.sharedInstance().getMetricsManager();
        if (metricsManager != null) {
            metricsManager.addMessageToUploadsQueue(logMessage);
        } else {
            AylaLog.d(LOG_TAG, "metricsManager is null, ignore metric message " + logMessage);
        }
    }

    private int getNetworkIdBySSIDName(WifiManager wifiManager,
                                       String unquotedNetworkSSID) throws SecurityException {
        int netId = AylaConnectivityManager.UNKNOWN_NET_ID;
        List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks();
        if (configs != null && unquotedNetworkSSID != null) {
            for (WifiConfiguration config : configs) {
                if (unquotedNetworkSSID.equals(ObjectUtils.unquote(config.SSID))) {
                    netId = config.networkId;
                    break;
                }
            }
        }

        return netId;
    }

    protected @NonNull Context getContext() {
        return _context;
    }

    protected @NonNull AylaSessionManager getSessionManager() {
        return _sessionManager;
    }

    protected AylaConnectivityManager getAylaConnectivityManager() {
        return _connectivityManager;
    }

    protected boolean shouldSendMetrics() {
        return AylaNetworks.sharedInstance().getSystemSettings().disableMetricsService;
    }

    protected @NonNull String getSetupDeviceIp() {
        return _setupDeviceIp;
    }

    protected @NonNull AylaLogService getLogService() {
        return _logService;
    }

    protected @NonNull Handler getUiHandler() {
        return _uiHandler;
    }

    private boolean isDeviceConnectedToAP(String ssid, AylaWifiStatus status) {
        if (status == null) {
            return false;
        }

        boolean connected = TextUtils.equals(ssid, status.getConnectedSsid());
        boolean noError = status.getConnectHistory() != null
                && status.getConnectHistory().length > 0
                && status.getConnectHistory()[0].error == AylaWifiStatus.HistoryItem.Error.NoError;

        return connected && noError;
    }

    private AylaError shouldPollDeviceConnectToAP(@NonNull  AylaWifiStatus status,
                                                  @Nullable AylaWifiStatus lastStatus) {
        AylaWifiStatus.HistoryItem[] history = status.getConnectHistory();
        if ((history != null) && (history.length > 0)  && (lastStatus != null)) {
            AylaWifiStatus.HistoryItem.Error error = history[0].error;
            if (error != AylaWifiStatus.HistoryItem.Error.InProgress) {
                AylaWifiStatus.HistoryItem[] lastHistory = lastStatus.getConnectHistory();
                if (lastHistory != null && lastHistory.length > 0) {
                    if (lastHistory[0].mtime != history[0].mtime) {
                        String errorMsg = "stop polling as mtime changed but connect " +
                                "error is " + error.name() + "instead of InProgress";
                        return new InternalError(errorMsg);
                    }
                }
                return new InternalError(error.name());
            }
        }
        return null;
    }

    private boolean isSecureSetup() {
        return _isSecureSetup;
    }

    private @NonNull AylaSetupDevice getSetupDevice() {
        if (_setupDevice == null) {
            _setupDevice = new AylaSetupDevice();
            AylaLog.d(LOG_TAG, "created new setup device");
        }
        return _setupDevice;
    }

    private void startSetupDeviceLanSession() {
        AylaSetupDevice setupDevice = getSetupDevice();
        AylaHttpServer httpServer = getHttpServer();
        setupDevice.setLanIp(getSetupDeviceIp());

        // Generate the RSA key pair for secure setup. This can be processor-intensive,
        // which is why we are doing this in parseNetworkResponse which is executed on
        // the networking thread.
        AylaSetupCrypto setupCrypto = new AylaSetupCrypto();
        setupCrypto.generateKeyPair();
        AylaLanConfig lanConfig = new AylaLanConfig(setupCrypto);
        setupDevice.setLanConfig(lanConfig);

        // Let the HTTP server know about our setup device and kick off LAN mode
        httpServer.setSetupDevice(setupDevice);
        setupDevice.startLanSession(httpServer);
    }

    private void stopSetupDeviceLanSession() {
        getSetupDevice().stopLanSession();
    }

    private @Nullable String getSetupSessionId() {
        return _setupSessionId;
    }

    private @NonNull AylaHttpServer getHttpServer() {
        return _lanHttpServer;
    }

}
