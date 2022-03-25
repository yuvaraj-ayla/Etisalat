package com.aylanetworks.aylasdk;/*
 * {PROJECT_NAME}
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
public class UserProfileTest {
    private boolean _isSignedIn;
    private String _originalFirstName;
    private String _originalCompanyName;
    private AylaUser _user;
    private AylaSessionManager _sessionManager;

    @Before
    public void setUp() {
        _isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue("Failed to sign-in", _isSignedIn);
        RequestFuture<AylaUser> future = RequestFuture.newFuture();
        _sessionManager = AylaNetworks.sharedInstance().getSessionManager(TestConstants
                .TEST_SESSION_NAME);
        assertNotNull("Null session manager", _sessionManager);
        _sessionManager.fetchUserProfile(future, future);
        try {
            _user = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("fetchUserProfile interrupted: " + e);
        } catch (ExecutionException e) {
            fail("fetchUserProfile execution exception: " + e);
        } catch (TimeoutException e) {
            fail("fetchUserProfile timed out: " + e);
        }
        assertNotNull("No user profile was fetched", _user);
        // Save the existing details
        _originalFirstName = _user.getFirstname();
        _originalCompanyName = _user.getCompany();
    }

    @Test
    public void testUpdateUserProfile() {

        RequestFuture<AylaUser> future = RequestFuture.newFuture();
        AylaLog.d("testUpdateUserProfile", "User first name: " + _user.getFirstname());
        String testUpdateFirstName = _originalFirstName + "-Updated";
        String testUpdateCompanyName = _originalCompanyName + "-Updated";

        _user.setFirstname(testUpdateFirstName);
        _user.setCompany(testUpdateCompanyName);
        _sessionManager.updateUserProfile(_user, future, future);
        try {
            _user = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("updateUserProfile interrupted: " + e);
        } catch (ExecutionException e) {
            fail("updateUserProfile execution exception: " + e);
        } catch (TimeoutException e) {
            fail("updateUserProfile timed out: " + e);
        }

        // Verify that the returned user has the correct first name
        assertEquals("Returned user did not reflect update", _user.getFirstname(),
                testUpdateFirstName);
        AylaLog.d("testUpdateUserProfile", "New user first name: " + _user.getFirstname());
        // Verify that the returned user has the correct company name
        assertEquals("Returned user did not reflect update", _user.getCompany(),
                testUpdateCompanyName);
        AylaLog.d("testUpdateUserProfile", "New company name: " + _user.getCompany());

        // Fetch the user again to make sure the server really did update
        _sessionManager.fetchUserProfile(future, future);
        AylaUser user = null;
        try {
            user = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("fetchUserProfile interrupted: " + e);
        } catch (ExecutionException e) {
            fail("fetchUserProfile execution exception: " + e);
        } catch (TimeoutException e) {
            fail("fetchUserProfile timed out: " + e);
        }
        assertNotNull(user);
        assertEquals("Subsequent fetch of user profile did not reflect update", user.getFirstname(),
                testUpdateFirstName);
        AylaLog.d("testUpdateUserProfile", "Second fetch first name: " + user.getFirstname());
        assertEquals("Subsequent fetch of user profile did not reflect update", user.getCompany(),
                testUpdateCompanyName);
        AylaLog.d("testUpdateUserProfile", "Second fetch company name: " + user.getCompany());

    }

    @After
    public void tearDown(){
        // Put the user back to what it was originally
        RequestFuture<AylaUser> future = RequestFuture.newFuture();
        _user.setFirstname(_originalFirstName);
        _user.setCompany(_originalCompanyName);
        _sessionManager.updateUserProfile(_user, future, future);
        try {
            _user = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("fetchUserProfile interrupted: " + e);
        } catch (ExecutionException e) {
            fail("fetchUserProfile execution exception: " + e);
        } catch (TimeoutException e) {
            fail("fetchUserProfile timed out: " + e);
        }
        AylaLog.d("testUpdateUserProfile", "Final update first name: " + _user.getFirstname());
        AylaLog.d("testUpdateUserProfile", "Final update company name: " + _user.getCompany());

        assertEquals("Failed to put original username back", _user.getFirstname(),
                _originalFirstName);
        assertEquals("Failed to put original company name back", _user.getCompany(),
                _originalCompanyName);
    }
}
