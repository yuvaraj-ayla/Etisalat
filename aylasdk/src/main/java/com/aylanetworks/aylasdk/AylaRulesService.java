package com.aylanetworks.aylasdk;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.InvalidArgumentError;
import com.aylanetworks.aylasdk.error.JsonError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.util.EmptyListener;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.aylanetworks.aylasdk.util.ServiceUrls;
import com.aylanetworks.aylasdk.util.URLHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */

/**
 * AylaRulesService class is responsible for Creating,Fetching,Updating and Deleting Rules and
 * Rule Actions. This class interacts with Rules Service in the Cloud that is a system of cloud
 * components on the Ayla Platform that implement a rules framework and enable devices to interact
 * with the outside world
 */
public class AylaRulesService {
    private static final String LOG_TAG = "AYLA_RULES_SERVICE";
    private static final String RULES_BASE_PATH = "rulesservice/v1/";
    private static final String ACTIONS_URL_PATH_JSON = RULES_BASE_PATH + "actions.json";
    private static final String ACTIONS_URL_PATH = RULES_BASE_PATH + "actions/";
    private static final String RULES_URL_PATH_JSON = RULES_BASE_PATH + "rules.json";
    private static final String RULES_URL_PATH = RULES_BASE_PATH + "rules/";
    /**
     * Request queue for Rules service messages
     */
    private final WeakReference<AylaSessionManager> _sessionManagerRef;

    /**
     * Package-private constructor. Only the SessionManager should create AylaRulesService.
     *
     * @param sessionManager Active session manager for the current session.
     */
    AylaRulesService(AylaSessionManager sessionManager) {
        _sessionManagerRef = new WeakReference<>(sessionManager);
    }

    /**
     * Create new action for the user in the cloud
     *
     * @param action          AylaAction with name type and parameters filled in
     * @param successListener Listener to receive on successful creation of Action
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Create Action
     */
    public AylaAPIRequest createAction(final AylaAction action,
                                       final Response.Listener<AylaAction> successListener,
                                       final ErrorListener errorListener) {
        try {
            Preconditions.checkState(getSessionManager() != null, "no active session");
            Preconditions.checkArgument(action != null, "action is null");
            Preconditions.checkArgument(action.getName() != null, "action name is null");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
                return null;
            }
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        AylaAction.ActionWrapper requestWrapper = new AylaAction.ActionWrapper();
        requestWrapper.action = action;
        final String postBodyString = AylaNetworks.sharedInstance().getGson().toJson
                (requestWrapper, AylaAction.ActionWrapper.class);

        String url = rulesServiceUrl(ACTIONS_URL_PATH_JSON);

        AylaAPIRequest<AylaAction.ActionWrapper> request = new AylaJsonRequest<>(
                Request.Method.POST, url, postBodyString,
                null, AylaAction.ActionWrapper.class, getSessionManager(),
                new Response.Listener<AylaAction.ActionWrapper>() {
                    @Override
                    public void onResponse(AylaAction.ActionWrapper response) {
                        AylaLog.d(LOG_TAG, "Rule created: " + response.action.toString());
                        successListener.onResponse(response.action);
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Fetches an array of AylaActions from the cloud
     *
     * @param successListener Listener to receive on successful fetch of AylaActions
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Fetch Actions
     */
    public AylaAPIRequest fetchActions(final Response.Listener<AylaAction[]> successListener,
                                       final ErrorListener errorListener) {

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        String url = rulesServiceUrl(ACTIONS_URL_PATH_JSON);
        AylaAPIRequest request = new AylaAPIRequest<>(
                Request.Method.GET,
                url,
                null,
                AylaAction.ActionsWrapper.class,
                sessionManager,
                new Response.Listener<AylaAction.ActionsWrapper>() {
                    @Override
                    public void onResponse(AylaAction.ActionsWrapper response) {
                        if (response != null && response.actions != null) {
                            successListener.onResponse(response.actions);
                        } else {
                            successListener.onResponse(new AylaAction[]{});
                        }
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Fetches an AylaAction from the cloud for the given action uuid
     *
     * @param uuid            Action uuid. This is uuid of an existing AylaAction fetched from cloud
     * @param successListener Listener to receive on successful fetch of AylaAction
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Fetch Action
     */
    public AylaAPIRequest fetchAction(final String uuid,
                                      final Response.Listener<AylaAction> successListener,
                                      final ErrorListener errorListener) {
        if (uuid == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }
        String url = rulesServiceUrl(ACTIONS_URL_PATH + uuid + ".json");

        AylaAPIRequest request = new AylaAPIRequest<>(
                Request.Method.GET,
                url,
                null,
                AylaAction.ActionWrapper.class,
                sessionManager,
                new Response.Listener<AylaAction.ActionWrapper>() {
                    @Override
                    public void onResponse(AylaAction.ActionWrapper response) {
                        successListener.onResponse(response.action);
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Deletes an existing Action from the cloud
     *
     * @param uuid            uuid of the existing Action that has been fetched from service
     * @param successListener Listener to receive on successful deletion of Action
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to delete Action
     */
    public AylaAPIRequest deleteAction(final String uuid,
                                       final Response.Listener<AylaAPIRequest.EmptyResponse>
                                               successListener,
                                       final ErrorListener errorListener) {
        if (uuid == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }
        String url = rulesServiceUrl(ACTIONS_URL_PATH + uuid + ".json");

        AylaAPIRequest<AylaAPIRequest.EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null, AylaAPIRequest.EmptyResponse.class,
                sessionManager, successListener, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Updates an existing Action from the cloud.
     *
     * @param action          Existing AylaAction with the updated parameters
     * @param successListener Listener to receive on successful Update of Action
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Create Action
     */
    public AylaAPIRequest updateAction(final AylaAction action,
                                       final Response.Listener<AylaAction> successListener,
                                       final ErrorListener errorListener) {
        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        if (action == null || action.getUUID() == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("Invalid action, Make " +
                        "sure that Action is first fetched from the cloud"));
            }
            return null;
        }

        String url = rulesServiceUrl(ACTIONS_URL_PATH + action.getUUID() + ".json");

        AylaAPIRequest<AylaAPIRequest.EmptyResponse> request = new AylaAPIRequest
                <AylaAPIRequest.EmptyResponse>(
                Request.Method.DELETE, url, null, AylaAPIRequest.EmptyResponse.class,
                sessionManager, new EmptyListener<AylaAPIRequest.EmptyResponse>(), errorListener) {
            @Override
            protected void deliverResponse(AylaAPIRequest.EmptyResponse response) {
                AylaAPIRequest originalRequest = this;
                if(!originalRequest.isCanceled()) {
                    //Now clone the Original action with all parameters except UUID
                    AylaAction updateAction = new AylaAction();
                    updateAction.setName(action.getName());
                    updateAction.setParameters(action.getParameters());
                    updateAction.setType(action.getType());
                    createAction(updateAction,successListener,errorListener);
                }
            }
        };
        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Create new Rule for the user in the cloud
     *
     * @param aylaRule        AylaRule with name,expression and action_ids filled in
     * @param successListener Listener to receive on successful creation of Rule
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Create Rule
     */
    public AylaAPIRequest createRule(final AylaRule aylaRule,
                                     final Response.Listener<AylaRule> successListener,
                                     final ErrorListener errorListener) {
        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        AylaRule.RuleWrapper ruleWrapper = new AylaRule.RuleWrapper();
        ruleWrapper.rule = aylaRule;
        final String postBodyString = AylaNetworks.sharedInstance().getGson().toJson
                (ruleWrapper, AylaRule.RuleWrapper.class);

        String url = rulesServiceUrl(RULES_URL_PATH_JSON);

        AylaAPIRequest<AylaRule.RuleWrapper> request = new AylaJsonRequest<>(
                Request.Method.POST, url, postBodyString,
                null, AylaRule.RuleWrapper.class, getSessionManager(),
                new Response.Listener<AylaRule.RuleWrapper>() {
                    @Override
                    public void onResponse(AylaRule.RuleWrapper response) {
                        AylaLog.d(LOG_TAG, "Rule created: " + response.rule.toString());
                        successListener.onResponse(response.rule);
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Updates an existing Rule from the cloud.
     *
     * @param rule            Existing AylaRule with the updated parameters
     * @param successListener Listener to receive on successful Update of Rule
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to update rule
     */

    public AylaAPIRequest updateRule(final AylaRule rule,
                                     final Response.Listener<AylaRule> successListener,
                                     final ErrorListener errorListener) {
        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        if (rule == null || rule.getUUID() == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(
                        rule == null ? "rule is null" : "rule uuid is null"));
            }
            return null;
        }

        String url = rulesServiceUrl(RULES_URL_PATH + rule.getUUID() + ".json");

        JsonElement ruleJson = AylaNetworks.sharedInstance().getGson().toJsonTree(
                rule, AylaRule.class);
        JsonObject bodyJson = new JsonObject();
        bodyJson.add("attributes", ruleJson);

        AylaJsonRequest<AylaRule.RuleWrapper> request = new AylaJsonRequest<>(
                Request.Method.PUT, url, bodyJson.toString(), null,
                AylaRule.RuleWrapper.class,
                sessionManager, new Response.Listener<AylaRule.RuleWrapper>() {
            @Override
            public void onResponse(AylaRule.RuleWrapper response) {
                if (response != null && response.rule != null) {
                    successListener.onResponse(response.rule);
                } else {
                    successListener.onResponse(new AylaRule());
                }
            }
        }, errorListener);
        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Fetches an array of AylaRules from the cloud for the given type and id
     * @param type null for Normal User. Mandatory for OEM/Ayla admin user. Valid values for
     *             OEM/Ayla admin user are device and user
     * @param id null for Normal User. Mandatory for OEM/Ayla admin user. Valid values for
     *             OEM/Ayla admin user are Device dsn and User uuid
     * @param successListener Listener to receive on successful fetch of AylaRules
     * @param errorListener Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to fetch Rules
     */
    public AylaAPIRequest fetchRules(final AylaRule.RuleType type,
                                     final String id,
                                     final Response.Listener<AylaRule[]> successListener,
                                     final ErrorListener errorListener) {
        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        Map<String, String> params = new HashMap<>();
        String baseUrl = rulesServiceUrl(RULES_URL_PATH_JSON);
        if (type != null && id != null) {
            params.put("type", type.stringValue());
            params.put("id", id);
        }

        final String url = URLHelper.appendParameters(baseUrl, params);
        AylaAPIRequest request = new AylaAPIRequest<>(
                Request.Method.GET,
                url,
                null,
                AylaRule.RulesWrapper.class,
                sessionManager,
                new Response.Listener<AylaRule.RulesWrapper>() {
                    @Override
                    public void onResponse(AylaRule.RulesWrapper response) {
                        if (response == null || response.rules == null) {
                            successListener.onResponse(new AylaRule[]{});
                        } else {
                            successListener.onResponse(response.rules);
                        }
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Fetches an AylaRule from the cloud for the given Rule uuid
     *
     * @param ruleUUID        Rule uuid. This is uuid of an existing AylaRule fetched from cloud
     * @param successListener Listener to receive on successful fetch of AylaRule
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Fetch Rule
     */
    public AylaAPIRequest fetchRule(String ruleUUID,
                                    final Response.Listener<AylaRule> successListener,
                                    final ErrorListener errorListener) {
        if (ruleUUID == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }
        String url = rulesServiceUrl(RULES_URL_PATH + ruleUUID + ".json");

        AylaAPIRequest request = new AylaAPIRequest<>(
                Request.Method.GET,
                url,
                null,
                AylaRule.RuleWrapper.class,
                sessionManager,
                new Response.Listener<AylaRule.RuleWrapper>() {
                    @Override
                    public void onResponse(AylaRule.RuleWrapper response) {
                        successListener.onResponse(response.rule);
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Fetches AylaActions from the cloud for the given Rule uuid
     *
     * @param ruleUUID        Rule uuid. This is uuid of an existing AylaRule fetched from cloud
     * @param successListener Listener to receive on successful fetch of AylaActions
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Fetch AylaActions
     */
    public AylaAPIRequest fetchRuleActions(final String ruleUUID,
                                           final Response.Listener<AylaAction[]> successListener,
                                           final ErrorListener errorListener) {
        if (ruleUUID == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        String url = rulesServiceUrl(RULES_URL_PATH + ruleUUID + "/actions.json");
        AylaAPIRequest request = new AylaAPIRequest<>(
                Request.Method.GET,
                url,
                null,
                AylaAction.ActionsWrapper.class,
                sessionManager,
                new Response.Listener<AylaAction.ActionsWrapper>() {
                    @Override
                    public void onResponse(AylaAction.ActionsWrapper response) {
                        successListener.onResponse(response.actions);
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Method to enable/disable the rule
     * @param enable true if rule is to be enabled or false if rule is to be disabled
     * @param ruleUUID Rule uuid. This is uuid of an existing AylaRule fetched from cloud
     * @param successListener Listener to receive on successful enable/disable of AylaRule
     * @param errorListener Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to enable/disable Rule
     */
    public AylaAPIRequest enableDisableRule(final boolean enable,
                                             final String ruleUUID,
                                             final Response.Listener<AylaRule> successListener,
                                             final ErrorListener errorListener) {
        if (ruleUUID == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        JSONObject bodyObject = new JSONObject();
        try {
            bodyObject.put("is_enabled", enable);
            jsonObject.put("attributes", bodyObject);
        } catch (JSONException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new JsonError(null, "JSONException trying to create "
                        + "Rule", e));
            }
            return null;
        }

        String url = rulesServiceUrl(RULES_URL_PATH + ruleUUID + ".json");

        AylaAPIRequest<AylaRule.RuleWrapper> request = new AylaJsonRequest<>(
                Request.Method.PUT, url, jsonObject.toString(),
                null, AylaRule.RuleWrapper.class, getSessionManager(),
                new Response.Listener<AylaRule.RuleWrapper>() {
                    @Override
                    public void onResponse(AylaRule.RuleWrapper response) {
                        AylaLog.d(LOG_TAG, "Rule created: " + response.rule.toString());
                        successListener.onResponse(response.rule);
                    }
                }, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Enable Ayla Rule
     * @param ruleUUID Rule uuid. This is uuid of an existing AylaRule fetched from cloud
     * @param successListener Listener to receive on successful enable of AylaRule
     * @param errorListener Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to enable Rule
     */
    public AylaAPIRequest enableRule(final String ruleUUID,
                                     final Response.Listener<AylaRule> successListener,
                                     final ErrorListener errorListener) {
        return enableDisableRule(true, ruleUUID, successListener, errorListener);
    }

    /**
     * Disable Ayla Rule
     * @param ruleUUID Rule uuid. This is uuid of an existing AylaRule fetched from cloud
     * @param successListener Listener to receive on successful disable of AylaRule
     * @param errorListener Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to disable Rule
     */
    public AylaAPIRequest disableRule(final String ruleUUID,
                                      final Response.Listener<AylaRule> successListener,
                                      final ErrorListener errorListener) {
        return enableDisableRule(false, ruleUUID, successListener, errorListener);
    }

    /**
     * Deletes an existing Rule from the cloud
     *
     * @param ruleUUID        uuid of the existing Rule that has been fetched from service
     * @param successListener Listener to receive on successful deletion of Rule
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Rule Action
     */
    public AylaAPIRequest deleteRule(final String ruleUUID,
                                     Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
                                     ErrorListener errorListener) {
        if (ruleUUID == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }
        String url = rulesServiceUrl(RULES_URL_PATH + ruleUUID + ".json");

        AylaAPIRequest<AylaAPIRequest.EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null, AylaAPIRequest.EmptyResponse.class,
                sessionManager, successListener, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    /**
     * Deletes an existing Rule and optionally associated actions and destinations
     *
     * @param rule Instantiated Rule object fetched from service
     * @param force Optional parameter to delete actions and destinations associated with the rule
     *                           - "none" or nil / null => delete only the rule instance App to manage orphaned action and destination instances
     *                           - "all" => delete the rule and all associated actions and destinations
     *                           - "orphan" => TBD delete the rule and all orphaned associated actions and destinations
     * @param successListener Block called on successful deletion of the rule
     * @param errorListener Block called with an `NSError` on failure to delete the rule
     */
    public AylaAPIRequest deleteRule(@NonNull AylaRule rule,
                                     @Nullable String force,
                                     @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
                                     @NonNull ErrorListener errorListener) {
        if (rule == null || rule.getUUID() == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        AylaAPIRequest dummyRequest = AylaAPIRequest.dummyRequest(
                AylaAPIRequest.EmptyResponse.class, successListener, errorListener);

        if (TextUtils.equals(force, "all")) {
            dummyRequest.setChainedRequest(fetchRuleActions(rule.getUUID(),
                    new Response.Listener<AylaAction[]>() {
                        @Override
                        public void onResponse(AylaAction[] actions) {
                            List<AylaAction> actionsToDelete = new ArrayList(Arrays.asList(actions));
                            deleteActionsInternal(dummyRequest, actionsToDelete,
                                    new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                                        @Override
                                        public void onResponse(AylaAPIRequest.EmptyResponse response) {
                                            dummyRequest.setChainedRequest(deleteRule(rule.getUUID(),
                                                    successListener, errorListener));
                                        }
                                    }, errorListener);
                        }
                    }, errorListener));
        } else if (TextUtils.equals(force, "orphan")) {
            errorListener.onErrorResponse(new InvalidArgumentError("not support yet"));
        } else {
            dummyRequest.setChainedRequest(deleteRule(rule.getUUID(),
                    successListener, errorListener));
        }

        return dummyRequest;
    }

    /**
     * Deletes an existing rule and associated actions from the cloud.
     * @param rule existing Rule that has been fetched from service.
     * @param successListener Listener to receive on successful deletion of Rule
     * @param errorListener   Listener to receive an AylaError should one occur
     * @return the AylaAPIRequest object used to Rule Action
     */
    public AylaAPIRequest deleteRuleAndAssociatedActions(
            @NonNull AylaRule rule,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @Nullable ErrorListener errorListener) {
        if (rule == null || rule.getUUID() == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError("UUID is required"));
            }
            return null;
        }

        AylaSessionManager sessionManager = getSessionManager();
        if (sessionManager == null) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("No session is active"));
            }
            return null;
        }

        String url = rulesServiceUrl(RULES_BASE_PATH + "rule_actions/rules/" + rule.getUUID());
        AylaAPIRequest<AylaAPIRequest.EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null, AylaAPIRequest.EmptyResponse.class,
                sessionManager, successListener, errorListener);

        sendRulesServiceRequest(request);
        return request;
    }

    private void deleteActionsInternal(
            @NonNull AylaAPIRequest originalRequest,
            @NonNull List<AylaAction> actions,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {

        if (originalRequest.isCanceled()) {
            return;
        }

        if (actions.size() == 0) {
            successListener.onResponse(new AylaAPIRequest.EmptyResponse());
            return;
        }

        AylaAction action = actions.remove(0);
        List<String> destinations = action.getDestinationIds();
        if (destinations != null && destinations.size() > 0) {
            getSessionManager().getMessageService().deleteDestinations(
                    destinations,
                    new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                        @Override
                        public void onResponse(AylaAPIRequest.EmptyResponse response) {
                            originalRequest.setChainedRequest(deleteAction(
                                    action.getUUID(),
                                    new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                                        @Override
                                        public void onResponse(AylaAPIRequest.EmptyResponse response) {
                                            deleteActionsInternal(originalRequest, actions,
                                                    successListener, errorListener);
                                        }
                                    }, errorListener));
                        }
                    }, errorListener);
        } else {
            originalRequest.setChainedRequest(deleteAction(
                    action.getUUID(),
                    new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                        @Override
                        public void onResponse(AylaAPIRequest.EmptyResponse response) {
                            deleteActionsInternal(originalRequest, actions,
                                    successListener, errorListener);
                        }
                    }, errorListener));
        }
    }

    private AylaSessionManager getSessionManager() {
        if (_sessionManagerRef.get() != null) {
            return AylaNetworks.sharedInstance().getSessionManager(_sessionManagerRef.get().
                    getSessionName());
        }
        return null;
    }

    /**
     * Enqueues the provided request to the Ayla Rules Service.
     *
     * @param request the request to send
     */
    private void sendRulesServiceRequest(AylaAPIRequest request) {
        if (getSessionManager() != null && getSessionManager().getDeviceManager() != null) {
            getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        } else {
            AylaLog.w(LOG_TAG, "unable to send request." + request.toString());
        }
    }


    /**
     * Returns a URL for the Rules service pointing to the specified path.
     * This URL will vary depending on the AylaSystemSettings provided to the CoreManager
     * during initialization.
     *
     * @param path Path of the URL to be returned
     * @return Full URL"
     */
    private String rulesServiceUrl(String path) {
        return AylaNetworks.sharedInstance().getServiceUrl(ServiceUrls.CloudService.Rules, path);
    }

}
