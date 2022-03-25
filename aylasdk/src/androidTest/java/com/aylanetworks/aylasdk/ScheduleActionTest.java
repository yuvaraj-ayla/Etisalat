package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */


import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;

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
public class ScheduleActionTest {
    private AylaSchedule _schedule;

    @Before
    public void setUp() throws Exception {
        TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();

        AylaDevice device;
        device = dm.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(device);
        AylaSchedule[] aylaSchedules = null;
        RequestFuture<AylaSchedule[]> futureFetch = RequestFuture.newFuture();
        device.fetchSchedules(futureFetch, futureFetch);
        try {
            int API_TIMEOUT_MS = 20000;
            aylaSchedules = futureFetch.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Fetch " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Fetch " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Fetch " + e);
        }
        assertNotNull(aylaSchedules);
        assert (aylaSchedules.length > 0);
        _schedule = aylaSchedules[0];
    }

    //Update Schedule Actions
    @Test
    public void testUpdateActions() {
        //First we create a new Schedule action
        AylaScheduleAction aylaScheduleAction = new AylaScheduleAction();
        aylaScheduleAction.setName("Green_LED");
        aylaScheduleAction.setBaseType("boolean");
        aylaScheduleAction.setValue("1");
        aylaScheduleAction.setScheduleActionFirePoint(AylaScheduleAction.
                AylaScheduleActionFirePoint.InRange);
        aylaScheduleAction.setType("SchedulePropertyAction");
        AylaScheduleAction createdAction = null;
        RequestFuture<AylaScheduleAction> futureCreate = RequestFuture.newFuture();
        _schedule.createAction(aylaScheduleAction, futureCreate, futureCreate);
        try {
            int API_TIMEOUT_MS = 30000;
            createdAction = futureCreate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action Create " + e);
        }
        assertNotNull(createdAction);

        //Create one more Schedule action
        aylaScheduleAction = new AylaScheduleAction();
        aylaScheduleAction.setName("Blue_LED");
        aylaScheduleAction.setBaseType("boolean");
        aylaScheduleAction.setValue("1");
        aylaScheduleAction.setScheduleActionFirePoint(AylaScheduleAction.
                AylaScheduleActionFirePoint.InRange);
        aylaScheduleAction.setType("SchedulePropertyAction");

        futureCreate = RequestFuture.newFuture();
        _schedule.createAction(aylaScheduleAction, futureCreate, futureCreate);
        try {
            int API_TIMEOUT_MS = 30000;
            createdAction = futureCreate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action Create " + e);
        }
        assertNotNull(createdAction);
        assertEquals(createdAction.getName(),"Blue_LED");

        //Now fetch all actions including the 2 newly created ones
        AylaScheduleAction[] aylaScheduleActions = null;
        RequestFuture<AylaScheduleAction[]> futureFetch = RequestFuture.newFuture();
        _schedule.fetchActions(futureFetch, futureFetch);
        try {
            int API_TIMEOUT_MS = 20000;
            aylaScheduleActions = futureFetch.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action Fetch " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action Fetch " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action Fetch " + e);
        }
        assertNotNull(aylaScheduleActions);
        assert (aylaScheduleActions.length > 0);
        //Now update the actions
        if (aylaScheduleActions.length > 0) {
            AylaScheduleAction updatedActions[] = null;
            RequestFuture<AylaScheduleAction[]> futureUpdate = RequestFuture.newFuture();
            for (AylaScheduleAction scheduleAction : aylaScheduleActions) {
                //Just toggle the value field
                if (scheduleAction.getValue().equalsIgnoreCase("1")) {
                    scheduleAction.setValue("0");
                } else {
                    scheduleAction.setValue("1");
                }
            }
            _schedule.updateActions(aylaScheduleActions, futureUpdate, futureUpdate);
            try {
                int API_TIMEOUT_MS = 30000;
                updatedActions = futureUpdate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Error in Schedule Update " + e);
            } catch (ExecutionException e) {
                fail("Error in Schedule Update " + e);
            } catch (TimeoutException e) {
                fail("Error in Schedule Update " + e);
            }
            assertNotNull(updatedActions);
        }
    }

    //Delete Schedule Action
    @Test
    public void testDeleteAction() {
        //First we create a new Schedule action
        AylaScheduleAction aylaScheduleAction = new AylaScheduleAction();
        aylaScheduleAction.setName("Green_LED");
        aylaScheduleAction.setBaseType("boolean");
        aylaScheduleAction.setValue("1");
        aylaScheduleAction.setScheduleActionFirePoint(AylaScheduleAction.
                AylaScheduleActionFirePoint.InRange);
        aylaScheduleAction.setType("SchedulePropertyAction");
        AylaScheduleAction createdAction = null;
        RequestFuture<AylaScheduleAction> futureCreate = RequestFuture.newFuture();
        _schedule.createAction(aylaScheduleAction, futureCreate, futureCreate);
        try {
            int API_TIMEOUT_MS = 30000;
            createdAction = futureCreate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action Create " + e);
        }
        assertNotNull(createdAction);

        //Now delete this action
        RequestFuture<AylaAPIRequest.EmptyResponse> futureDeleteAction = RequestFuture
                .newFuture();
        _schedule.deleteAction(createdAction, futureDeleteAction, futureDeleteAction, null);
        try {
            futureDeleteAction.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action delete " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action delete " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action delete " + e);
        }
    }

    //Delete Schedule Action
    @Test
    public void testDeleteAllActions() {
        //First we create a new Schedule action
        AylaScheduleAction aylaScheduleAction = new AylaScheduleAction();
        aylaScheduleAction.setName("Green_LED");
        aylaScheduleAction.setBaseType("boolean");
        aylaScheduleAction.setValue("1");
        aylaScheduleAction.setScheduleActionFirePoint(AylaScheduleAction.
                AylaScheduleActionFirePoint.InRange);
        aylaScheduleAction.setType("SchedulePropertyAction");
        AylaScheduleAction createdAction = null;
        RequestFuture<AylaScheduleAction> futureCreate = RequestFuture.newFuture();
        _schedule.createAction(aylaScheduleAction, futureCreate, futureCreate);
        try {
            int API_TIMEOUT_MS = 30000;
            createdAction = futureCreate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action Create " + e);
        }
        assertNotNull(createdAction);

        //Then we create a second schedule action
        aylaScheduleAction = new AylaScheduleAction();
        aylaScheduleAction.setName("Blue_LED");
        aylaScheduleAction.setBaseType("boolean");
        aylaScheduleAction.setValue("1");
        aylaScheduleAction.setScheduleActionFirePoint(AylaScheduleAction.
                AylaScheduleActionFirePoint.InRange);
        aylaScheduleAction.setType("SchedulePropertyAction");
        futureCreate = RequestFuture.newFuture();
        _schedule.createAction(aylaScheduleAction, futureCreate, futureCreate);
        try {
            int API_TIMEOUT_MS = 30000;
            createdAction = futureCreate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action Create " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action Create " + e);
        }
        assertNotNull(createdAction);

        //Now delete all actions including the newly created one
        RequestFuture<AylaAPIRequest.EmptyResponse> futureDeleteAction = RequestFuture
                .newFuture();
        _schedule.deleteAllActions(futureDeleteAction, futureDeleteAction);
        try {
            futureDeleteAction.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Action delete " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Action delete " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Action delete " + e);
        }
    }
}