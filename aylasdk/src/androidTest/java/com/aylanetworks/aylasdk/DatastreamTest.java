package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Android_AylaSDK
 * <p>
 * Copyright 2016 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class DatastreamTest {
    private AylaTestAccountConfig _user;
    private AylaTestConfig _testConfig;
    AylaSystemSettings _testSystemSettings;
    private AylaAuthorization _aylaAuthorization;
    private AylaSessionManager _sessionManager;
    private AylaDSSubscription _aylaSubscription;
    private AylaDSManager _dssManager;
    private AylaDeviceManager _deviceManager;

    //Have two sets of tests. _testSystemSettings.allowDSS = true and false
    //To test subscription CRUD. Set allowDSS to false.
    @Before
    public void setUp() throws Exception {
        _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(10000);
        _testSystemSettings = TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS;
        assertTrue(_testSystemSettings.allowDSS);
        _testConfig.setTestSystemSettings(_testSystemSettings);
        _user = new AylaTestAccountConfig(TestConstants.TEST_USERNAME, TestConstants
                .TEST_PASSWORD, TestConstants.TEST_DEVICE_DSN, TestConstants.TEST_DSS_SESSION_NAME);
        _aylaAuthorization = _testConfig.signIn(_user, InstrumentationRegistry.getContext());

        assertNotNull("Failed to sign-in", _aylaAuthorization);
        _sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_user.getTestSessionName());
        assertNotNull("Failed to get session manager", _sessionManager);

        _testConfig.waitForDeviceManagerInitComplete(_user.getTestSessionName());

    }

    @Test
    public void testCreateSubscription(){
        _deviceManager = _sessionManager.getDeviceManager();
        _dssManager = _sessionManager.getDSManager();
        RequestFuture<AylaDSSubscription> requestFutureCreate = RequestFuture.newFuture();
        _dssManager.createSubscription("testName", "description1", _deviceManager.getDevices(),
                 requestFutureCreate, requestFutureCreate);

        try {
            _aylaSubscription = requestFutureCreate.get(_testConfig.getApiTimeOut(), TimeUnit
                    .MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("Failed to create subscription "+e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            fail("Failed to create subscription " + e);
        } catch (TimeoutException e) {
            e.printStackTrace();
            fail("Failed to create subscription "+e);
        }
        assertNotNull("Ayla subscription created ", _aylaSubscription);
    }

    @Test
    public void testFetchSubscriptionWithId(){
        _deviceManager = _sessionManager.getDeviceManager();
        _dssManager = _sessionManager.getDSManager();
        AylaDSSubscription subscription = null;
        RequestFuture<AylaDSSubscription> requestFutureFetch = RequestFuture.newFuture();
        if(_aylaSubscription != null){
            if(_aylaSubscription.getId() != null){
                _dssManager.fetchSubscription(_aylaSubscription.getId(),
                        requestFutureFetch,
                        requestFutureFetch);
                try {
                    subscription = requestFutureFetch.get(_testConfig.getApiTimeOut(), TimeUnit
                            .MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail("Exception in fetchSubscription with id "+e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    fail("Exception in fetchSubscription with id " + e);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    fail("Exception in fetchSubscription with id " + e);
                }
                assertNotNull(subscription);
                assertEquals("Fetched subscription is same as excpected subscription" ,
                        _aylaSubscription.getId(), subscription.getId());

            }
        }
    }

    @Test
    public void testUpdateSubscription(){
        _deviceManager = _sessionManager.getDeviceManager();
        _dssManager = _sessionManager.getDSManager();
        AylaDSSubscription updatedSubscription = null;
        if(_aylaSubscription != null){
            if(_aylaSubscription.getId() != null){
                RequestFuture<AylaDSSubscription> requestFutureCreate = RequestFuture.newFuture();
                _dssManager.updateSubscription(_aylaSubscription,
                        _deviceManager.getDevices(),
                         requestFutureCreate, requestFutureCreate);

                try {
                    updatedSubscription = requestFutureCreate.get(_testConfig.getApiTimeOut(), TimeUnit
                            .MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    fail("Failed to create subscription "+e);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    fail("Failed to create subscription " + e);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    fail("Failed to create subscription "+e);
                }
                assertNotNull("Ayla subscription created ", updatedSubscription);
                assertEquals("Fetched subscription is same as excpected subscription",
                        _aylaSubscription.getId(), updatedSubscription.getId());
            }
        }

    }

    @After
    public void tearDown() throws Exception {
        RequestFuture<AylaAPIRequest.EmptyResponse> requestFutureDelete = RequestFuture.newFuture();
        if(_aylaSubscription != null){
            _dssManager.deleteSubscription(_aylaSubscription.getId(), requestFutureDelete,
                    requestFutureDelete);

            try {
                requestFutureDelete.get(_testConfig.getApiTimeOut(), TimeUnit
                        .MILLISECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                fail("Failed to delete subscription "+e);
            } catch (ExecutionException e) {
                e.printStackTrace();
                fail("Failed to delete subscription " + e);
            } catch (TimeoutException e) {
                e.printStackTrace();
                fail("Failed to delete subscription "+e);
            }
        }

    }
}
