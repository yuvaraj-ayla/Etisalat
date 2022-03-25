package com.aylanetworks.aylasdk;
/*
 * Android_AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class PropertyTest{
    // Time we will wait to enter LAN mode on our test device
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
    public void testProperties() {

        AylaProperty<Integer> property = _device.getProperty(TestConstants.TEST_DEVICE_PROPERTY);

        assertNotNull("Test device not found", _device);
        assertNotNull("Test property not found", property);

        int oldValue = property.getValue();
        int newValue = (oldValue == 0) ? 1 : 0;

        // Create a datapoint via the cloud

        RequestFuture<AylaDatapoint<Integer>> future = RequestFuture.newFuture();
        AylaDatapoint<Integer> dp = null;

        property.createDatapointCloud(newValue, null, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointCloud interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointCloud execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointCloud timed out");
        }

        assertTrue("Datapoint has wrong value after create", (dp.getValue() == newValue));
        assertTrue("Property has wrong value after create", property.getValue() == newValue);

        // Put the datapoint back via the LAN
        future = RequestFuture.newFuture();
        property.createDatapointLAN(oldValue, null, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointLAN interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointLAN execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointLAN timed out");
        }

        assertTrue("Datapoint has wrong value after create", dp.getValue() == oldValue);
        assertTrue("Property has wrong value after create", property.getValue() == oldValue);

        // Test with metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Key1", "Value1");
        metadata.put("Key2", "Value2");
        metadata.put("Key3", "Value3");

        future = RequestFuture.newFuture();
        property.createDatapointCloud(newValue, metadata, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointCloud w/ metadata interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointCloud w/ metadata execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointCloud w/ metadata timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue().intValue(), newValue);
        assertEquals("Property has wrong value after create", property.getValue().intValue(), newValue);
        assertNotNull("Datapoint does not have metadata", dp.getMetadata());
        assertEquals("Datapoint metadata does not match", dp.getMetadata(), metadata);
        assertNotNull("Property metadata is null", property.getMetadata());
        assertEquals("Property metadata does not match", property.getMetadata(), metadata);

        // Now again with LAN
        future = RequestFuture.newFuture();

        Map<String, String> newMeta = new HashMap<>();
        newMeta.put("KeyFour", "Value Four");
        newMeta.put("KeyFive", "Value Five");
        newMeta.put("KeySix", "Value Six");
        property.createDatapointLAN(oldValue, newMeta, future, future);
        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointLAN w/ metadata interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointLAN w/ metadata execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointLAN w/ metadata timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue().intValue(), oldValue);
        assertEquals("Property has wrong value after create", property.getValue().intValue(), oldValue);
        assertNotNull("Datapoint does not have metadata", dp.getMetadata());
        assertEquals("Datapoint metadata does not match", dp.getMetadata(), newMeta);
        assertNotNull("Property metadata is null", property.getMetadata());
        assertEquals("Property metadata does not match", property.getMetadata(), newMeta);

    }
    @Test
    public void testStringProperties(){

        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        _device.fetchPropertiesCloud(null, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        } catch (TimeoutException e) {
            fail("Timeout Exception in fetchPropertiesCloud");
        }

        AylaProperty<String> propertyCmd = _device.getProperty(
                TestConstants.TEST_DEVICE_PROPERTY_STRING);

        assertNotNull("Test string property not found", propertyCmd);

        String oldStringVal = propertyCmd.getValue();
        String newStringVal = oldStringVal +"_new";

        // Create a datapoint via the cloud

        RequestFuture<AylaDatapoint<String>> future = RequestFuture.newFuture();
        AylaDatapoint<String> dp = null;

        propertyCmd.createDatapointCloud(newStringVal, null, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointCloud interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointCloud execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointCloud timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), newStringVal);
        assertEquals("Property has wrong value after create", propertyCmd.getValue(), newStringVal);

        assertTrue("Device did not enter LAN mode", _device.isLanModeActive());

        // Put the datapoint back via the LAN
        future = RequestFuture.newFuture();
        propertyCmd.createDatapointLAN(oldStringVal, null, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointLAN interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointLAN execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointLAN timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), oldStringVal);
        assertEquals("Property has wrong value after create", propertyCmd.getValue(), oldStringVal);

        // Test with metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Key1", "Value1");
        metadata.put("Key2", "Value2");
        metadata.put("Key3", "Value3");

        future = RequestFuture.newFuture();
        propertyCmd.createDatapointCloud(newStringVal, metadata, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointCloud w/ metadata interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointCloud w/ metadata execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointCloud w/ metadata timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), newStringVal);
        assertEquals("Property has wrong value after create", propertyCmd.getValue(), newStringVal);
        assertNotNull("Datapoint does not have metadata", dp.getMetadata());
        assertEquals("Datapoint metadata does not match", dp.getMetadata(), metadata);
        assertNotNull("Property metadata is null", propertyCmd.getMetadata());
        assertEquals("Property metadata does not match", propertyCmd.getMetadata(), metadata);

        // Now again with LAN
        future = RequestFuture.newFuture();

        Map<String, String> newMeta = new HashMap<>();
        newMeta.put("KeyFour", "Value Four");
        newMeta.put("KeyFive", "Value Five");
        newMeta.put("KeySix", "Value Six");
        propertyCmd.createDatapointLAN(oldStringVal, newMeta, future, future);
        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointLAN w/ metadata interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointLAN w/ metadata execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointLAN w/ metadata timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), oldStringVal);
        assertEquals("Property has wrong value after create", propertyCmd.getValue(), oldStringVal);
        assertNotNull("Datapoint does not have metadata", dp.getMetadata());
        assertEquals("Datapoint metadata does not match", dp.getMetadata(), newMeta);
        assertNotNull("Property metadata is null", propertyCmd.getMetadata());
        assertEquals("Property metadata does not match", propertyCmd.getMetadata(), newMeta);

    }
    @Test
    public void testDecimalProperties(){

        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        _device.fetchPropertiesCloud(null, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        } catch (TimeoutException e) {
            fail("Timeout Exception in fetchPropertiesCloud");
        }

        AylaProperty<Float> propertyDecimal = _device.getProperty(TestConstants
                .TEST_DEVICE_PROPERTY_DECIMAL);

        assertNotNull("Test decimal property not found", propertyDecimal);

        Float oldDecimalVal = propertyDecimal.getValue() == null ? 0.0f : propertyDecimal.getValue();
        Float newDecimalVal = oldDecimalVal + 1;

        // Create a datapoint via the cloud

        RequestFuture<AylaDatapoint<Float>> future = RequestFuture.newFuture();
        AylaDatapoint<Float> dp = null;

        propertyDecimal.createDatapointCloud(newDecimalVal, null, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointCloud interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointCloud execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointCloud timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), newDecimalVal);
        assertEquals("Property has wrong value after create", propertyDecimal.getValue(), newDecimalVal);

        assertTrue("Device did not enter LAN mode", _device.isLanModeActive());

        // Put the datapoint back via the LAN
        future = RequestFuture.newFuture();
        propertyDecimal.createDatapointLAN(oldDecimalVal, null, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointLAN interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointLAN execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointLAN timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), oldDecimalVal);
        assertEquals("Property has wrong value after create", propertyDecimal.getValue(), oldDecimalVal);

        // Test with metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Key1", "Value1");
        metadata.put("Key2", "Value2");
        metadata.put("Key3", "Value3");

        future = RequestFuture.newFuture();
        propertyDecimal.createDatapointCloud(newDecimalVal, metadata, future, future);

        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointCloud w/ metadata interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointCloud w/ metadata execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointCloud w/ metadata timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), newDecimalVal);
        assertEquals("Property has wrong value after create", propertyDecimal.getValue(), newDecimalVal);
        assertNotNull("Datapoint does not have metadata", dp.getMetadata());
        assertEquals("Datapoint metadata does not match", dp.getMetadata(), metadata);
        assertNotNull("Property metadata is null", propertyDecimal.getMetadata());
        assertEquals("Property metadata does not match", propertyDecimal.getMetadata(), metadata);

        // Now again with LAN
        future = RequestFuture.newFuture();

        Map<String, String> newMeta = new HashMap<>();
        newMeta.put("KeyFour", "Value Four");
        newMeta.put("KeyFive", "Value Five");
        newMeta.put("KeySix", "Value Six");
        propertyDecimal.createDatapointLAN(oldDecimalVal, newMeta, future, future);
        try {
            dp = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("createDatapointLAN w/ metadata interrupted: " + e);
        } catch (ExecutionException e) {
            fail("createDatapointLAN w/ metadata execution exception: " + e);
        } catch (TimeoutException e) {
            fail("createDatapointLAN w/ metadata timed out");
        }

        assertEquals("Datapoint has wrong value after create", dp.getValue(), oldDecimalVal);
        assertEquals("Property has wrong value after create", propertyDecimal.getValue(), oldDecimalVal);
        assertNotNull("Datapoint does not have metadata", dp.getMetadata());
        assertEquals("Datapoint metadata does not match", dp.getMetadata(), newMeta);
        assertNotNull("Property metadata is null", propertyDecimal.getMetadata());
        assertEquals("Property metadata does not match", propertyDecimal.getMetadata(), newMeta);
    }

}
