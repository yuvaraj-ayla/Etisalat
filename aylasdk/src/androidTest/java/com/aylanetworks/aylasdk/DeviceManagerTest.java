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

import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DeviceManagerTest {

    @Before
    public void setup() {
        assertTrue(TestConstants.signIn(InstrumentationRegistry.getContext()));
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
    }

    @Test
    public void testInitComplete() {
        AylaDeviceManager deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();

        // We should have at least one device at this point
        List<AylaDevice> deviceList = deviceManager.getDevices();
        assertNotNull(deviceList);
        assertTrue(deviceList.size() > 0);

        // The devices should all have properties
        for (AylaDevice device : deviceList) {
            assertTrue("Device " + device.getDsn() + " has no properties",
                    device.getProperties().size() > 0);
        }
    }

    @Test
    public void testFetchDeviceWithDSN() {
        AylaDeviceManager deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        List<AylaDevice> deviceList = deviceManager.getDevices();
        String dsn = deviceList.get(0).getDsn();

        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        deviceManager.fetchDeviceDetailsWithDSN(dsn, future, future);
        try {
            AylaDevice device = future.get();
            assertNotNull(device.getCreatedAt());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        }
    }
}
