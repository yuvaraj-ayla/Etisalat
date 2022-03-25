package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DeviceNotificationTest {
    private AylaDevice _device;
    private AylaDeviceNotification _deviceNotification;

    @Before
    public void setUp() throws Exception {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        _device = dm.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(_device);
    }

    @Test
    public void testCreateDeviceNotification() {
        AylaDeviceNotification deviceNotification = new AylaDeviceNotification();
        deviceNotification.setNotificationType(AylaDeviceNotification.NotificationType.
                ConnectionLost);
        deviceNotification.setThreshold(3600);
        deviceNotification.setMessage("Connection dropped");

        RequestFuture<AylaDeviceNotification> future = RequestFuture.newFuture();
        _device.createNotification(deviceNotification, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotification = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotification);
    }

    @Test
    public void testUpdateDeviceNotification() {
        //First create a notification
        AylaDeviceNotification deviceNotification = new AylaDeviceNotification();
        deviceNotification.setNotificationType(AylaDeviceNotification.NotificationType.
                ConnectionLost);
        deviceNotification.setThreshold(3600);
        deviceNotification.setMessage("Connection dropped");
        deviceNotification.setActive(true);

        RequestFuture<AylaDeviceNotification> future = RequestFuture.newFuture();
        _device.createNotification(deviceNotification, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotification = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotification);

        //Now update the created Notification's ThreshHold
        Integer threshHold = 2000;
        _deviceNotification.setThreshold(threshHold);
        _deviceNotification.setActive(false);
        RequestFuture<AylaDeviceNotification> futureUpdate = RequestFuture.newFuture();
        _device.updateNotification(_deviceNotification, futureUpdate, futureUpdate);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotification = futureUpdate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification update " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification update " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification update " + e);
        }
        assertNotNull(_deviceNotification);
        assertEquals(_deviceNotification.getThreshold(), threshHold);
        assertFalse(_deviceNotification.getActive());
    }

    //fetch all Notifications
    @Test
    public void testFetchAllNotifications() {
        AylaDeviceNotification deviceNotification = new AylaDeviceNotification();
        deviceNotification.setNotificationType(AylaDeviceNotification.NotificationType.
                ConnectionLost);
        deviceNotification.setThreshold(3600);
        deviceNotification.setMessage("Connection dropped");

        RequestFuture<AylaDeviceNotification> future = RequestFuture.newFuture();
        _device.createNotification(deviceNotification, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotification = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotification);

        RequestFuture<AylaDeviceNotification[]> futureFetch = RequestFuture.newFuture();
        AylaDeviceNotification[] notifications = null;
        _device.fetchNotifications(futureFetch, futureFetch);
        try {
            int API_TIMEOUT_MS = 20000;
            notifications = futureFetch.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification fetch " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification fetch " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification fetch " + e);
        }
        assertNotNull(notifications);
        assert (notifications.length > 0);
    }

    @After
    public void tearDown() throws Exception {
        if (_deviceNotification != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
            _device.deleteNotification(_deviceNotification, futureDelete, futureDelete);
            try {
                futureDelete.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Error in DeviceNotification delete " + e);
            } catch (ExecutionException e) {
                fail("Error in DeviceNotification delete " + e);
            } catch (TimeoutException e) {
                fail("Error in DeviceNotification delete " + e);
            }
        }
    }
}
