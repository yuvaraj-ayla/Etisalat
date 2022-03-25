package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.auth.CachedAuthProvider;
import com.aylanetworks.aylasdk.auth.UsernameAuthProvider;
import com.aylanetworks.aylasdk.error.AuthError;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class SignInTest {
    @Before
    public void setUp() throws Exception {

        AylaSystemSettings systemSettings =
                new AylaSystemSettings(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);

        // We need to set our context
        systemSettings.context = InstrumentationRegistry.getContext();

        assertNotNull(systemSettings.context);
        AylaNetworks.initialize(systemSettings);

        AylaNetworks cm = AylaNetworks.sharedInstance();
        assertNotNull(cm);
    }

    @Test
    public void testSignIn() {
        // Use a RequestFuture to make our call synchronous
        RequestFuture<AylaAuthorization> future = RequestFuture.newFuture();

        AylaLoginManager loginManager = AylaNetworks.sharedInstance().getLoginManager();

        UsernameAuthProvider authProvider = new UsernameAuthProvider(TestConstants.TEST_USERNAME,
                TestConstants.TEST_PASSWORD);

        loginManager.signIn(authProvider, TestConstants.TEST_SESSION_NAME, future, future);

        AylaAuthorization auth = null;

        try {
            auth = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }

        // Make sure we have a user and an access token and a refresh token.
        assertNotNull("No authorization returned", auth);
        assertNotNull("Authorization has no access token", auth.getAccessToken());
        assertNotNull("Authorization has no refresh token", auth.getRefreshToken());
    }

    @Test
    public void testSignInFail() {
        RequestFuture<AylaAuthorization> future = RequestFuture.newFuture();

        AylaLoginManager loginManager = AylaNetworks.sharedInstance().getLoginManager();
        assertNotNull("No login manager found", loginManager);

        UsernameAuthProvider authProvider = new UsernameAuthProvider(TestConstants.TEST_USERNAME,
                TestConstants.TEST_BAD_PASSWORD);

        loginManager.signIn(authProvider, TestConstants.TEST_SESSION_NAME, future, future);

        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            try {
                // We're looking for an AuthFailureError. Let's try. Since we are using a Future
                // to wait for the results, the exception will actually be the inner exception of
                // an ExecutionException.
                //
                // We will try to cast the inner error, and if that works, we're done!
                //
                // Inspection ignored because we don't intent to use this object, just to see if
                // the cast works or throws an exception.

                AuthError authError = (AuthError) e.getCause();
            } catch (ClassCastException classCastException) {
                fail("Returned error was not an AuthFailureError:" + e.getCause().getClass().toString());
                return;
            }
            // Success!
            return;
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }
        fail("Expected an AuthFailureError");
    }

    @Test
    public void testRefreshToken() {
        // First sign in:
        // Use a RequestFuture to make our call synchronous
        RequestFuture<AylaAuthorization> future = RequestFuture.newFuture();

        AylaLoginManager loginManager = AylaNetworks.sharedInstance().getLoginManager();

        UsernameAuthProvider authProvider = new UsernameAuthProvider(TestConstants.TEST_USERNAME,
                TestConstants.TEST_PASSWORD);

        loginManager.signIn(authProvider, TestConstants.TEST_SESSION_NAME, future, future);

        AylaAuthorization auth = null;

        try {
            auth = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }

        // Now try to refresh our token
        future = RequestFuture.newFuture();

        AylaSessionManager sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        assertNotNull(sessionManager);
        
        sessionManager.refreshAuthorization(future, future);
        AylaAuthorization newAuth = null;
        try {
            newAuth = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }
        assertNotNull(newAuth);
    }

    @Test
    public void testCachedAuthProvider() {
        // First sign in:
        // Use a RequestFuture to make our call synchronous
        RequestFuture<AylaAuthorization> future = RequestFuture.newFuture();

        AylaLoginManager loginManager = AylaNetworks.sharedInstance().getLoginManager();

        UsernameAuthProvider authProvider = new UsernameAuthProvider(TestConstants.TEST_USERNAME,
                TestConstants.TEST_PASSWORD);

        loginManager.signIn(authProvider, TestConstants.TEST_SESSION_NAME, future, future);

        AylaAuthorization auth = null;

        try {
            auth = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }

        // Now try to sign in using a CachedAuthProvider object created with the
        // credentials we just got from a normal sign-in.
        CachedAuthProvider cachedAuthProvider = new CachedAuthProvider(auth);

        loginManager.signIn(cachedAuthProvider, TestConstants.TEST_SESSION_NAME, future, future);
        try {
            auth = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }

        // Now test the auto-caching feature of CachedAuthProvider
        CachedAuthProvider.cacheAuthorization(InstrumentationRegistry.getContext(), auth);

        cachedAuthProvider = CachedAuthProvider.getCachedProvider(InstrumentationRegistry.getContext());
        assertNotNull(cachedAuthProvider);
        loginManager.signIn(cachedAuthProvider, TestConstants.TEST_SESSION_NAME, future, future);
        try {
            auth = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Interrupted exception in sign-in: " + e.toString());
        } catch (ExecutionException e) {
            fail("Execution exception in sign-in: " + e.toString());
        } catch (TimeoutException e) {
            fail("Sign-in request timed out: " + e.toString());
        }
    }
}
