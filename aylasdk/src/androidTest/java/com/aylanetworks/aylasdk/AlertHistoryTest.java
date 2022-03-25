package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.AylaAlertHistory.AlertFilter;
import com.aylanetworks.aylasdk.error.AuthError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.error.ServerError;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.aylanetworks.aylasdk.AylaAlertHistory.AlertHistoryFilters.AlertType;
import static com.aylanetworks.aylasdk.AylaAlertHistory.AlertHistoryFilters.PropertyName;
import static com.aylanetworks.aylasdk.AylaAlertHistory.FilterOperators.Not;
import static com.aylanetworks.aylasdk.AylaAlertHistory.FilterOperators.NotIn;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * AylaSDK
 * <p/>
 * Copyright 2016 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class AlertHistoryTest {
    AylaDevice _device;
    AylaDeviceManager _deviceManager;

    @Before
    public void setUp() throws Exception {

        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        _deviceManager = AylaNetworks.sharedInstance().getSessionManager(TestConstants
                .TEST_SESSION_NAME).getDeviceManager();
        assertNotNull(_deviceManager);
        _device = _deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(_device);
    }

    //Test with null DSN
    @Test
    public void testWithNullDSN(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        _deviceManager.fetchAlertHistory(null, requestFuture,
                requestFuture, false, 10, 1, null, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNull(alertHistory);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            try{
                PreconditionError error = (PreconditionError) e.getCause();
                assertEquals(PreconditionError.class, error.getClass());
            } catch (ClassCastException ex){
                fail("testWithNullDSN failed with invalid unexpected " +
                        "error code " + e);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }


    //Test with DSN not owned by the user. Expects 403 error code.
    @Test
    public void testWithForbiddenDSN(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE2_DSN, requestFuture,
                requestFuture, false, 10, 1, null, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNull(alertHistory);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            try{
                AuthError error = (AuthError) e.getCause();
               assertTrue(error.getMessage().contains("403"));
            } catch (ClassCastException ex){
                fail("testWithForbiddenDSN failed with invalid unexpected " +
                        "error code " + e);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }
    //Test invalid DSN. Expects 404 error code.
    @Test
    public void testWithInvalidDSN(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        _deviceManager.fetchAlertHistory("AC0000", requestFuture,
                requestFuture, false, 10, 1, null, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNull(alertHistory);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            try{
                ServerError error = (ServerError) e.getCause();
                assertTrue(error.getMessage().contains("404"));
            } catch (ClassCastException ex){
                fail("testWithForbiddenDSN failed with invalid unexpected " +
                        "error code " + e);
            }
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    //Test with no filter
    @Test
    public void testWithNoFilters(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, false, 10, 1, null, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    //Test with pagination
    @Test
    public void testPaginated(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, true, 5, 1, null, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
            assertTrue(alertHistory.length <= 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    //Test with one filter, one operand
    @Test
    public void testWithFilter(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        AlertFilter filter = new AlertFilter();
        filter.add(PropertyName, Not, "Blue_LED");
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, false, 10, 1, filter, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
            for(AylaAlertHistory alert: alertHistory){
                assertFalse(alert.getPropertyName().equals("Blue_LED"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }


    //Test with one filter, mulitple operands
    @Test
    public void testWithSingleFilterMultipleOperands(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        AlertFilter filter = new AlertFilter();
        filter.add(PropertyName, NotIn,  "Blue_LED,Blue_Button");
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, false, 10, 1, filter, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
            for(AylaAlertHistory alert: alertHistory){
                assertFalse(alert.getPropertyName().equals("Blue_LED"));
                assertFalse(alert.getPropertyName().equals("Blue_button"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    //Test with multiple filters
    @Test
    public void testWithMultipleFilters(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;
        AlertFilter filter = new AlertFilter();
        filter.add(PropertyName, Not,  "Blue_LED");
        filter.add(AlertType, Not, "email");
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, false, 10, 1, filter, null);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
            for(AylaAlertHistory alert: alertHistory){
                assertFalse(alert.getPropertyName().equals("Blue_LED"));
                assertFalse(alert.getAlertType().equals("email"));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    //Test with results sorted oldest to most recent
    @Test
    public void testWithSortDesc(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;

        Map<String, String> map = new HashMap<>(2);
        map.put("order_by", "sent_at");
        map.put("order", "desc");
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, false, 10, 1, null, map);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
            boolean success = checkDateSortingOrder(alertHistory, false);
            assertTrue(success);

        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    //Test with results sorted most recent to oldest
    @Test
    public void testWithSortAsc(){
        RequestFuture<AylaAlertHistory[]> requestFuture = RequestFuture.newFuture();
        AylaAlertHistory[] alertHistory;

        Map<String, String> map = new HashMap<>(2);
        map.put("order_by", "sent_at");
        map.put("order", "asc");
        _deviceManager.fetchAlertHistory(TestConstants.TEST_DEVICE_DSN, requestFuture,
                requestFuture, false, 10, 1, null, map);
        try {
            alertHistory = requestFuture.get(30000, TimeUnit.MILLISECONDS);
            assertNotNull(alertHistory);
            boolean success = checkDateSortingOrder(alertHistory, true);
            assertTrue(success);

        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Exception "+e.getMessage());
        }
    }

    private boolean checkDateSortingOrder(AylaAlertHistory[] alertList, boolean isAscending){
        boolean success = true;
        Date date1;
        Date date2;
        for(int i=1; i< alertList.length; i++){
            date1 = alertList[i-1].getSentAtDate();
            date2 = alertList[i].getSentAtDate();
            if(date1.equals(date2)){
                continue;
            }
            success = date1.after(date2) ;
            if(isAscending){
                //check for ascending order
                if(success){
                    return false;
                }
            } else{
                // check for descneding order
                if(!success){
                    return false;
                }
            }

        }
        return true;
    }
}
