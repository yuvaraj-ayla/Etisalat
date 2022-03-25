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
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AylaDeviceNotificationAppTest  {
    private final String _testEMailAddress = "raghu@aylanetworks.com";
    private AylaDevice _device;
    private AylaDeviceNotification _deviceNotification;
    private AylaDeviceNotificationApp _deviceNotificationApp;

    @Before
    public void setUp() throws Exception {
        TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();

        _device = dm.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(_device);
        //First create a Device Notification
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

    //This test will first create an email DeviceNotification App and then update it
    @Test
    public void testCreateUpdateEmailDeviceNotificationApp() {
        //First create a new email DeviceNotificationApp
        String nickName = "myEmailNickname";
        AylaDeviceNotificationApp notificationApp = new AylaDeviceNotificationApp();
        notificationApp.setEmailAddress(_testEMailAddress);
        //setting nickname is optional
        notificationApp.setNickname(nickName);

        AylaEmailTemplate template = new AylaEmailTemplate();
        String emailTemplateId = "myTemplateId";
        String emailSubject = "myemailSubject";
        String emailBodyHTML = "myemailBodyHTML";
        String message = "test message";
        String user = "testUser";

        template.setEmailTemplateId(emailTemplateId);
        template.setEmailSubject(emailSubject);
        template.setEmailBodyHtml(emailBodyHTML);

        notificationApp.configureAsEmail(null, message, user, template);

        RequestFuture<AylaDeviceNotificationApp> future = RequestFuture.newFuture();
        _deviceNotification.createApp(notificationApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);
        //Make sure they are created correctly
        assertEquals(_deviceNotificationApp.getEmailAddress(), _testEMailAddress);
        assertEquals(_deviceNotificationApp.getEmailTemplate().getEmailTemplateId(), emailTemplateId);
        assertEquals(_deviceNotificationApp.getEmailTemplate().getEmailBodyHtml(), emailBodyHTML);
        assertEquals(_deviceNotificationApp.getMessage(), message);
        assertEquals(_deviceNotificationApp.getUsername(), user);

        //Now update the DeviceNotificationApp
        String updatedEmailBody = "this is updated email body";
        String updateEmailSubject = "updatedEmailSubject";
        String updateMessage = "updated Message";

        AylaEmailTemplate templateUpdated = new AylaEmailTemplate();
        templateUpdated.setEmailBodyHtml(updatedEmailBody);
        templateUpdated.setEmailSubject(updateEmailSubject);

        _deviceNotificationApp.configureAsEmail(null, updateMessage, user, templateUpdated);
        RequestFuture<AylaDeviceNotificationApp> futureUpdate = RequestFuture.newFuture();
        _deviceNotification.updateApp(_deviceNotificationApp, futureUpdate, futureUpdate);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = futureUpdate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);
        //Make sure the values are updated
        assertEquals(_deviceNotificationApp.getEmailTemplate().getEmailBodyHtml(),
                updatedEmailBody);
        assertEquals(_deviceNotificationApp.getEmailTemplate().getEmailSubject(),
                updateEmailSubject);
        assertEquals(_deviceNotificationApp.getMessage(),
                updateMessage);
        assertEquals(_deviceNotificationApp.getUsername(), user);
        assertEquals(_deviceNotificationApp.getEmailAddress(), _testEMailAddress);
    }

    //This test will first create an push Android DeviceNotification App and then update it
    @Test
    public void testCreateUpdatePushDeviceNotificationApp() {
        //First create a Notification app For Push Android
        AylaDeviceNotificationApp notificationApp = new AylaDeviceNotificationApp();
        String registrationId = "112233";
        String message = "MyPushMsg";
        String pushSound = "MyPushSound";
        String pushMData = "MyPushMData";
        String nickName = "nickName1";

        notificationApp.configureAsPushFCM(registrationId, message, pushSound, pushMData);

        //setting nickname is optional
        notificationApp.setNickname(nickName);

        RequestFuture<AylaDeviceNotificationApp> future = RequestFuture.newFuture();
        _deviceNotification.createApp(notificationApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);
        //Make sure they are created correctly
        assertEquals(_deviceNotificationApp.getRegistrationId(), registrationId);
        assertEquals(_deviceNotificationApp.getMessage(), message);
        assertEquals(_deviceNotificationApp.getPushSound(), pushSound);
        assertEquals(_deviceNotificationApp.getPushMdata(), pushMData);
        assertEquals(_deviceNotificationApp.getNickname(), nickName);
        //Now update the App. Change PushSound and PushMData
        future = RequestFuture.newFuture();
        String updatedPushSound = "updatedSound";
        String updatedPushMdata = "updatedMdata";

        _deviceNotificationApp.configureAsPushFCM(registrationId, message, updatedPushSound, updatedPushMdata);

        _deviceNotification.updateApp(_deviceNotificationApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);
        //Make sure the values are updated
        assertEquals(_deviceNotificationApp.getPushSound(), updatedPushSound);
        assertEquals(_deviceNotificationApp.getPushMdata(), updatedPushMdata);
        assertEquals(_deviceNotificationApp.getRegistrationId(), registrationId);
        assertEquals(_deviceNotificationApp.getMessage(), message);
        assertEquals(_deviceNotificationApp.getNickname(), nickName);
    }

    //This test will first create an SMS DeviceNotification App and then update it
    @Test
    public void testCreateUpdateSMSDeviceNotificationApp() {
        //First create a Notification app For Push Android
        String nickName = "mySMSNickname";
        String countryCode = "1";
        String phoneNumber = "8005000000";
        String message = "SMS Device Message";
        String uName = "test user";

        AylaDeviceNotificationApp notificationApp = new AylaDeviceNotificationApp();
        notificationApp.configureAsSMS(countryCode, phoneNumber,uName,message);
        //setting nickname is optional
        notificationApp.setNickname(nickName);

        RequestFuture<AylaDeviceNotificationApp> future = RequestFuture.newFuture();
        _deviceNotification.createApp(notificationApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);
        //Make sure they are created correctly
        assertEquals(_deviceNotificationApp.getNickname(), nickName);
        assertEquals(_deviceNotificationApp.getCountryCode(), countryCode);
        assertEquals(_deviceNotificationApp.getPhoneNumber(), phoneNumber);
        assertEquals(_deviceNotificationApp.getMessage(), message);
        assertEquals(_deviceNotificationApp.getUsername(), uName);
        //Now update the App. Change Message and Phone Number
        future = RequestFuture.newFuture();
        String updatedMessage = "updatedSMSMessage";
        String updatedCountryCode = "2";
        String updatedPhoneNumber = "8008000000";

        _deviceNotificationApp.configureAsSMS(updatedCountryCode, updatedPhoneNumber,uName,
                updatedMessage);
        _deviceNotification.updateApp(_deviceNotificationApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);
        //Make sure the values are updated
        assertEquals(_deviceNotificationApp.getMessage(), updatedMessage);
        assertEquals(_deviceNotificationApp.getPhoneNumber(), updatedPhoneNumber);
        assertEquals(_deviceNotificationApp.getCountryCode(), updatedCountryCode);
        assertEquals(_deviceNotificationApp.getNickname(), nickName);
        assertEquals(_deviceNotificationApp.getUsername(), uName);
    }

    //fetch all Notifications
    @Test
    public void testFetchAllAppNotifications() {
        AylaDeviceNotificationApp notificationApp = new AylaDeviceNotificationApp();
        notificationApp.setEmailAddress(_testEMailAddress);
        AylaEmailTemplate template = new AylaEmailTemplate();
        template.setEmailBodyHtml("this is body");
        notificationApp.configureAsEmail(null, "test", null, template);

        RequestFuture<AylaDeviceNotificationApp> future = RequestFuture.newFuture();
        _deviceNotification.createApp(notificationApp, future, future);
        try {
            int API_TIMEOUT_MS = 20000;
            _deviceNotificationApp = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (ExecutionException e) {
            fail("Error in DeviceNotification creation " + e);
        } catch (TimeoutException e) {
            fail("Error in DeviceNotification creation " + e);
        }
        assertNotNull(_deviceNotificationApp);

        RequestFuture<AylaDeviceNotificationApp[]> futureFetch = RequestFuture.newFuture();
        AylaDeviceNotificationApp[] notifications = null;
        _deviceNotification.fetchApps(futureFetch, futureFetch);
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
        if (_deviceNotificationApp != null && _deviceNotification != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
            _deviceNotification.deleteApp(_deviceNotificationApp, futureDelete, futureDelete);
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

