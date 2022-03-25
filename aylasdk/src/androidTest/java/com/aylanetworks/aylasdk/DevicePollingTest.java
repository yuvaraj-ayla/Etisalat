package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import android.os.Handler;
import android.os.Looper;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.change.Change;
import com.aylanetworks.aylasdk.change.PropertyChange;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DevicePollingTest {
    private static final String LOG_TAG = "DevicePollingTest";
    final DeviceListener _listener = new DeviceListener();

    @Before
    public void setUp() {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        try {
            AylaLog.initAylaLog(TestConstants.TEST_SESSION_NAME, "ayla_logs_test",
                    AylaLog.LogLevel.Verbose, AylaLog.LogLevel.Verbose);
        } catch (PreconditionError preconditionError) {
            preconditionError.printStackTrace();
            fail("PreconditionError in initAylaLog");
        }

    }

    @Test
    public void testPoll() {
        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();

        if (dm.getDevices().size() == 0) {
            fail("No devices on account to test with");
        }

        // First find an EVB test device
        AylaDevice device = dm.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull("Test device with DSN " + TestConstants.TEST_DEVICE_DSN +
            " not found on this test account", device);

        if (!device.getOemModel().equals("ledevb") && !device.getOemModel().equals("AylaShield")) {
            fail("Non-EVB device found on account. These tests require only Ayla EVB devices " +
                    "connected in order to pass successfully");
        }

        // Disable LAN mode for this device
        device.setLanModePermitted(false);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            fail("Failed to sleep");
        }

        // Register our listener
        device.addListener(_listener);

        final AylaProperty<Integer> property = device.getProperty(
                TestConstants.TEST_DEVICE_PROPERTY);
        int blueLEDValue = property.getValue();
        Date blueLEDOldDate = property.getDataUpdatedAt();

        AylaLog.v(LOG_TAG, "Device " + device.getDsn() + " Blue LED: " + blueLEDValue);

        // Create a new datapoint with the opposite value in 1 second
        final int newDatapointValue = blueLEDValue == 0 ? 1 : 0;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                RequestFuture<AylaDatapoint<Integer>> future = RequestFuture.newFuture();
                property.createDatapoint(newDatapointValue, null, future, future);
            }
        }, 1000);

        // We won't actually wait for the future here- we will instead wait for the property
        // change notification coming from DeviceManager.
        synchronized (_listener) {
            try {
                _listener.wait(30000);
            } catch (InterruptedException e) {
                fail("Interrupted waiting for poll notification");
            }
        }

        // Make sure the value changed
        int postChangeBlueLEDValue = property.getValue();
        assertNotSame("Property value did not change: " + blueLEDValue + " != " +
                postChangeBlueLEDValue, blueLEDValue, postChangeBlueLEDValue);

        // Make sure the timestamp is later than it used to be
        Date postChangeBlueLEDDate = property.getDataUpdatedAt();
        assertTrue(postChangeBlueLEDDate.after(blueLEDOldDate));
    }

    /**
     * Listener class we use to wait for device changes with
     */
    class DeviceListener implements AylaDevice.DeviceChangeListener {
        public AylaError _error = null;

        @Override
        public synchronized void deviceChanged(AylaDevice device, Change change) {
            AylaLog.v(LOG_TAG, "Device " + device.getDsn() + " changed: " + change);
            if (change instanceof PropertyChange) {
                PropertyChange propertyChange = (PropertyChange) change;
                if (propertyChange.getPropertyName().equals(TestConstants.TEST_DEVICE_PROPERTY) &&
                        propertyChange.getChangedFieldNames().contains("value")) {
                    notify();
                    AylaLog.v(LOG_TAG, "Blue LED value has changed. Notifying waiters...");
                }
            }
        }

        @Override
        public synchronized void deviceError(AylaDevice device, AylaError error) {
            _error = error;
            AylaLog.e(LOG_TAG, "Device " + device.getDsn() + " error: " + error);
            notify();
        }

        @Override
        public void deviceLanStateChanged(AylaDevice device, boolean lanModeEnabled, AylaError error) {
            AylaLog.d(LOG_TAG, "Device " + device.getDsn() + " changed LAN state. Enabled: " +
                    lanModeEnabled);
        }
    }
}
