package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LanModeTest {
    private final static String LOG_TAG = "HTTPDTest";
    public static final int LAN_TIMEOUT = 10000;
    private AylaDeviceManager _deviceManager;
    private AylaDevice _device;

    @Before
    public void setUp() {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        _deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        _device = _deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        _device.setLanModePermitted(true);
        TestConstants.waitForTestDeviceLANMode(LAN_TIMEOUT);
    }

    @Test
    public void testLanMode() {
        String[] propNames = new String[]{"Blue_LED", "Green_LED", "Blue_button"};

        int timeout = 10000;
        // Toggle the blue LED 5 times
        for ( int i = 0; i < 5; i++ ) {
            try {
                AylaLog.d(LOG_TAG, "calling fetchProperties");
                assertNotNull("Device with DSN " + TestConstants.TEST_DEVICE_DSN + " not found on" +
                        " account", _device);
                RequestFuture<AylaProperty[]> future = RequestFuture.newFuture();

                // Make sure we're still in LAN mode
                assertTrue(_device.isLanModeActive());
                _device.fetchProperties(propNames, future, future);
                AylaProperty[] fetchedProperties = future.get(timeout, TimeUnit.MILLISECONDS);

                // Set the datapoint to the opposite value
                AylaProperty<Integer> prop = _device.getProperty("Blue_LED");
                int currentValue = prop.getValue();
                int newValue = 0;
                if ( currentValue == 0) {
                    newValue = 1;
                }

                AylaLog.d(LOG_TAG, "Calling createDatapoint");
                RequestFuture<AylaDatapoint<Integer>> createFuture = RequestFuture.newFuture();
                prop.createDatapoint(newValue, null, createFuture, createFuture);
                AylaDatapoint dp = createFuture.get(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                AylaLog.e(LOG_TAG, "Interrupted error");
                e.printStackTrace();
            } catch (ExecutionException e) {
                String cause = e.getCause() == null ? e.getMessage(): e.getCause().getMessage();
                AylaLog.e(LOG_TAG, "Execution error for cause:" + cause);
                e.printStackTrace();
                fail("execution error");
            } catch (TimeoutException e) {
                AylaLog.e(LOG_TAG, "Timeout error");
                fail("timed out");
                e.printStackTrace();
            }
        }
    }

}
