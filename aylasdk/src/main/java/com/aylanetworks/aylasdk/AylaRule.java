package com.aylanetworks.aylasdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.ams.action.params.AylaActionParameters;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.rules.AylaRuleExpression;
import com.aylanetworks.aylasdk.util.ObjectUtils;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/*
 * AylaSDK
 *
 * Copyright 2017 Ayla Networks, all rights reserved
 */
/**
 * Class used to represent Rule in Ayla cloud. A rule consists of expression and Action(s). When
 * an expression is reached the actions are fired. In general, rule expression is a logical
 * statement conveyed in terms of logical relationships between the rule subjects.
 * Rule subjects can enter a rule expression as stand-alone terms or as function arguments.
 * For example, the above statement expresses relationships between the following six rule
 * (event) subjects: <pre>
 *   DATAPOINT(dsn_1,prop_name_1)
 *   DATAPOINT(dsn_2,prop_name_2)
 *   DATAPOINT(dsn_3,prop_name_3)
 *   DATAPOINT(dsn_4,prop_name_4)
 *   LOCATION(uuid_1)
 *   LOCATION(dsn_1) </pre>
 *
 * Currently supported rule subjects:
 * <table border="1">
 *   <tr><td>Rule Subject</td>                  <td>Derived from event</td>                 <td>Type</td></tr>
 *   <tr><td>DATAPOINT(dsn,prop_name)</td>      <td>datapoint event</td>                    <td>datapoint_value</td></tr>
 *   <tr><td>DATAPOINT_ACK(dsn,prop_name)</td>  <td>datapoint event</td>                    <td>datapoint_value</td></tr>
 *   <tr><td>CONNECTIVITY(dsn)</td>             <td>connectivity event</td>                 <td>status -> dsn online or offline</td></tr>
 *   <tr><td>REGISTRATION(dsn)</td>             <td>device registration event</td>          <td>boolean -> dsn registered or</td></tr>
 *   <tr><td>LOCATION(dsn)</td>                 <td>device location change event</td>       <td>(lat,long) -> pair of decimals</td></tr>
 *   <tr><td>LOCATION(uuid)</td>                <td>user’s phone location change event</td> <td>(lat,long) -> pair of decimals</td></tr>
 * </table>
 */
public class AylaRule {
    @Expose
    private String rule_uuid; //This is created by cloud when a new Action is created
    @Expose
    private String name; //User given name to this rule
    @Expose
    private boolean isEnabled;//Boolean value to indicate if Rule is enabled or not
    @Expose
    private String description;//Optional.User given description of this rule
    @Expose
    private String expression;//A logical statement conveyed in terms of logical relationships
                             //between the rule subjects
    @Expose
    private String[] actionIds;// An array of Action UUIDs
    @Expose
    private String createdAt;//Time set by service when the Rule  was created
    @Expose
    private String updatedAt;//Time set by service when the Rule was updated

    public String getName() { return name; }

    public boolean getEnabled() { return isEnabled; }

    public String getDescription() { return description; }

    public String getExpression() { return expression; }

    public String[] getActionIds() { return actionIds; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }

    public String getUUID() { return rule_uuid; }

    public void setName(String name) { this.name = name; }

    public void setEnabled(Boolean enabled) { isEnabled = enabled; }

    public void setDescription(String description) { this.description = description; }

    public void setExpression(String expression) { this.expression = expression; }

    public void setActionIds(String[] actionIds) { this.actionIds = actionIds; }

    /**
     * Wrapper object used by AylaRulesService
     */
    public static class RulesWrapper {
        @Expose
        public AylaRule[] rules;
    }

    /**
     * Wrapper object used by AylaRulesService
     */
    public static class RuleWrapper {
        @Expose
        public AylaRule rule;
    }

    /**
     * RuleType is  an enumerator that has device/user
     */
    public enum RuleType {
        Device("device"),
        User("user");

        RuleType(String value) {
            _stringValue = value;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public static RuleType fromStringValue(String value) {
            for (RuleType val : values()) {
                if (val.stringValue().equals(value)) {
                    return val;
                }
            }
            return null;
        }

        private final String _stringValue;
    }

    /**
     * Helper class for creating a rule and associated actions. To use, create a Builder object and
     * call methods on it to set the name, description, property to evaluate, condition and actions.
     *
     * When the Builder has been configured, call {@link #create} to create the rule and any
     * required actions on the service.
     *
     * Any actions that are added to the Builder will be created if they do not already exist (e.g.
     * if they do not have an action ID already associated, they will be created).
     *
     */
    public static class Builder {
        private final static String LOG_TAG = "AylaRule.Builder";
        private String _name;
        private String _description;
        private boolean _enabled = true;
        private AylaRuleExpression _expression;

        final private List<AylaAction> _actionsList;
        final private AylaRulesService _rulesService;

        public Builder(AylaRulesService rulesService) {
            _rulesService = rulesService;
            _actionsList = new ArrayList<>();
        }

        public Builder setName(@NonNull String name) {
            _name = name;
            return this;
        }

        public Builder setDescription(@Nullable String description) {
            _description = description;
            return this;
        }

        public Builder setEnabled(boolean enabled) {
            _enabled = enabled;
            return this;
        }

        public Builder addAction(@NonNull AylaAction action) {
            _actionsList.add(action);
            return this;
        }

        public Builder addActions(@NonNull List<AylaAction> action) {
            _actionsList.addAll(action);
            return this;
        }

        public Builder setExpression(@NonNull AylaRuleExpression expression) {
            _expression = expression;
            return this;
        }

        /**
         * @deprecated To create new datapont rule expression, use following statement instead,
         * <code>
         *     setRuleExpression(new AylaDatapointRuleExpression(property, condition, value));
         * </code>
         * Note that this method does nothing and might be removed in the future release.
         */
        public Builder setProperty(@NonNull AylaProperty property) {
            return this;
        }

        /**
         * @deprecated To create new datapont rule expression, use following statement instead,
         * <code>
         *     setRuleExpression(new AylaDatapointRuleExpression(property, condition, value));
         * </code>
         * Note that this method does nothing and might be removed in the future release.
         */
        public Builder setCondition(@NonNull String condition, @NonNull Object value) {
            return this;
        }

        /**
         * Builds the rule and associate it with created actions. Note that some actions
         * added through either {@link #addAction(AylaAction)} or {@link #addActions(List)}
         * might not have been created in the cloud, actions without valid action UUID will
         * be ignored. To create the actions and associate them with the rule, use
         * {@link #create(Response.Listener, ErrorListener)} instead.
         *
         * @return Returns a new rule which can then be created in the cloud via a call
         * to {@link AylaRulesService#createRule(AylaRule, Response.Listener, ErrorListener)}.
         */
        public AylaRule build() {
            AylaRule rule = new AylaRule();
            rule.setName(_name);
            rule.setDescription(_description);
            rule.setEnabled(_enabled);

            if (_expression != null) {
                rule.setExpression(_expression.create());
            }

            List<String> actionIdsList = new ArrayList<>();
            for (AylaAction action : _actionsList) {
                if (action.getUUID() != null) {
                    actionIdsList.add(action.getUUID());
                } else {
                    AylaLog.i(LOG_TAG, "ignore action:" + action.getName());
                }

                if (action.getParameters() == null) {
                    // defaults to use empty action parameters to avoid
                    // "Action parameters are empty or not valid" error
                    action.setParameters(new AylaActionParameters());
                }
            }
            String[] ids = new String[actionIdsList.size()];
            rule.setActionIds(actionIdsList.toArray(ids));

            return rule;
        }


        /**
         * Creates the rule and associate it with provided actions. Note that actions
         * without valid UUID will be created first.
         *
         * @param successListener Listener to be called when the rule creation succeeds
         * @param errorListener Listener to be called should one error occurred.
         *
         * @return An AylaAPIRequest, or null if an error occurred. If an error occurred,
         * this method will return null and the error listener will be called.
         * The returned AylaAPIRequest may be canceled.
         */
        public AylaAPIRequest create(final Response.Listener<AylaRule> successListener,
                                     final ErrorListener errorListener) {
            try {
                // Make sure we have everything we need to create the rule
                Preconditions.checkNotNull(_rulesService, "Rules service is null");
                Preconditions.checkNotNull(_name, "Name is required");
                Preconditions.checkNotNull(_expression, "Rule expression is required");
            } catch (NullPointerException e) {
                errorListener.onErrorResponse(new PreconditionError(e.getMessage()));
                return null;
            }

            // Create the actions if necessary
            List<AylaAction> actionsToCreate = new ArrayList<>();
            for (AylaAction action : _actionsList) {
                if (action.getUUID() == null) {
                    actionsToCreate.add(action);
                }

                if (action.getName() == null) {
                    // generate a default unique action name to avoid action name conflicts,
                    // specifically, to avoid "Action with given name already exists" error.
                    action.setName(ObjectUtils.generateRandomToken(16));
                }
            }

            AylaAPIRequest dummyRequest = AylaAPIRequest.dummyRequest(
                    AylaRule.class, successListener, errorListener);
            createActions(dummyRequest, actionsToCreate);

            return dummyRequest;
        }

        private void createActions(final AylaAPIRequest<AylaRule> request,
                                   final List<AylaAction> actions) {
            if (request.isCanceled()) {
                return;
            }

            if (actions.isEmpty()) {
                createRule(request);
                return;
            }

            AylaAction nextAction = actions.remove(0);
            _rulesService.createAction(nextAction, response -> {
                nextAction.updateUuidFrom(response);
                createActions(request, actions);
            }, error -> {
                AylaLog.e(LOG_TAG, "failed to create action " + nextAction
                        + " " + error.getMessage());
                request._errorListener.onErrorResponse(error);
            });
        }

        private void createRule(AylaAPIRequest<AylaRule> request) {
            AylaLog.d(LOG_TAG, "all actions created and each has valid UUID now");
            if (request.isCanceled()) {
                return;
            }

            AylaRule newRule = build();
            _rulesService.createRule(newRule, request._successListener, request._errorListener);
        }
    }
}
