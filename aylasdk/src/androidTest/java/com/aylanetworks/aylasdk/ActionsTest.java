package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.ams.action.params.AylaDatapointActionParameters;
import com.aylanetworks.aylasdk.ams.action.params.AylaEmailActionParameters;
import com.aylanetworks.aylasdk.ams.action.params.AylaUrlActionParameters;
import com.aylanetworks.aylasdk.ams.dest.AylaDestination;
import com.aylanetworks.aylasdk.ams.dest.AylaEmailDestination;
import com.aylanetworks.aylasdk.ams.dest.AylaPushDestination;
import com.aylanetworks.aylasdk.ams.dest.AylaSmsDestination;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ActionsTest {

    private static final String TEST_DEVICE   = TestConstants.TEST_DEVICE_DSN;
    private static final String TEST_PROPERTY_BLUE_LED = TestConstants.TEST_DEVICE_PROPERTY;

    static final String TEST_SMS_ACTION_NAME  = "__SmsActionTestName__";
    static final String TEST_URL_ACTION_NAME  = "__UrlActionTestName__";
    static final String TEST_PUSH_ACTION_NAME  = "__PushActionTestName__";
    static final String TEST_EMAIL_ACTION_NAME  = "__EmailActionTestName__";
    static final String TEST_DATA_POINT_ACTION_NAME  = "__DPActionTestName__";
    static final String TEST_DATA_POINT_ACTION_NAME_UPDATED  = "__DPActionTestNameUpdated__";
    static final String TEST_DATA_POINT_ACTION_LED_ON = "DATAPOINT(%s, %s) = 1";
    static final String TEST_DATA_POINT_ACTION_LED_OFF = "DATAPOINT(%s, %s) = 0";

    private AylaAction _createdAction;

    @Before
    public void setUp() throws Exception {
        assertTrue(TestConstants.signIn(InstrumentationRegistry.getContext()));
        AylaSessionManager sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        assertNotNull(sessionManager);
        assertNotNull(sessionManager.getRulesService());

        TestConstants.waitForDeviceManagerInitComplete();
        assertNotNull(getDevice(TEST_DEVICE));
        assertNotNull(getDeviceProperty(TEST_DEVICE, TEST_PROPERTY_BLUE_LED));

        // delete created actions in cases of last failure to avoid name
        // conflicts when creating new actions.
        String[] actionNamesToDelete = new String[] {
                TEST_SMS_ACTION_NAME,
                TEST_URL_ACTION_NAME,
                TEST_PUSH_ACTION_NAME,
                TEST_EMAIL_ACTION_NAME,
                TEST_DATA_POINT_ACTION_NAME,
                TEST_DATA_POINT_ACTION_NAME_UPDATED
        };
        deleteActions(actionNamesToDelete);
    }

    @After
    public void tearDown() {
        if (_createdAction != null) {
            ActionsTest.deleteAction(_createdAction);
        }
    }

    @Test
    public void testCreateAndFetchAction() {
        AylaAction newAction = newDatapointAction();
        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(newAction, future, future);
        try {
            _createdAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdAction);
            assertNotNull(_createdAction.getUUID());
            assertEquals(_createdAction.getName(), newAction.getName());
            assertEquals(_createdAction.getType(), newAction.getType());
            assertEquals(((AylaDatapointActionParameters) _createdAction.getParameters()).getDatapoint(),
                    ((AylaDatapointActionParameters) newAction.getParameters()).getDatapoint());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action creation " + e);
        }

        getRulesService().fetchAction(_createdAction.getUUID(), future, future);
        try {
            AylaAction fetchedAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(fetchedAction);
            assertNotNull(fetchedAction.getUUID());
            assertEquals(_createdAction.getUUID(), fetchedAction.getUUID());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action fetch " + e);
        }
    }


    @Test
    public void testCreateAndUpdateAction() {
        AylaAction action = newDatapointAction();
        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(action, future, future);
        try {
            _createdAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdAction);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action creation " + e);
        }

        action = new AylaAction();
        action.updateFrom(_createdAction);
        action.setName(TEST_DATA_POINT_ACTION_NAME_UPDATED);

        future = RequestFuture.newFuture();
        getRulesService().updateAction(action, future, future);
        try {
            AylaAction updatedAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(updatedAction);
            assertEquals(updatedAction.getName(), action.getName());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action update " + e);
        }
    }

    @Test
    public void testCreateActionWithInvalidDSN() {
        AylaAction newAction = newDatapointAction();
        String paramsWithInvalidDsn = String.format(TEST_DATA_POINT_ACTION_LED_ON, "DSN00000", TEST_PROPERTY_BLUE_LED);
        newAction.setParameters(new AylaDatapointActionParameters(paramsWithInvalidDsn));

        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(newAction, future, future);
        try {
            //Test Creating Action with Invalid DSN. Expects 404 error code.
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e instanceof ExecutionException && e.getCause() != null) {
                assertTrue(e.getCause().getMessage().contains("404"));
            } else {
                fail("Error in testWithInvalidDSN " + e);
            }
        }
    }

    @Test
    public void testCreateUrlAction() {
        AylaAction newAction = newUrlAction();
        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(newAction, future, future);
        try {
            _createdAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdAction);
            assertNotNull(_createdAction.getUUID());
            assertEquals(_createdAction.getName(), newAction.getName());
            assertEquals(_createdAction.getType(), newAction.getType());

            AylaUrlActionParameters newParams = (AylaUrlActionParameters) newAction.getParameters();
            AylaUrlActionParameters createdParams = (AylaUrlActionParameters) _createdAction.getParameters();
            assertEquals(createdParams.scheme, newParams.scheme);
            assertEquals(createdParams.username, newParams.username);
            assertEquals(createdParams.endpoint, newParams.endpoint);
            assertEquals(createdParams.body, newParams.body);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action creation " + e);
        }
    }

    @Test
    public void testCreateEmailAction() {
        AylaAction newAction = newEmailAction();
        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(newAction, future, future);
        try {
            _createdAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdAction);
            assertNotNull(_createdAction.getUUID());
            assertEquals(_createdAction.getName(), newAction.getName());
            assertEquals(_createdAction.getType(), newAction.getType());

            AylaEmailActionParameters newParams = (AylaEmailActionParameters) newAction.getParameters();
            AylaEmailActionParameters createdParams = (AylaEmailActionParameters) _createdAction.getParameters();
            assertEquals(createdParams.email_subject, newParams.email_subject);
            assertEquals(createdParams.email_body, newParams.email_body);
            assertEquals(createdParams.email_to.length, newParams.email_to.length);
            for (int i = 0; i < newParams.email_to.length; i++) {
                assertEquals(createdParams.email_to[i], newParams.email_to[i]);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action creation " + e);
        }
    }

    @Test
    public void testCreatePushAction() {
        AylaAction newAction = newPushDestinationAction();
        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(newAction, future, future);
        try {
            _createdAction = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdAction);
            assertNotNull(_createdAction.getUUID());
            assertEquals(_createdAction.getName(), newAction.getName());
            assertEquals(_createdAction.getType(), newAction.getType());

            List<String> newDstIds = newAction.getDestinationIds();
            List<String> createdDstIds = _createdAction.getDestinationIds();
            assertEquals(newDstIds.size(), createdDstIds.size());
            for (String dst : newDstIds) {
                assertTrue(createdDstIds.contains(dst));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("error in Action creation " + e);
        }
    }

    static void deleteAction(AylaAction action) {
        if (action != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> future = RequestFuture.newFuture();
            getRulesService().deleteAction(action.getUUID(), future, future);
            try {
                future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    private static AylaRulesService getRulesService() {
        AylaSessionManager sm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        return sm.getRulesService();
    }

    private static AylaMessageService getMessageService() {
        AylaSessionManager sm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        return sm.getMessageService();
    }

    static AylaAction newDatapointAction() {
        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        String params = String.format(TEST_DATA_POINT_ACTION_LED_ON,
                TEST_DEVICE, TEST_PROPERTY_BLUE_LED);
        actionBuilder.setName(TEST_DATA_POINT_ACTION_NAME)
                .setType(AylaAction.ActionType.DATAPOINT)
                .setParameters(new AylaDatapointActionParameters(params));
        return actionBuilder.build();
    }

    static AylaAction newEmailAction() {
        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        AylaEmailActionParameters emailActionParameters = new AylaEmailActionParameters();
        emailActionParameters.email_to = new String[] {"to1@test.com", "to2@test.com"};
        emailActionParameters.email_subject = "This is a test email subject";
        emailActionParameters.email_body = "This is a test email body";

        actionBuilder.setName(TEST_EMAIL_ACTION_NAME);
        actionBuilder.setType(AylaAction.ActionType.EMAIL);
        actionBuilder.setParameters(emailActionParameters);

        return actionBuilder.build();
    }

    static AylaAction newUrlAction() {
        AylaUrlActionParameters urlActionParameters = new AylaUrlActionParameters();
        urlActionParameters.body = "foo=barbar";
        urlActionParameters.endpoint = "https://webhook.site/3ef8a9ca-b4c2-4223-8a69-234bdc0308d8";
        urlActionParameters.password = "s33cret";
        urlActionParameters.scheme = "Basic";
        urlActionParameters.username = "test_user11";

        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        actionBuilder.setName(TEST_URL_ACTION_NAME);
        actionBuilder.setType(AylaAction.ActionType.URL);
        actionBuilder.setParameters(urlActionParameters);

        return actionBuilder.build();
    }

    static AylaAction newPushDestinationAction() {
        AylaPushDestination destination = DestinationsTest.newPushDestination();
        destination = (AylaPushDestination) createDestination(destination);

        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        actionBuilder.setName(TEST_PUSH_ACTION_NAME);
        actionBuilder.setType(AylaAction.ActionType.AMS_PUSH);
        actionBuilder.addDestination(destination);

        return actionBuilder.build();
    }

    static AylaAction createDatapointAction() {
        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        String turnOnBlueLED = String.format(TEST_DATA_POINT_ACTION_LED_ON,
                TEST_DEVICE, TEST_PROPERTY_BLUE_LED);
        actionBuilder.setName(TEST_DATA_POINT_ACTION_NAME)
                .setType(AylaAction.ActionType.DATAPOINT)
                .setParameters(new AylaDatapointActionParameters(turnOnBlueLED));
        AylaAction newAction = actionBuilder.build();

        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        getRulesService().createAction(newAction, future, future);
        try {
            return future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
            return null;
        }
    }

    static AylaAction createSmsDestinationAction() {
        AylaSmsDestination destination = DestinationsTest.newSmsDestination();
        destination = (AylaSmsDestination) createDestination(destination);

        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        actionBuilder.setName(TEST_SMS_ACTION_NAME);
        actionBuilder.setType(AylaAction.ActionType.AMS_SMS);
        actionBuilder.addDestination(destination);

        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        actionBuilder.create(future, future);
        try {
            return future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
            return null;
        }
    }

    static AylaAction createEmailDestinationAction() {
        AylaEmailDestination destination = DestinationsTest.newEmailDestination();
        destination = (AylaEmailDestination) createDestination(destination);

        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        actionBuilder.setName(TEST_EMAIL_ACTION_NAME);
        actionBuilder.setType(AylaAction.ActionType.AMS_EMAIL);
        actionBuilder.addDestination(destination);

        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        actionBuilder.create(future, future);
        try {
            return future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
            return null;
        }
    }

    static AylaAction createPushDestinationAction() {
        AylaPushDestination destination = DestinationsTest.newPushDestination();
        destination = (AylaPushDestination) createDestination(destination);

        AylaAction.Builder actionBuilder = new AylaAction.Builder(
                getRulesService(), getMessageService());
        actionBuilder.setName(TEST_PUSH_ACTION_NAME);
        actionBuilder.setType(AylaAction.ActionType.AMS_PUSH);
        actionBuilder.addDestination(destination);

        RequestFuture<AylaAction> future = RequestFuture.newFuture();
        actionBuilder.create(future, future);
        try {
            return future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
            return null;
        }
    }

    static AylaDestination createDestination(@NonNull AylaDestination destination) {
        RequestFuture<AylaDestination> future = RequestFuture.newFuture();
        getMessageService().createDestination(destination, future, future);
        try {
            return future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
            return null;
        }
    }

    static AylaDevice getDevice(String dsn) {
        AylaDeviceManager deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME)
                .getDeviceManager();
        return deviceManager.deviceWithDSN(dsn);
    }

    static AylaProperty getDeviceProperty(String dsn, String propertyName) {
        return getDevice(dsn).getProperty(propertyName);
    }

    static void deleteActions(String[] actionsNamesToDelete) {
        if (actionsNamesToDelete == null || actionsNamesToDelete.length == 0) {
            return;
        }

        RequestFuture<AylaAction[]> future = RequestFuture.newFuture();
        getRulesService().fetchActions(future, future);
        try {
            AylaAction[] fetchedActions = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            List<AylaAction> actionsToDelete = new ArrayList<>();

            for (String actionName : actionsNamesToDelete) {
                for (AylaAction action : fetchedActions) {
                    if (TextUtils.equals(actionName, action.getName())) {
                        actionsToDelete.add(action);
                    }
                }
            }

            if (actionsToDelete.size() > 0) {
                deleteActions(actionsToDelete);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    static void deleteActions(List<AylaAction> actionsToDelete) {
        if (actionsToDelete == null || actionsToDelete.size() == 0) {
            return;
        }

        AylaAction action = actionsToDelete.remove(0);
        RequestFuture<AylaAPIRequest.EmptyResponse> future = RequestFuture.newFuture();
        getRulesService().deleteAction(action.getUUID(), future, future);

        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            deleteActions(actionsToDelete);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            deleteActions(actionsToDelete);
        }
    }
}