package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2019 Ayla Networks, all rights reserved
 */

import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.volley.Request;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.aylanetworks.aylasdk.TestConstants.TEST_DEVICE_MESSAGE_PROPERTY_BINARY_OUT;
import static com.aylanetworks.aylasdk.TestConstants.TEST_DEVICE_MESSAGE_PROPERTY_JSON_OUT;
import static com.aylanetworks.aylasdk.TestConstants.TEST_DEVICE_MESSAGE_PROPERTY_STRING_OUT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class MessageDatapointTest {

    private static final String LOG_TAG = "MessageDatapointTest";

    private AylaMessageProperty _jsonOutProperty;
    private AylaMessageProperty _binaryOutProperty;
    private AylaMessageProperty _stringOutProperty;

    private static final String[] TEST_MESSAGE_PROPERTIES = new String[] {
            TEST_DEVICE_MESSAGE_PROPERTY_JSON_OUT,
            TEST_DEVICE_MESSAGE_PROPERTY_STRING_OUT,
            TEST_DEVICE_MESSAGE_PROPERTY_BINARY_OUT
    };

    @Before
    public void setUp() {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue("Failed to sign-in", isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME)
                .getDeviceManager();
        AylaDevice device = deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(device);

        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        device.fetchPropertiesCloud(TEST_MESSAGE_PROPERTIES, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        } catch (TimeoutException e) {
            fail("Timeout Exception in fetchProperties");
        }

        _jsonOutProperty = (AylaMessageProperty) device.getProperty(TEST_DEVICE_MESSAGE_PROPERTY_JSON_OUT);
        if (_jsonOutProperty == null) {
            _jsonOutProperty = createMessageProperty(device,
                    TEST_DEVICE_MESSAGE_PROPERTY_JSON_OUT, "application/json");
        }

        _stringOutProperty = (AylaMessageProperty) device.getProperty(TEST_DEVICE_MESSAGE_PROPERTY_STRING_OUT);
        if (_stringOutProperty == null) {
            _stringOutProperty = createMessageProperty(device,
                    TEST_DEVICE_MESSAGE_PROPERTY_STRING_OUT, "text/plain");
        }

        _binaryOutProperty = (AylaMessageProperty) device.getProperty(TEST_DEVICE_MESSAGE_PROPERTY_BINARY_OUT);
        if (_binaryOutProperty == null) {
            _binaryOutProperty = createMessageProperty(device,
                    TEST_DEVICE_MESSAGE_PROPERTY_BINARY_OUT, "application/octet-stream");
        }

        String message = "%s property is null";
        assertNotNull(String.format(message, TEST_DEVICE_MESSAGE_PROPERTY_JSON_OUT), _jsonOutProperty);
        assertNotNull(String.format(message, TEST_DEVICE_MESSAGE_PROPERTY_STRING_OUT), _stringOutProperty);
        assertNotNull(String.format(message, TEST_DEVICE_MESSAGE_PROPERTY_BINARY_OUT), _binaryOutProperty);
    }

    @Test
    public void testCreateDatapointJSON() {
        RequestFuture<AylaDatapoint<String>> future = RequestFuture.newFuture();
        String random = String.valueOf(new Random(1).nextInt());
        String newJsonValue = "{\"json_out\":\"Hello JSON message " + random + "\"}";
        _jsonOutProperty.createDatapoint(newJsonValue, null, future, future);

        AylaDatapoint<String> datapoint = null;
        try {
            datapoint = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("CreateDatapoint Interrupted Exception");
        } catch (ExecutionException e) {
            fail("CreateDatapoint Execution Exception " + e.getMessage());
        } catch (TimeoutException e) {
            fail("Timeout Exception");
        }

        assertNotNull("datapoint is null", datapoint);
        assertNotNull("datapoint value is null", datapoint.getValue());
        assertNotNull("datapoint ID is null", datapoint.getId());
        assertEquals("datapoint ID not match", datapoint.getId(), _jsonOutProperty.getMessageDatapoiontId());

        String dpValue = datapoint.getValue();
        String expectedDpValue = "/" + _jsonOutProperty.getName() + "/" + datapoint.getId();
        assertEquals("datapoint value not match", expectedDpValue, dpValue);
        assertEquals("datapoint value not match", expectedDpValue, _jsonOutProperty.getValue());

        RequestFuture<String> messageFuture = RequestFuture.newFuture();
        _jsonOutProperty.fetchMessageContent(messageFuture, messageFuture);
        try {
            String expectedJsonValue = messageFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            Log.d(LOG_TAG, "expectedJsonValue:" + expectedJsonValue);
            assertEquals("mismatched message value", newJsonValue, expectedJsonValue);
        } catch (InterruptedException e) {
            fail("CreateDatapoint Interrupted Exception");
        } catch (ExecutionException e) {
            fail("CreateDatapoint Execution Exception " + e.getMessage());
        } catch (TimeoutException e) {
            fail("Timeout Exception");
        }
    }

    @Test
    public void testCreateDatapointString() {
        RequestFuture<AylaDatapoint<String>> future = RequestFuture.newFuture();
        String random = String.valueOf(new Random(1).nextInt());
        String newJsonValue = "Hello String message " + random;
        _stringOutProperty.createDatapoint(newJsonValue, null, future, future);

        AylaDatapoint<String> datapoint = null;
        try {
            datapoint = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("CreateDatapoint Interrupted Exception");
        } catch (ExecutionException e) {
            fail("CreateDatapoint Execution Exception " + e.getMessage());
        } catch (TimeoutException e) {
            fail("Timeout Exception");
        }

        assertNotNull("datapoint is null", datapoint);
        assertNotNull("datapoint value is null", datapoint.getValue());
        assertNotNull("datapoint ID is null", datapoint.getId());
        assertEquals("datapoint ID not match", datapoint.getId(), _stringOutProperty.getMessageDatapoiontId());

        String dpValue = datapoint.getValue();
        String expectedDpValue = "/" + _stringOutProperty.getName() + "/" + datapoint.getId();
        assertEquals("datapoint value not match", expectedDpValue, dpValue);
        assertEquals("datapoint value not match", expectedDpValue, _stringOutProperty.getValue());

        RequestFuture<String> messageFuture = RequestFuture.newFuture();
        _stringOutProperty.fetchMessageContent(messageFuture, messageFuture);
        try {
            String expectedJsonValue = messageFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            Log.d(LOG_TAG, "expectedStringValue:" + expectedJsonValue);
            assertEquals("mismatched message value", newJsonValue, expectedJsonValue);
        } catch (InterruptedException e) {
            fail("CreateDatapoint Interrupted Exception");
        } catch (ExecutionException e) {
            fail("CreateDatapoint Execution Exception " + e.getMessage());
        } catch (TimeoutException e) {
            fail("Timeout Exception");
        }
    }

    @Test
    public void testCreateDatapointBinary() {
        RequestFuture<AylaDatapoint<String>> future = RequestFuture.newFuture();
        String newJsonValue = "0x480x650x6c0x6c0x6f0x200x620x690x6e0x610x720x790x200x6d0x650x730x730x610x670x65";
        _binaryOutProperty.createDatapoint(newJsonValue, null, future, future);

        AylaDatapoint<String> datapoint = null;
        try {
            datapoint = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("CreateDatapoint Interrupted Exception");
        } catch (ExecutionException e) {
            fail("CreateDatapoint Execution Exception " + e.getMessage());
        } catch (TimeoutException e) {
            fail("Timeout Exception");
        }

        assertNotNull("datapoint is null", datapoint);
        assertNotNull("datapoint value is null", datapoint.getValue());
        assertNotNull("datapoint ID is null", datapoint.getId());
        assertEquals("datapoint ID not match", datapoint.getId(), _binaryOutProperty.getMessageDatapoiontId());

        String dpValue = datapoint.getValue();
        String expectedDpValue = "/" + _binaryOutProperty.getName() + "/" + datapoint.getId();
        assertEquals("datapoint value not match", expectedDpValue, dpValue);
        assertEquals("datapoint value not match", expectedDpValue, _binaryOutProperty.getValue());

        RequestFuture<String> messageFuture = RequestFuture.newFuture();
        _binaryOutProperty.fetchMessageContent(messageFuture, messageFuture);
        try {
            String expectedJsonValue = messageFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            Log.d(LOG_TAG, "expectedBinaryValue:" + expectedJsonValue);
            assertEquals("mismatched message value", newJsonValue, expectedJsonValue);
        } catch (InterruptedException e) {
            fail("CreateDatapoint Interrupted Exception");
        } catch (ExecutionException e) {
            fail("CreateDatapoint Execution Exception " + e.getMessage());
        } catch (TimeoutException e) {
            fail("Timeout Exception");
        }
    }

    private AylaMessageProperty createMessageProperty(AylaDevice device, String propertyName, String mimeType) {
        AylaDeviceManager dm = device.getDeviceManager();
        String path = String.format("apiv1/devices/%d/properties.json", device.getKey().intValue());
        String url = dm.deviceServiceUrl(path);
        String payload = "{\"property\":" +
                "{\"type\":\"Property\"," +
                "\"base_type\":\"message\", " +
                "\"mime_type\":\"" + mimeType + "\", " +
                "\"direction\":\"output\", " +
                "\"name\":\"" + propertyName + "\", " +
                "\"scope\":\"user\"}}";

        RequestFuture<AylaProperty.Wrapper> future = RequestFuture.newFuture();
        dm.sendDeviceServiceRequest(new AylaJsonRequest<>(
                Request.Method.POST,
                url,
                payload,
                null,
                AylaProperty.Wrapper.class,
                dm.getSessionManager(),
                future, future));

        try {
            AylaProperty.Wrapper wrapper = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            AylaMessageProperty property = (AylaMessageProperty) wrapper.property;
            property.setOwner(device);
            return property;
        } catch (InterruptedException e) {
            fail("CreateProperty Interrupted Exception");
        } catch (ExecutionException e) {
            String msg = (e.getCause() == null) ? e.getMessage() : e.getCause().getMessage();
            fail("CreateProperty Execution Exception: " + msg);
        } catch (TimeoutException e) {
            fail("CreateProperty Timeout Exception");
        }

        return null;
    }

}
