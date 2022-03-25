package com.aylanetworks.aylasdk;

import android.text.TextUtils;

import androidx.test.InstrumentationRegistry;

import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.rules.AylaConnectionRuleExpression;
import com.aylanetworks.aylasdk.rules.AylaDatapointRuleExpression;
import com.aylanetworks.aylasdk.rules.AylaNumericComparisonExpression;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
public class RulesTest {

    private static final String TEST_DEVICE   = TestConstants.TEST_DEVICE_DSN;
    private static final String TEST_PROPERTY_BLUE_LED = TestConstants.TEST_DEVICE_PROPERTY;
    private static final String TEST_RULE_NAME  = "__TestRuleName__";

    private AylaRule _createdRule;
    private AylaAction _createdDpAction;
    private AylaAction _createdSmsAction;
    private AylaAction _createdEmailAction;
    private AylaAction _createdPushAction;

    @Before
    public void setUp() {
        assertTrue(TestConstants.signIn(InstrumentationRegistry.getContext()));
        AylaSessionManager sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME);
        assertNotNull(sessionManager);
        assertNotNull(sessionManager.getRulesService());
        assertNotNull(sessionManager.getMessageService());

        TestConstants.waitForDeviceManagerInitComplete();
        assertNotNull(getDeviceProperty(TEST_DEVICE, TEST_PROPERTY_BLUE_LED));

        // delete created rules and associated action in cases of last failure
        // to avoid name conflicts when creating new test rules and actions.
        deleteRules(new String[] {TEST_RULE_NAME});
    }

    @After
    public void tearDown() {
        if (_createdRule != null) {
            deleteRule(_createdRule);
        }

        if (_createdDpAction != null) {
            ActionsTest.deleteAction(_createdDpAction);
        }

        if (_createdSmsAction != null) {
            ActionsTest.deleteAction(_createdSmsAction);
        }

        if (_createdEmailAction != null) {
            ActionsTest.deleteAction(_createdEmailAction);
        }

        if (_createdPushAction != null) {
            ActionsTest.deleteAction(_createdPushAction);
        }
    }

    @Test
    public void testCreateRuleWithActions() {
        _createdDpAction = ActionsTest.createDatapointAction(); assertNotNull(_createdDpAction.getUUID());
        _createdSmsAction = ActionsTest.createSmsDestinationAction(); assertNotNull(_createdSmsAction.getUUID());
        _createdEmailAction = ActionsTest.createEmailDestinationAction(); assertNotNull(_createdEmailAction.getUUID());
        _createdPushAction = ActionsTest.createPushDestinationAction(); assertNotNull(_createdPushAction.getUUID());

        List<AylaAction> createdActions = new ArrayList<>();
        createdActions.add(_createdDpAction);
        createdActions.add(_createdSmsAction);
        createdActions.add(_createdEmailAction);
        createdActions.add(_createdPushAction);

        AylaRule newRule = newDatapointRule(TEST_RULE_NAME, createdActions);
        RequestFuture<AylaRule> ruleRequestFuture = RequestFuture.newFuture();
        getRulesService().createRule(newRule, ruleRequestFuture, ruleRequestFuture);
        try {
            _createdRule = ruleRequestFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdRule.getUUID());
            assertEquals(_createdRule.getName(), newRule.getName());
            assertEquals(_createdRule.getDescription(), newRule.getDescription());
            assertEquals(_createdRule.getActionIds().length, newRule.getActionIds().length);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateRuleWithRuleBuilder() {
        AylaAction dpAction = ActionsTest.newDatapointAction();
        AylaAction urlAction = ActionsTest.newUrlAction();

        RequestFuture<AylaRule> future = RequestFuture.newFuture();
        new AylaRule.Builder(getRulesService())
                .setName(TEST_RULE_NAME)
                .setDescription("This is a rule builder test")
                .setExpression(new AylaDatapointRuleExpression(
                        getDeviceProperty(TEST_DEVICE, TEST_PROPERTY_BLUE_LED),
                        AylaNumericComparisonExpression.Comparator.EQ, 0))
                .addAction(dpAction)
                .addAction(urlAction)
                .create(future, future);

        try {
            _createdRule = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdRule);
            assertNotNull(_createdRule.getUUID());
            assertTrue(_createdRule.getEnabled());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }

        RequestFuture<AylaAction[]> futureFetch = RequestFuture.newFuture();
        getRulesService().fetchRuleActions(_createdRule.getUUID(), futureFetch, futureFetch);
        try {
            AylaAction[] actions = futureFetch.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertEquals(actions.length, _createdRule.getActionIds().length);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateRuleWithoutRuleName() {
        AylaRule newRule = newDatapointRule(null, Collections.emptyList());
        RequestFuture<AylaRule> future = RequestFuture.newFuture();
        getRulesService().createRule(newRule, future, future);
        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e instanceof ExecutionException && e.getCause() != null) {
                // Expects 422 error code
                assertTrue(e.getCause().getMessage().contains("422"));
            } else {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testCreateRuleWithoutRuleExpression() {
        AylaRule newRule = newDatapointRule(TEST_RULE_NAME, Collections.emptyList());
        newRule.setExpression(null);
        RequestFuture<AylaRule> future = RequestFuture.newFuture();
        getRulesService().createRule(newRule, future, future);
        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            if (e instanceof ExecutionException && e.getCause() != null) {
                // Expects 422 error code
                assertTrue(e.getCause().getMessage().contains("422"));
            } else {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testCreateAndUpdateRule() {
        AylaAction newAction = ActionsTest.newDatapointAction();
        RequestFuture<AylaAction> actionFuture = RequestFuture.newFuture();
        getRulesService().createAction(newAction, actionFuture, actionFuture);
        try {
            _createdDpAction = actionFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdDpAction);
            assertNotNull(_createdDpAction.getUUID());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }

        List<AylaAction> actions = new ArrayList<>();
        actions.add(_createdDpAction);
        AylaRule newRule = newDatapointRule(TEST_RULE_NAME, actions);

        RequestFuture<AylaRule> ruleFuture = RequestFuture.newFuture();
        getRulesService().createRule(newRule, ruleFuture, ruleFuture);
        try {
            _createdRule = ruleFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdRule);
            assertNotNull(_createdRule.getUUID());
            assertEquals(_createdRule.getName(), newRule.getName());
            assertEquals(_createdRule.getDescription(), newRule.getDescription());
            assertEquals(_createdRule.getActionIds().length, newRule.getActionIds().length);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }

        // update rule name and description
        _createdRule.setName(_createdRule.getName() + "_updated");
        _createdRule.setDescription(_createdRule.getDescription() + "_updated");

        ruleFuture = RequestFuture.newFuture();
        getRulesService().updateRule(_createdRule, ruleFuture, ruleFuture);
        try {
            AylaRule updatedRule = ruleFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertEquals(updatedRule.getUUID(), _createdRule.getUUID());
            assertEquals(updatedRule.getName(), _createdRule.getName());
            assertEquals(updatedRule.getDescription(), _createdRule.getDescription());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }

        // update rule actions
        _createdEmailAction = ActionsTest.createEmailDestinationAction();
        assertNotNull(_createdEmailAction);
        assertNotNull(_createdEmailAction.getUUID());
        _createdRule.setActionIds(new String[]{_createdEmailAction.getUUID()});
        ruleFuture = RequestFuture.newFuture();
        getRulesService().updateRule(_createdRule, ruleFuture, ruleFuture);
        try {
            AylaRule updatedRule = ruleFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertEquals(updatedRule.getUUID(), _createdRule.getUUID());
            assertEquals(updatedRule.getActionIds()[0], _createdEmailAction.getUUID());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEnableAndDisableRule() {
        RequestFuture<AylaRule> future = RequestFuture.newFuture();
        new AylaRule.Builder(getRulesService())
                .setName(TEST_RULE_NAME)
                .setDescription("This is a rule enable/disable test")
                .setExpression(new AylaDatapointRuleExpression(
                        getDeviceProperty(TEST_DEVICE, TEST_PROPERTY_BLUE_LED),
                        AylaNumericComparisonExpression.Comparator.EQ, 0))
                .addAction(ActionsTest.newDatapointAction())
                .create(future, future);

        try {
            _createdRule = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(_createdRule);
            assertNotNull(_createdRule.getUUID());
            assertTrue(_createdRule.getEnabled());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }

        future = RequestFuture.newFuture();
        getRulesService().disableRule(_createdRule.getUUID(), future, future);
        try {
            AylaRule updatedRule = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(updatedRule);
            assertEquals(updatedRule.getUUID(), _createdRule.getUUID());
            assertFalse(updatedRule.getEnabled());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }

        future = RequestFuture.newFuture();
        getRulesService().enableRule(_createdRule.getUUID(), future, future);
        try {
            AylaRule updatedRule = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            assertNotNull(updatedRule);
            assertEquals(updatedRule.getUUID(), _createdRule.getUUID());
            assertTrue(updatedRule.getEnabled());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCreateDeviceConnectionRule() {
        RequestFuture<AylaRule> future = RequestFuture.newFuture();
        new AylaRule.Builder(getRulesService())
                .setName(TEST_RULE_NAME)
                .setDescription("This is a test connection rule")
                .setExpression(new AylaConnectionRuleExpression(getDevice(TEST_DEVICE),
                        AylaConnectionRuleExpression.ConnectionStatus.ALL))
                //.addActions(...)
                .create(future, future);

        try {
            _createdRule = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e.getMessage());
        }
    }

    private static AylaSessionManager getSessionManager() {
        return AylaNetworks.sharedInstance().getSessionManager(
                TestConstants.TEST_SESSION_NAME);
    }

    private static AylaRulesService getRulesService() {
        return getSessionManager().getRulesService();
    }

    static AylaRule newDatapointRule(String ruleName, List<AylaAction> createdActions) {
        AylaRule.Builder ruleBuilder = new AylaRule.Builder(getRulesService());
        ruleBuilder.setName(ruleName);
        ruleBuilder.setDescription("This is a test datapoint rule");

        AylaProperty property = getDeviceProperty(TEST_DEVICE, TEST_PROPERTY_BLUE_LED);
        String comparator = AylaNumericComparisonExpression.Comparator.EQ;
        AylaDatapointRuleExpression expression = new AylaDatapointRuleExpression(property, comparator, 1);

        ruleBuilder.setExpression(expression);
        ruleBuilder.addActions(createdActions);

        return ruleBuilder.build();
    }

    static void deleteRule(AylaRule rule) {
        if (rule != null) {
            RequestFuture<AylaAPIRequest.EmptyResponse> future = RequestFuture.newFuture();
            getRulesService().deleteRuleAndAssociatedActions(rule, future, future);
            try {
                future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                e.printStackTrace();
            }
        }
    }

    static void deleteRules(String[] ruleNamesToDelete) {
        if (ruleNamesToDelete == null || ruleNamesToDelete.length  == 0) {
            return;
        }

        RequestFuture<AylaRule[]> future = RequestFuture.newFuture();
        getRulesService().fetchRules(null, null, future, future);
        try {
            AylaRule[] fetchedRules = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            List<AylaRule> rulesToDelete = new ArrayList<>();
            for (String ruleName : ruleNamesToDelete) {
                for (AylaRule rule : fetchedRules) {
                    if (TextUtils.equals(ruleName, rule.getName())) {
                        rulesToDelete.add(rule);
                    }
                }
            }

            if (rulesToDelete.size() > 0) {
                deleteRules(rulesToDelete);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    static void deleteRules(List<AylaRule> rulesToDelete) {
        if (rulesToDelete == null || rulesToDelete.size() == 0) {
            return;
        }

        AylaRule rule = rulesToDelete.remove(0);
        RequestFuture<AylaAPIRequest.EmptyResponse> future = RequestFuture.newFuture();
        getRulesService().deleteRule(rule.getUUID(), future, future);

        try {
            future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            deleteRules(rulesToDelete);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            deleteRules(rulesToDelete);
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
}
