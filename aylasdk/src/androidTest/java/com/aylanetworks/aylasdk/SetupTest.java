package com.aylanetworks.aylasdk;
/*
 * Android_AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.setup.AylaSetup;
import com.aylanetworks.aylasdk.setup.AylaSetupDevice;
import com.aylanetworks.aylasdk.setup.AylaWifiScanResults;
import com.aylanetworks.aylasdk.setup.AylaWifiStatus;
import com.aylanetworks.aylasdk.util.ObjectUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class SetupTest {
    private final static String LOG_TAG = "Setup";

    @Before
    public void setUp() throws Exception {
        // Requires permissions needed for running the test.
        String packageName = InstrumentationRegistry.getTargetContext()
                .getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] requiredPermissions = AylaSetup.SETUP_REQUIRED_PERMISSIONS;
            for (String permission : requiredPermissions) {
                String shellCmd = String.format("pm grant %s %s", packageName, permission);
                getInstrumentation().getUiAutomation().executeShellCommand(shellCmd);
            }
        }
    }

    @Test
    public void testConnectReconnect() {
        Context context = InstrumentationRegistry.getContext();
        TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS.context = context;
        AylaNetworks.initialize(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);

        // Find out our original network SSID
        WifiManager wifiManager = (WifiManager)context.getApplicationContext().getSystemService
                (Context.WIFI_SERVICE);
        String originalSsid = wifiManager.getConnectionInfo().getSSID();
        String deviceSsid =TestConstants.TEST_DEVICE_SSID;

        assertNotSame("Already on device's AP!", originalSsid, deviceSsid);
        AylaSetup setup = null;
        try {
            setup = new AylaSetup(context, null);
        } catch (AylaError aylaError) {
            fail("Could not create setup: " + aylaError);
        }

        // Scan for APs, make sure we can find the one we want
        RequestFuture<ScanResult[]> deviceScan = RequestFuture.newFuture();

        setup.scanForAccessPoints(10, null, deviceScan, deviceScan);
        ScanResult[] scanResults = null;
        try {
            scanResults = deviceScan.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interuppted: " + e);
        } catch (ExecutionException e) {
            fail("Execution: " + e.getCause());
        } catch (TimeoutException e) {
            fail("Timeout");
        }

        assertNotNull(scanResults);

        boolean foundMyAP = false;
        for (ScanResult result : scanResults) {
            if (TextUtils.equals(result.SSID, deviceSsid)) {
                foundMyAP = true;
                break;
            }
        }

        assertTrue("Failed to find my device's AP", foundMyAP);

        RequestFuture<AylaSetupDevice> future = RequestFuture.newFuture();
        setup.connectToNewDevice(deviceSsid, 30, future, future);
        AylaSetupDevice setupDevice = null;
        try {
            setupDevice = future.get(60, TimeUnit.SECONDS);
            AylaLog.d("TestConnect", "Setup device: " + setupDevice);
        } catch (InterruptedException e) {
            fail("Interrupted exception: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception: " + e.toString());
        } catch (TimeoutException e) {
            fail("Timeout exception");
        }

        // We should be connected to the setup device now. Wait for LAN mode
        boolean inLanMode = true;
        if ( !setupDevice.isLanModeActive()) {
            AylaLog.d(LOG_TAG, "Waiting for LAN mode on the setup device...");
            inLanMode = new TestConstants.LanModeWaiter(setupDevice).waitForLanMode(20);
        }

        AylaLog.d(LOG_TAG, "Device is in LAN mode: " + inLanMode);

        AylaLog.d(LOG_TAG, "Starting device scan for APs...");
        RequestFuture<AylaAPIRequest.EmptyResponse> startScanFuture = RequestFuture.newFuture();
        setup.startDeviceScanForAccessPoints(startScanFuture, startScanFuture);
        try {
            startScanFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted");
        } catch (ExecutionException e) {
            fail("Execution: " + e);
        } catch (TimeoutException e) {
            fail("Timeout");
        }

        AylaLog.d(LOG_TAG, "Device is scanning for APs. Wait a few secs...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            fail("Interrupted sleeping");
        }

        AylaLog.d(LOG_TAG, "Requesting scan results...");
        RequestFuture<AylaWifiScanResults> scanFuture = RequestFuture.newFuture();
        setup.fetchDeviceAccessPoints(null, scanFuture, scanFuture);
        AylaWifiScanResults deviceScanResults = null;
        try {
            deviceScanResults = scanFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted");
        } catch (ExecutionException e) {
            fail("Execution: " + e);
        } catch (TimeoutException e) {
            fail("Timeout");
        }

        assertNotNull("No scan results returned", deviceScanResults);

        for ( AylaWifiScanResults.Result result : deviceScanResults.results ) {
            AylaLog.d(LOG_TAG, "Discovered " + result.ssid + "|" + result.type);
        }

        String regToken = ObjectUtils.generateRandomToken(8);

        // Connect the device to our wifi network
        RequestFuture<AylaWifiStatus> connectFuture = RequestFuture.newFuture();
        setup.connectDeviceToService("Frodo", "ILoveLittleDogs!", regToken, null, null,
                60, connectFuture, connectFuture);
        try {
            connectFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted");
        } catch (ExecutionException e) {
            fail("Execution: " + e);
        } catch (TimeoutException e) {
            fail("Timeout");
        }

        // Disconnect AP Mode
        RequestFuture<AylaAPIRequest.EmptyResponse> disconnectAPModeFuture =
                RequestFuture.newFuture();
        setup.disconnectAPMode(disconnectAPModeFuture, disconnectAPModeFuture);

        try {
            disconnectAPModeFuture.get(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception: " + e.toString());
        } catch (TimeoutException e) {
            fail("Timeout exception");
        }

        // Re-join our original network
        RequestFuture<AylaAPIRequest.EmptyResponse> reconnectFuture = RequestFuture.newFuture();
        setup.reconnectToOriginalNetwork(30, reconnectFuture, reconnectFuture);

        try {
            reconnectFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception: " + e.toString());
        } catch (TimeoutException e) {
            fail("Timeout exception");
        }

        String currentSsid = wifiManager.getConnectionInfo().getSSID();
        assertEquals("Did not re-join our original network", originalSsid, currentSsid);

        // Confirm the device connected to the service
        RequestFuture<AylaSetupDevice> serviceFuture = RequestFuture.newFuture();
        setup.confirmDeviceConnected(20, setupDevice.getDsn(), regToken, serviceFuture,
                serviceFuture);
        try {
            serviceFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted: " + e);
        } catch (ExecutionException e) {
            fail("Execution: " + e.getCause());
        } catch (TimeoutException e) {
            fail("Timeout");
        }
    }
}
