package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.AylaAPIRequest.EmptyResponse;
import com.aylanetworks.aylasdk.ams.dest.AylaDestination;
import com.aylanetworks.aylasdk.ams.dest.AylaEmailDestination;
import com.aylanetworks.aylasdk.ams.dest.AylaPushDestination;
import com.aylanetworks.aylasdk.ams.dest.AylaSmsDestination;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class DestinationsTest {

    @Before
    public void setUp() throws Exception {
        boolean isSignedIn = TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue("Failed to sign-in", isSignedIn);
        AylaSessionManager sm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        assertNotNull(sm);

        TestConstants.waitForDeviceManagerInitComplete();
        AylaDeviceManager dm = sm.getDeviceManager();
        assertNotNull(dm);
    }

    @Test
    public void testCreateSmsDestination() {
        AylaSmsDestination dst = newSmsDestination();
        RequestFuture<AylaDestination> future = RequestFuture.newFuture();
        getMessageService().createDestination(dst, future, future);
        try {
            dst = (AylaSmsDestination) future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(dst);
            assertNotNull(dst.getUUID());

            RequestFuture<EmptyResponse> newFuture = RequestFuture.newFuture();
            getMessageService().deleteDestinationWithUUID(dst.getUUID(), newFuture, newFuture);
            EmptyResponse response = newFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNull(response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateEmailDestination() {
        AylaEmailDestination dst = newEmailDestination();
        RequestFuture<AylaDestination> future = RequestFuture.newFuture();
        getMessageService().createDestination(dst, future, future);
        try {
            dst = (AylaEmailDestination) future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(dst);
            assertNotNull(dst.getUUID());

            RequestFuture<EmptyResponse> newFuture = RequestFuture.newFuture();
            getMessageService().deleteDestinationWithUUID(dst.getUUID(), newFuture, newFuture);
            EmptyResponse response = newFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNull(response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreatePushDestination() {
        AylaPushDestination dst = newPushDestination();
        RequestFuture<AylaDestination> future = RequestFuture.newFuture();
        getMessageService().createDestination(dst, future, future);
        try {
            dst = (AylaPushDestination) future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(dst);
            assertNotNull(dst.getUUID());

            RequestFuture<EmptyResponse> newFuture = RequestFuture.newFuture();
            getMessageService().deleteDestinationWithUUID(dst.getUUID(), newFuture, newFuture);
            EmptyResponse response = newFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNull(response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUpdatePushDestination() {
        AylaPushDestination newDestination = newPushDestination();
        RequestFuture<AylaDestination> future = RequestFuture.newFuture();
        getMessageService().createDestination(newDestination, future, future);
        try {
            newDestination = (AylaPushDestination) future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(newDestination);
            assertNotNull(newDestination.getUUID());

            AylaPushDestination destination = new AylaPushDestination();
            destination.updateUuidFrom(newDestination); // specify which destination to be updated
            destination.setSound("nature");  // set fields to update.
            getMessageService().updateDestination(destination, future, future);
            AylaPushDestination updatedDst = (AylaPushDestination) future.get(
                    TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertEquals(newDestination.getSound(), updatedDst.getSound());

            RequestFuture<EmptyResponse> newFuture = RequestFuture.newFuture();
            getMessageService().deleteDestinationWithUUID(newDestination.getUUID(), newFuture, newFuture);
            EmptyResponse response = newFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNull(response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchDestinationWithId() {
        AylaPushDestination dst = newPushDestination();
        RequestFuture<AylaDestination> future = RequestFuture.newFuture();
        getMessageService().createDestination(dst, future, future);
        try {
            dst = (AylaPushDestination) future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(dst);
            assertNotNull(dst.getUUID());

            getMessageService().fetchDestinationWithId(dst.getUUID(), future, future);
            AylaPushDestination fetchedDst = (AylaPushDestination) future.get(
                    TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertEquals(dst.getUUID(), fetchedDst.getUUID());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchDestinationWithTypes() {
        String[] types = new String[] {"push"};
        RequestFuture<AylaDestination[]> future = RequestFuture.newFuture();
        getMessageService().fetchDestinationsWithTypes(types, future, future);
        try {
            AylaDestination[] response = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(response);

            getMessageService().fetchDestinationsWithTypes(null, future, future);
            response = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(response);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    static AylaPushDestination newPushDestination() {
        return new AylaPushDestination(
                "fcm",
                "fxQVcSTqTNKGvMxYV7L9mf:APA91bG9akR8mpCyYVh0fbRG8jCmJGWKNQnq2jpqbXvT9g09ryR4BBo0fRhiBYBxbL9Dzj_x9jIKYfXtE9flT41tn8XtdLgWRvRPf7uBdXwCHPQ4oJ_BLHeAsoPO6GoKpkLAVC3VVLL0",
                "test push message title",
                "test push message body",
                "chime");
    }

    static AylaEmailDestination newEmailDestination() {
        return new AylaEmailDestination(
                "your.name@test.com",
                "Hello Ayla Destination Message",
                "test email message body",
                "Hello Ayla User",
                "Hello Ayla Destination Message");
    }

    static AylaSmsDestination newSmsDestination() {
        return new AylaSmsDestination(
                "18665969857",
                "+86",
                "test sms message title",
                "test sms message body");
    }

    private AylaMessageService getMessageService() {
        AylaSessionManager sm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        return sm.getMessageService();
    }

}