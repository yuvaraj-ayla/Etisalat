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

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class BatchDatapointTest {
    private AylaDevice _device;
    private AylaDeviceManager _deviceManager;

    @Before
    public void setUp() {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        _deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME)
                .getDeviceManager();
        _device = _deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(_device);

        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        _device.fetchPropertiesCloud(null, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get();
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        }
    }

    @Test
    public void testCreateDataPoints() {
        AylaProperty<Integer> blueLEDProperty = _device.getProperty("Blue_LED");
        assertNotNull(blueLEDProperty);
        AylaProperty<Integer> greenLEDProperty = _device.getProperty("Green_LED");
        assertNotNull(greenLEDProperty);
        AylaProperty<String> cmdProperty = _device.getProperty(TestConstants.TEST_DEVICE_PROPERTY_STRING);
        assertNotNull(cmdProperty);
        AylaProperty<Float> decimalProperty = _device.getProperty(TestConstants.TEST_DEVICE_PROPERTY_DECIMAL);
        assertNotNull(decimalProperty);
        AylaProperty<Integer> intProperty = _device.getProperty(TestConstants.TEST_DEVICE_PROPERTY_INT);
        assertNotNull(intProperty);

        int oldValue = blueLEDProperty.getValue();
        //Just flip Blue LED
        int newBlueValue = ((oldValue == 0) ? 1 : 0);
        oldValue =  greenLEDProperty.getValue();
        //Just flip Green LED
        int newGreenValue = ((oldValue == 0) ? 1 : 0);

        String newStringValue =  String.valueOf(new Random(1).nextInt());;

        Float oldDecimalvalue = decimalProperty.getValue() == null
                ? 0.0f
                : decimalProperty.getValue().floatValue();
        Float newDecimalValue = oldDecimalvalue + 1;

        int oldIntvalue = intProperty.getValue() == null
                ? 0
                : intProperty.getValue().intValue();
        int newIntValue = oldIntvalue + 1;

        AylaDatapointBatchRequest<Integer> request1 = new AylaDatapointBatchRequest(newBlueValue,
                blueLEDProperty);
        AylaDatapointBatchRequest<Integer> request2 = new AylaDatapointBatchRequest(newGreenValue,
                greenLEDProperty);
        AylaDatapointBatchRequest<String> request3 = new AylaDatapointBatchRequest(newStringValue,
                cmdProperty);
        AylaDatapointBatchRequest<Float> request4 = new AylaDatapointBatchRequest(newDecimalValue,
                decimalProperty);
        AylaDatapointBatchRequest<Integer> request5 = new AylaDatapointBatchRequest(newIntValue,
                intProperty);

        AylaDatapointBatchRequest[] requests = new AylaDatapointBatchRequest[5];
        requests[0] = request1;
        requests[1] = request2;
        requests[2] = request3;
        requests[3] = request4;
        requests[4] = request5;

        RequestFuture<AylaDatapointBatchResponse[]> future = RequestFuture.newFuture();
        _deviceManager.createDatapointBatch(requests, future, future);
        AylaDatapointBatchResponse[] response = null;
        try {
            int API_TIMEOUT_MS = 30000;
            response = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Batch datapoint creation" + e);
        } catch (ExecutionException e) {
            fail("Error in Batch datapoint creation" + e);
        } catch (TimeoutException e) {
            fail("Error in Batch datapoint creation" + e);
        }
        assertNotNull(response);
        assertEquals(response[0].getDatapoint().getValue(), newBlueValue);
        assertEquals(response[1].getDatapoint().getValue(), newGreenValue);
        assertEquals(response[2].getDatapoint().getValue(), newStringValue);
        assertEquals(response[3].getDatapoint().getValue(), newDecimalValue);
        assertEquals(response[4].getDatapoint().getValue(), newIntValue);

        RequestFuture<AylaDatapointBatchResponse[]> futureResetValue = RequestFuture.newFuture();
        AylaDatapointBatchRequest<Float> request6 = new AylaDatapointBatchRequest(oldDecimalvalue,
                decimalProperty);
        AylaDatapointBatchRequest<Integer> request7 = new AylaDatapointBatchRequest(oldIntvalue,
                intProperty);

        requests = new AylaDatapointBatchRequest[2];
        requests[0] = request6;
        requests[1] = request7;
        response = null;
        _deviceManager.createDatapointBatch(requests, futureResetValue, futureResetValue);
        try {
            int API_TIMEOUT_MS = 30000;
            response = futureResetValue.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Batch datapoint creation" + e);
        } catch (ExecutionException e) {
            fail("Error in Batch datapoint creation" + e);
        } catch (TimeoutException e) {
            fail("Error in Batch datapoint creation" + e);
        }
        assertNotNull(response);
        assertEquals(response[0].getDatapoint().getValue(), oldDecimalvalue);
        assertEquals(response[1].getDatapoint().getValue(), oldIntvalue);
    }
}
