package com.aylanetworks.aylasdk;

import android.content.Context;
import android.util.Log;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.auth.UsernameAuthProvider;
import com.aylanetworks.aylasdk.change.Change;
import com.aylanetworks.aylasdk.change.ListChange;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.RequestFuture;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.aylanetworks.aylasdk.AylaSystemSettings.*;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Android_AylaSDK
 * <p>
 * Copyright 2016 Ayla Networks, all rights reserved
 */
public class AylaTestConfig {
    private AylaSystemSettings testSystemSettings;
    private int apiTimeOut = 5000;

    /**
     * Create test environment for this test.
     * @param appId
     * @param appSecret
     * @param location
     * @param serviceType
     * @param deviceDetailProvider
     */
    public void setTestSystemSettings(String appId, String appSecret, Context context,
                                      ServiceLocation location,
                                      ServiceType serviceType,
                                      DeviceDetailProvider deviceDetailProvider,
                                      boolean allowDSS, String[]
                                              subscriptionTypes){
        testSystemSettings.context = context;
        testSystemSettings.appId = appId;
        testSystemSettings.appSecret = appSecret;
        testSystemSettings.serviceType = serviceType;
        testSystemSettings.deviceDetailProvider = deviceDetailProvider;
        testSystemSettings.allowDSS = allowDSS;
        testSystemSettings.dssSubscriptionTypes = subscriptionTypes;
    }

    public int getApiTimeOut() {
        return apiTimeOut;
    }

    public void setApiTimeOut(int timeOut){
        apiTimeOut = timeOut;
    }

    public AylaSystemSettings getTestSystemSettings() {
        return testSystemSettings;
    }

    /**
     * Create test environment for this test.
     * @param testSystemSettings AylaSystemSettings object containing appId, appSecret,
     *                           location, serviceType and devicedetailProvider.
     */
    public void setTestSystemSettings(AylaSystemSettings testSystemSettings) {
        this.testSystemSettings = testSystemSettings;
    }

    /**
     * Helper method for test cases to sign in and optionally wait for the device manager
     * initialization to complete.
     *
     * @return The AylaAuthorization object resulting from sign-in, or null if a problem occurred
     */
    public AylaAuthorization signIn(AylaTestAccountConfig testAccount, Context context) {
        // Initialize the library

        assertNotNull(context);
        testSystemSettings.context = context;
        AylaNetworks.initialize(testSystemSettings);
        AylaNetworks cm = AylaNetworks.sharedInstance();
        assertNotNull(cm);

        // Use a RequestFuture to make our call to sign in synchronous
        RequestFuture<AylaAuthorization> future = RequestFuture.newFuture();
        AylaLoginManager loginManager = AylaNetworks.sharedInstance().getLoginManager();
        UsernameAuthProvider authProvider = new UsernameAuthProvider(testAccount.getUserEmail(),
                testAccount.getPassword());
        loginManager.signIn(authProvider, testAccount.getTestSessionName(), future, future);
        AylaAuthorization auth = null;

        try {
            auth = future.get(apiTimeOut, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }

        // Make sure we have a user and an access token and a refresh token.
        assertNotNull("No authorization returned", auth);
        assertNotNull("Authorization has no access token",
                auth.getAccessToken());
        assertNotNull("Authorization has no refresh token",
                auth.getRefreshToken());

        return auth;
    }

    public boolean waitForDeviceManagerInitComplete(String sessionName) {
        // Make sure a DeviceManager and SessionManager exist
        AylaSessionManager sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(sessionName);
        assertNotNull("No session manager was created after login",
                sessionManager);

        AylaDeviceManager deviceManager = sessionManager.getDeviceManager();
        assertNotNull("No device manager was created after login",
                deviceManager);


        InitCompleteWaiter listener = new InitCompleteWaiter();
        deviceManager.addListener(listener);

        // Wait for the init complete message to come in
        //noinspection ThrowableResultOfMethodCallIgnored
        return listener.waitForInitComplete();
    }

    public boolean waitForDSManagerInitComplete(String sessionName) {
        // Make sure a DeviceManager and SessionManager exist
        AylaSessionManager sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(sessionName);
        assertNotNull("No session manager was created after login",
                sessionManager);

        AylaDeviceManager deviceManager = sessionManager.getDeviceManager();
        assertNotNull("No device manager was created after login",
                deviceManager);

        AylaDSManager dsManager = sessionManager.getDSManager();
        InitDSSWaiter listener = new InitDSSWaiter();
        dsManager.addListener(listener);

        // Wait for the init complete message to come in
        //noinspection ThrowableResultOfMethodCallIgnored
        return listener.waitForConnectionChange();
    }

    public static boolean waitForTestDeviceLANMode(int timeout, String dsn, String sessionName) {
        AylaDeviceManager dm = AylaNetworks.sharedInstance().getSessionManager(sessionName)
                .getDeviceManager();
        AylaDevice testDevice = dm.deviceWithDSN(dsn);
        return new LanModeWaiter(testDevice).waitForLanMode(timeout);
    }

    /**
     * Waits for the DeviceManagerInitComplete notification
     */
    public static class InitCompleteWaiter implements AylaDeviceManager.DeviceManagerListener {

        private static final String TAG = "InitCompleteWaiter";

        private Exception _error;
        private final Object _initCompleteSemaphore = new Object();

        public boolean waitForInitComplete() {
            try {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (_initCompleteSemaphore) {
                    // Give this a longer time than normal - it could take a while
                    _initCompleteSemaphore.wait(TestConstants.API_TIMEOUT_MS * 3);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Failed to get deviceListChanged call from DeviceManager");
                _error = e;
            }

            return _error == null;
        }

        @Override
        public void deviceManagerInitComplete(Map<String, AylaError> failedDevices) {
            if (failedDevices.size() > 0) {
                Log.e(TAG, "deviceManagerInitComplete called with errors: "
                        + failedDevices
                        .toString());
                _error = failedDevices.values().iterator().next();
            }
            synchronized (_initCompleteSemaphore) {
                _initCompleteSemaphore.notify();
            }
        }

        @Override
        public void deviceManagerInitFailure(AylaError error,
                                             AylaDeviceManager.DeviceManagerState
                                                     failedState) {
            Log.e(TAG, "deviceManagerInitFailure called. Error: " + error +
                    ", failedState: " + failedState);
            _error = error;
            synchronized (_initCompleteSemaphore) {
                _initCompleteSemaphore.notify();
            }
        }

        @Override
        public void deviceListChanged(ListChange change) {
        }

        @Override
        public void deviceManagerError(AylaError error) {
            Log.e(TAG, "Device manager encountered an error: " + error);
            _error = error;
        }

        @Override
        public void deviceManagerStateChanged(AylaDeviceManager.DeviceManagerState oldState,
                                              AylaDeviceManager.DeviceManagerState newState) {
            AylaLog.d("DeviceManagerTest", "State changed: " + oldState.toString() + " --> " +
                    newState.toString());
        }
    }

    public static class InitDSSWaiter implements AylaDSManager.DSManagerListener{

        private final Object _initCompleteSemaphore = new Object();

        public boolean waitForConnectionChange() {
            try {
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (_initCompleteSemaphore) {
                    // Give this a longer time than normal - it could take a while
                    _initCompleteSemaphore.wait(TestConstants.API_TIMEOUT_MS * 3);
                }
            } catch (InterruptedException e) {
                fail("Failed to get dss start call from " +
                        "sessionManager");
                return false;
            }
            return true;
        }

        @Override
        public void dsManagerConnectionChanged(boolean isConnected) {
            synchronized (_initCompleteSemaphore) {
                _initCompleteSemaphore.notify();
            }
        }
    }

    public static class LanModeWaiter implements AylaDevice.DeviceChangeListener {
        private AylaDevice _device;
        final Object semaphore = new Object();

        public LanModeWaiter(AylaDevice device) {
            _device = device;
            _device.addListener(this);
        }

        public boolean waitForLanMode(int timeout) {
            boolean ret = false;
            synchronized (semaphore) {
                try {
                    semaphore.wait(timeout);
                    ret = true;
                } catch (InterruptedException e) {
                    ret = false;
                }
            }

            _device.removeListener(this);
            return ret;
        }

        @Override
        public void deviceChanged(AylaDevice device, Change change) {

        }

        @Override
        public void deviceError(AylaDevice device, AylaError error) {

        }

        @Override
        public void deviceLanStateChanged(AylaDevice device, boolean lanModeEnabled, AylaError error) {
            if ( device.getDsn().equals(_device.getDsn()) && lanModeEnabled ) {
                synchronized (semaphore) {
                    semaphore.notify();
                }
            }
        }
    }

}
