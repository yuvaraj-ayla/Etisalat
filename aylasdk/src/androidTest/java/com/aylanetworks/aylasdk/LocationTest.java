package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.error.ServerError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Android_AylaSDK
 * <p>
 * Copyright 2016 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class LocationTest {

    private boolean _isSignedIn;
    AylaDevice _device;
    AylaDeviceManager _deviceManager;
    String _testDeviceLat ;
    String _testDeviceLong;
    String _testLat1 = "39.09";
    String _testLong1 = "-121.5673";

    @Before
    public void setUp() throws Exception {
        _isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue("Failed to sign-in", _isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        _deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        assertNotNull(_deviceManager);
        _device = _deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        _testDeviceLat = _device.getLat();
        _testDeviceLong = _device.getLng();
        assertNotNull(_device);
    }

    //Update with valid data
    @Test
    public void testUpdateLocation(){
        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        _device.updateLocation(_testLat1, _testLong1, AylaDevice.LocationProvider.User, future,
                future);

        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    //Update with invalid location
    @Test
    public void testUpdateLocationInvalidLocation(){
        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        _device.updateLocation("3745376237", "28346782347", AylaDevice.LocationProvider.User,
                future, future);

        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            try{
                ServerError error = (ServerError) e.getCause();
                assertTrue(error.getServerResponseCode() == 422);
            } catch (ClassCastException ex){
                fail("testUpdateLocationInvalidDevice failed with unexpected " +
                        "error code " + e);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

    @After
    public void tearDown() throws Exception {
        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        _device.updateLocation(_testDeviceLat, _testDeviceLong, AylaDevice.LocationProvider.User, future,
                future);

        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}