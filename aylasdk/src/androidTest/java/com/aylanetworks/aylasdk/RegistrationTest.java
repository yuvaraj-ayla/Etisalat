package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;

import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.setup.AylaRegistration;
import com.aylanetworks.aylasdk.setup.AylaRegistrationCandidate;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


public class RegistrationTest {

    @Before
    public void setUp() {
        TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
    }

    @Test
    public void testSameLanRegistration() {
        if (true) {
            // FIXME: skip as no registration candidate device available in the cloud
            // Remove this block if there is a candidate with same-LAN registration method is ready
            return;
        }

        //For Same Lan registration 2 calls are needed
        //First call fetchRegistrationCandidate then call registerCandidate

        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        AylaRegistration aylaRegistration = dm.getAylaRegistration();
        RequestFuture<AylaRegistrationCandidate> futureRegCandidate = RequestFuture.newFuture();
        aylaRegistration.fetchCandidate(TestConstants.TEST_REGISTER_SAME_LAN_DSN,
                AylaDevice.RegistrationType.SameLan,
                futureRegCandidate,
                futureRegCandidate);

        AylaRegistrationCandidate registrationCandidateDevice = null;
        try {
            int API_TIMEOUT_MS = 20000;
            registrationCandidateDevice = futureRegCandidate.get(API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("fetchRegistrationCandidate SameLan interrupted: " + e);
        } catch (ExecutionException e) {
            fail("fetchRegistrationCandidate SameLan execution exception: " + e);
        } catch (TimeoutException e) {
            fail("fetchRegistrationCandidate SameLan timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
        assertNotNull(registrationCandidateDevice);

        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        aylaRegistration.registerCandidate(registrationCandidateDevice, future, future);

        AylaDevice regDevice = null;
        try {
            int API_TIMEOUT_MS = 20000;
            regDevice = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("registerCandidate SameLan interrupted: " + e);
        } catch (ExecutionException e) {
            fail("registerCandidate SameLan execution exception: " + e);
        } catch (TimeoutException e) {
            fail("registerCandidate SameLan timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
        assertNotNull(regDevice);

        RequestFuture<AylaAPIRequest.EmptyResponse> futureUnregister = RequestFuture.newFuture();
        regDevice.unregister(futureUnregister, futureUnregister);

        try {
            int API_TIMEOUT_MS = 15000;
            futureUnregister.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("UnRegisterNewDevice SameLan interrupted: " + e);
        } catch (ExecutionException e) {
            fail("UnRegisterNewDevice SameLan execution exception: " + e);
        } catch (TimeoutException e) {
            fail("UnRegisterNewDevice SameLan timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
    }

    @Test
    public void testButtonPushRegistration() {
        if (true) {
            // FIXME: skip as no registration candidate device available in the cloud
            // Remove this block if there is a candidate with Button-Push registration method is ready
            return;
        }

        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        AylaRegistration aylaRegistration = dm.getAylaRegistration();
        RequestFuture<AylaRegistrationCandidate> futureRegCandidate = RequestFuture.newFuture();
        aylaRegistration.fetchCandidate(TestConstants.TEST_REGISTER_BUTTON_PUSH,
                AylaDevice.RegistrationType.ButtonPush,
                futureRegCandidate,
                futureRegCandidate);

        AylaRegistrationCandidate registrationCandidateDevice = null;
        try {
            int API_TIMEOUT_MS = 20000;
            registrationCandidateDevice = futureRegCandidate.get(API_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("fetchRegistrationCandidate ButtonPush interrupted: " + e);
        } catch (ExecutionException e) {
            fail("fetchRegistrationCandidate ButtonPush execution exception: " + e);
        } catch (TimeoutException e) {
            fail("fetchRegistrationCandidate ButtonPush timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
        assertNotNull(registrationCandidateDevice);

        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        aylaRegistration.registerCandidate(registrationCandidateDevice, future, future);

        AylaDevice regDevice = null;
        try {
            int API_TIMEOUT_MS = 20000;
            regDevice = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("registerCandidate ButtonPush interrupted: " + e);
        } catch (ExecutionException e) {
            fail("registerCandidate ButtonPush execution exception: " + e);
        } catch (TimeoutException e) {
            fail("registerCandidate ButtonPush timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
        assertNotNull(regDevice);

        RequestFuture<AylaAPIRequest.EmptyResponse> futureUnregister = RequestFuture.newFuture();
        regDevice.unregister(futureUnregister, futureUnregister);

        try {
            int API_TIMEOUT_MS = 15000;
            futureUnregister.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("UnRegisterNewDevice ButtonPush interrupted: " + e);
        } catch (ExecutionException e) {
            fail("UnRegisterNewDevice ButtonPush execution exception: " + e);
        } catch (TimeoutException e) {
            fail("UnRegisterNewDevice ButtonPush timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
    }

    @Test
    public void testAPModeRegistration() {
        if (true) {
            // FIXME: skip as no registration candidate device available in the cloud
            // Remove this block if there is a candidate with AP-Mode registration method is ready
            return;
        }

        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        AylaRegistration aylaRegistration = dm.getAylaRegistration();

        AylaRegistrationCandidate device = new AylaRegistrationCandidate();
        device.setRegistrationType(AylaDevice.RegistrationType.APMode);
        device.setDsn(TestConstants.TEST_REGISTER_AP_MODE);
        device.setSetupToken(randomToken(8));

        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        aylaRegistration.registerCandidate(device, future, future);

        AylaDevice regDevice = null;
        try {
            int API_TIMEOUT_MS = 20000;
            regDevice = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("registerCandidate APMode interrupted: " + e);
        } catch (ExecutionException e) {
            fail("registerCandidate APMode execution exception: " + e);
        } catch (TimeoutException e) {
            fail("registerCandidate APMode timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
        assertNotNull(regDevice);

        RequestFuture<AylaAPIRequest.EmptyResponse> futureUnregister = RequestFuture.newFuture();
        regDevice.unregister(futureUnregister, futureUnregister);

        try {
            int API_TIMEOUT_MS = 15000;
            futureUnregister.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("UnRegisterNewDevice APMode interrupted: " + e);
        } catch (ExecutionException e) {
            fail("UnRegisterNewDevice APMode execution exception: " + e);
        } catch (TimeoutException e) {
            fail("UnRegisterNewDevice APMode timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
    }

    @Test
    public void testDSNRegistration() {
        if (true) {
            // FIXME: skip as no registration candidate device available in the cloud
            // Remove this block if there is a candidate with DSN registration method is ready
            return;
        }

        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        AylaRegistration aylaRegistration = dm.getAylaRegistration();

        AylaRegistrationCandidate device = new AylaRegistrationCandidate();
        device.setRegistrationType(AylaDevice.RegistrationType.DSN);
        device.setDsn(TestConstants.TEST_REGISTER_DSN);

        RequestFuture<AylaDevice> future = RequestFuture.newFuture();
        aylaRegistration.registerCandidate(device, future, future);

        AylaDevice regDevice = null;
        try {
            int API_TIMEOUT_MS = 20000;
            regDevice = future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("registerCandidate DSN interrupted: " + e);
        } catch (ExecutionException e) {
            fail("registerCandidate DSN execution exception: " + e);
        } catch (TimeoutException e) {
            fail("registerCandidate DSN timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
        assertNotNull(regDevice);

        RequestFuture<AylaAPIRequest.EmptyResponse> futureUnregister = RequestFuture.newFuture();
        regDevice.unregister(futureUnregister, futureUnregister);

        try {
            int API_TIMEOUT_MS = 15000;
            futureUnregister.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("UnRegisterNewDevice DSN interrupted: " + e);
        } catch (ExecutionException e) {
            fail("UnRegisterNewDevice DSN execution exception: " + e);
        } catch (TimeoutException e) {
            fail("UnRegisterNewDevice DSN timed out");
        } catch (Exception e) {
            fail("general exception:" + e);
        }
    }

    static String randomToken(int length) {
        String token = "";
        char c;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        for (int i = 0; i < length; i++) {
            c = chars.charAt((int) (Math.random() * chars.length()));
            token += c;
        }

        return token;
    }
}
