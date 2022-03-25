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

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ApplicationTriggerTest {
    private AylaProperty _property;
    private AylaPropertyTrigger _propertyTrigger;
    private AylaPropertyTriggerApp _triggerApp;
    private final String _testEMailAddress = TestConstants.TEST_USERNAME;
    private final String _testPhoneNumber = TestConstants.TEST_PHONE_NUMBER;
    private AylaContact _aylaContact = null;
    private AylaSessionManager _sessionManager;

    @Before
    public void setUp() throws Exception {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(isSignedIn);
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        _sessionManager = dm.getSessionManager();
        AylaDevice device = dm.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(device);
        _property = device.getProperty(TestConstants.TEST_DEVICE_PROPERTY);
        assertNotNull(_property);

        //Create a Property Trigger First
        Random random = new Random();
        Integer anInt = random.nextInt();
        String propertyNickname = "TriggerName_" + anInt.toString();
        AylaPropertyTrigger aylaPropertyTrigger = new AylaPropertyTrigger();
        aylaPropertyTrigger.setPropertyNickname(propertyNickname);
        aylaPropertyTrigger.setTriggerType(AylaPropertyTrigger.TriggerType.Always.stringValue());

        RequestFuture<AylaPropertyTrigger> future = RequestFuture.newFuture();
        _property.createTrigger(aylaPropertyTrigger, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _propertyTrigger = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in propertyTrigger creation " + e);
        } catch (ExecutionException e) {
            fail("Error in propertyTrigger creation " + e);
        } catch (TimeoutException e) {
            fail("Error in propertyTrigger creation " + e);
        }
        assertNotNull(_propertyTrigger);
    }

    @Test
    public void testCreateEmailTriggerApp() {
        AylaPropertyTriggerApp triggerApp = new AylaPropertyTriggerApp();
        triggerApp.setEmailAddress(_testEMailAddress);
        AylaEmailTemplate template = new AylaEmailTemplate();
        template.setEmailBodyHtml("this is body");
        triggerApp.configureAsEmail(null, "test", null, template);

        RequestFuture<AylaPropertyTriggerApp> future = RequestFuture.newFuture();
        _propertyTrigger.createApp(triggerApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in AppTrigger creation " + e);
        } catch (ExecutionException e) {
            fail("Error in AppTrigger creation " + e);
        } catch (TimeoutException e) {
            fail("Error in AppTrigger creation " + e);
        }
        assertNotNull(_triggerApp);
    }

    @Test
    public void testUpdateEmailTriggerApp() {
        //First create a trigger
        AylaPropertyTriggerApp triggerApp = new AylaPropertyTriggerApp();
        triggerApp.setEmailAddress(_testEMailAddress);
        AylaEmailTemplate template = new AylaEmailTemplate();
        template.setEmailBodyHtml("this is body");
        triggerApp.configureAsEmail(null, "test", null, template);

        RequestFuture<AylaPropertyTriggerApp> future = RequestFuture.newFuture();
        _propertyTrigger.createApp(triggerApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in App Creation on Trigger " + e);
        } catch (ExecutionException e) {
            fail("Error in App Creation on Trigger " + e);
        } catch (TimeoutException e) {
            fail("Error in App Creation on Trigger " + e);
        }
        assertNotNull(_triggerApp);

        //Now update the trigger
        RequestFuture<AylaPropertyTriggerApp> updatedApp = RequestFuture.newFuture();
        String updatedBodyHTML = "this is updated body";
        template.setEmailBodyHtml(updatedBodyHTML);
        _triggerApp.setEmailTemplate(template);
        _propertyTrigger.updateApp(_triggerApp, updatedApp, updatedApp);
        try {
            int API_TIMEOUT_MS = 20000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in App Update on Trigger " + e);
        } catch (ExecutionException e) {
            fail("Error in App Update on Trigger " + e);
        } catch (TimeoutException e) {
            fail("Error in App Update on Trigger " + e);
        }
        assertNotNull(_triggerApp);
        assertEquals(updatedBodyHTML, _triggerApp.getEmailTemplate().getEmailBodyHtml());
    }

    @Test
    public void testCreateSMSTriggerApp() {
        AylaPropertyTriggerApp triggerApp = new AylaPropertyTriggerApp();
        RequestFuture<AylaContact> futureContact = RequestFuture.newFuture();

        String countryCode = "1";
        AylaContact contact = new AylaContact();
        contact.setPhoneCountryCode(countryCode);
        contact.setPhoneNumber(_testPhoneNumber);

        _sessionManager.createContact(contact, futureContact, futureContact);

        try {
            _aylaContact = futureContact.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in contact creation " + e);
        } catch (ExecutionException e) {
            fail("Error in contact creation " + e);
        } catch (TimeoutException e) {
            fail("Error in contact creation " + e);
        }
        assertNotNull(_aylaContact);

        triggerApp.configureAsSMS(_aylaContact, "SMS trigger Message");

        RequestFuture<AylaPropertyTriggerApp> future = RequestFuture.newFuture();
        _propertyTrigger.createApp(triggerApp, future, future);
        try {
            int API_TIMEOUT_MS = 90000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in App Creation on Trigger " + e);
        } catch (ExecutionException e) {
            fail("Error in App Creation on Trigger " + e);
        } catch (TimeoutException e) {
            fail("Error in App Creation on Trigger " + e);
        }
        assertNotNull(_triggerApp);
        assertEquals(_triggerApp.getPhoneNumber(), _testPhoneNumber);
        assertEquals(_triggerApp.getCountryCode(), countryCode);
    }

    @Test
    public void testUpdateSMSTriggerApp() {
        //First create a trigger
        AylaPropertyTriggerApp triggerApp = new AylaPropertyTriggerApp();
        String countryCode = "1";
        String msg = "SMS trigger Message";
        String username = "SMS User";
        triggerApp.configureAsSMS(countryCode, _testPhoneNumber, username, msg);

        RequestFuture<AylaPropertyTriggerApp> future = RequestFuture.newFuture();
        _propertyTrigger.createApp(triggerApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in App Creation on Trigger " + e);
        } catch (ExecutionException e) {
            fail("Error in App Creation on Trigger " + e);
        } catch (TimeoutException e) {
            fail("Error in App Creation on Trigger " + e);
        }
        assertNotNull(_triggerApp);
        assertEquals(_triggerApp.getCountryCode(),countryCode);
        assertEquals(_triggerApp.getPhoneNumber(),_testPhoneNumber);
        assertEquals(_triggerApp.getMessage(),msg);

        //Now update the trigger
        RequestFuture<AylaPropertyTriggerApp> updatedApp = RequestFuture.newFuture();
        String updateMsg="Updated SMS Message";
        _triggerApp.configureAsSMS(countryCode, _testPhoneNumber, username, updateMsg);

        _propertyTrigger.updateApp(_triggerApp, updatedApp, updatedApp);
        try {
            int API_TIMEOUT_MS = 20000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in App Update on Trigger " + e);
        } catch (ExecutionException e) {
            fail("Error in App Update on Trigger " + e);
        } catch (TimeoutException e) {
            fail("Error in App Update on Trigger " + e);
        }
        assertNotNull(_triggerApp);
        assertEquals(_triggerApp.getCountryCode(),countryCode);
        assertEquals(_triggerApp.getPhoneNumber(),_testPhoneNumber);
        assertEquals(_triggerApp.getMessage(), updateMsg);
    }

    //fetch all Triggers
    @Test
    public void testFetchAllAppTriggers() {
        AylaPropertyTriggerApp triggerApp = new AylaPropertyTriggerApp();
        triggerApp.setEmailAddress(_testEMailAddress);
        AylaEmailTemplate template = new AylaEmailTemplate();
        template.setEmailBodyHtml("this is body");
        triggerApp.configureAsEmail(null, "test", null, template);

        RequestFuture<AylaPropertyTriggerApp> future = RequestFuture.newFuture();
        _propertyTrigger.createApp(triggerApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _triggerApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in AppTrigger creation " + e);
        } catch (ExecutionException e) {
            fail("Error in AppTrigger creation " + e);
        } catch (TimeoutException e) {
            fail("Error in AppTrigger creation " + e);
        }
        assertNotNull(_triggerApp);

        RequestFuture<AylaPropertyTriggerApp[]> futureFetch = RequestFuture.newFuture();
        _propertyTrigger.fetchApps(futureFetch, futureFetch);
        AylaPropertyTriggerApp[] triggers = null;
        try {
            triggers = futureFetch.get(TestConstants.API_TIMEOUT_MS, TimeUnit
                    .MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Fetch AppTrigger " + e);
        } catch (ExecutionException e) {
            fail("Error in Fetch AppTrigger " + e);
        } catch (TimeoutException e) {
            fail("Error in Fetch AppTrigger " + e);
        }
        assertNotNull(triggers);
    }

    @After
    public void tearDown() throws Exception {
        if (_triggerApp != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
            _propertyTrigger.deleteApp(_triggerApp, futureDelete, futureDelete);
            try {
                futureDelete.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Error in propertyTrigger delete " + e);
            } catch (ExecutionException e) {
                fail("Error in propertyTrigger delete " + e);
            } catch (TimeoutException e) {
                fail("Error in propertyTrigger delete " + e);
            }
        }
        if (_propertyTrigger != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
            _property.deleteTrigger(_propertyTrigger, futureDelete, futureDelete);
            try {
                futureDelete.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Error in propertyTrigger delete " + e);
            } catch (ExecutionException e) {
                fail("Error in propertyTrigger delete " + e);
            } catch (TimeoutException e) {
                fail("Error in propertyTrigger delete " + e);
            }
        }
        if (_aylaContact != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
            _sessionManager.deleteContact(_aylaContact, futureDelete, futureDelete);
            try {
                futureDelete.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Error in delete contact " + e);
            } catch (ExecutionException e) {
                fail("Error in delete contact " + e);
            } catch (TimeoutException e) {
                fail("Error in delete contact " + e);
            }
        }
    }
}

