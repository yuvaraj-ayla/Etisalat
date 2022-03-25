package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;

import com.aylanetworks.aylasdk.error.InvalidArgumentError;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;

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
public class EmailUpdateTest {

    private static final String TEST_USERNAME_INVALID = "";

    @Before
    public void setUp() {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue("Failed to sign-in", isSignedIn);
    }

    @Test
    public void testUpdateUserEmailAddress() {
        AylaSessionManager sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        assertNotNull(sessionManager);

        RequestFuture<AylaAPIRequest.EmptyResponse> future = RequestFuture.newFuture();
        sessionManager.updateUserEmailAddress(TEST_USERNAME_INVALID, future, future);
        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("updateUserEmailAddress interrupted: " + e);
        } catch (ExecutionException e) {
            // as we don't really won't to update the email address, so it's supposed to
            // return InvalidArgumentError for the new invalid email address.
            Throwable cause = e.getCause();
            if (cause != null && !InvalidArgumentError.class.isInstance(cause)) {
                fail("updateUserEmailAddress execution exception: " + e);
            }
        } catch (TimeoutException e) {
            fail("updateUserEmailAddress timeout: " + e);
        }
        assertTrue("Update Email success", future.isDone());
    }
}
