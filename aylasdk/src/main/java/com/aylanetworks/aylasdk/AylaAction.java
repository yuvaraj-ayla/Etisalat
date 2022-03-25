package com.aylanetworks.aylasdk;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.ams.action.params.AylaActionParameters;
import com.aylanetworks.aylasdk.ams.action.params.AylaDatapointActionParameters;
import com.aylanetworks.aylasdk.ams.dest.AylaDestination;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.rules.AylaDatapointRuleExpression;
import com.aylanetworks.aylasdk.util.ObjectUtils;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.aylanetworks.aylasdk.util.TypeUtils;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
/**
 * Class used to represent Action in Ayla cloud. An AylaAction object needs to be
 * created for using Rules. A Rule has one or more Actions. Check {@link ActionType}
 * for more details about the supported action types.
 */
public class AylaAction {
    @Expose
    private String action_uuid; //This is created by cloud when a new Action is created
    @Expose
    private String name; //User given name to this action
    @Expose
    private String type; //User given action type (DATAPOINT,EMAIL)
    @Expose
    private AylaActionParameters parameters; //Contains key/value pairs with information relevant
                                            // to be performed
    @Expose
    private List<String> destinationIds; // Contains destinationsIds which are required for destination type actions

    @Expose
    private List<String> ruleIds; // Contains rule ids which are required for destination type actions

    @Expose
    private String createdAt; //Time set by service when the action was created
    @Expose
    private String updatedAt; //Time set by service when the action was updated

    public String getName() { return name; }

    public ActionType getType() { return ActionType.fromStringValue(type); }

    public String getUUID() { return action_uuid; }

    public String getCreatedAt() { return createdAt; }

    public String getUpdatedAt() { return updatedAt; }

    public AylaActionParameters getParameters() { return parameters; }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ActionType type) {
       this.type = type.stringValue();
    }

    public void setParameters(AylaActionParameters parameters) {
        this.parameters = parameters;
    }

    public void updateFrom(AylaAction other) {
        action_uuid = other.action_uuid;
        name = other.name;
        type = other.type;
        parameters = other.parameters;
        createdAt = other.createdAt;
        updatedAt = other.updatedAt;
    }

    public List<String> getDestinationIds() {
        return destinationIds;
    }

    public List<String> getRuleIds() {
        return ruleIds;
    }

    public void updateUuidFrom(AylaAction other) {
        this.action_uuid = other.action_uuid;
    }

    /**
     * Helper method to create a new datapoint action.
     * @deprecated use {@link Builder Action Builder} to create new datapoint action.
     */
    public static AylaAction datapointAction(String name,
                                             AylaProperty property,
                                             String operator,
                                             Object value) {
        AylaDatapointActionParameters params = AylaDatapointActionParameters.
                createPropertyValueParameters(property, value);
        String actionName = (name == null) ? params.getDatapoint() : name;
        AylaAction newAction = new AylaAction();
        newAction.setName(actionName);
        newAction.setType(ActionType.DATAPOINT);
        newAction.setParameters(params);
        return newAction;
    }

    /**
     * Helper method to create a new datapoint rule expression.
     * @deprecated use {@link AylaDatapointRuleExpression} to create new datapoint expression.
     */
    public static String formatDatapointExpression(AylaProperty property,
                                                   String operator,
                                                   Object value) {
        return new AylaDatapointRuleExpression(property, operator, value).create();
    }

    /**
     * Wrapper object used by AylaRulesService
     */
    public static class ActionsWrapper {
        @Expose
        AylaAction[] actions;
    }

    /**
     * Wrapper object used by AylaRulesService
     */
    public static class ActionWrapper {
        @Expose
        public AylaAction action;
    }

    /**
     * ActionType is  an enumerator that has Email/Datapoint
     */
    public enum ActionType {
        URL("URL"),
        SMS("SMS"),
        EMAIL("EMAIL"),
        DATAPOINT("DATAPOINT"),
        AMS_SMS("AMS_SMS"),
        AMS_EMAIL("AMS_EMAIL"),
        AMS_PUSH("AMS_PUSH");

        ActionType(String value) {
            _stringValue = value;
        }

        public final String stringValue() {
            return _stringValue;
        }

        public static ActionType fromStringValue(String value) {
            for (ActionType val : values()) {
                if (val.stringValue().equals(value)) {
                    return val;
                }
            }
            return null;
        }

        private final String _stringValue;
    }

    /**
     * Helper class for creating an action and associated destinations. To use, create a Builder
     * object and call methods on it to set the name, type and destinations.
     *
     * When the Builder has been configured, call {@link #create} to create the action and any
     * associated actions on the messages service.
     *
     * Any destinations that are added to the Builder will be created if they do not already
     * exist (e.g. if they do not have an UUID already associated, they will be created).
     *
     */
    public static class Builder {

        private final static String LOG_TAG = "AylaAction.Builder";

        private String _name;
        private ActionType _type;
        private AylaActionParameters _parameters;

        final private Set<String> _ruleIds;
        final private Set<AylaDestination> _destinations;
        final private AylaRulesService _rulesService;
        final private AylaMessageService _messageService;

        public Builder(@NonNull AylaRulesService rulesService,
                       @NonNull AylaMessageService messageService) {
            _rulesService = rulesService;
            _messageService = messageService;
            _destinations = new HashSet<>();
            _ruleIds = new HashSet<>();
        }

        /**
         * Sets the unique name of the action.
         */
        public Builder setName(@NonNull String name) {
            _name = name;
            return this;
        }

        /**
         * Sets action type and corresponding parameters.
         */
        public Builder setType(@NonNull ActionType type) {
            _type = type;
            return this;
        }

        /**
         * Sets action parameters required for some action types, including
         * <ul>
         *     <li>{@link ActionType#DATAPOINT}</li>
         *     <li>{@link ActionType#EMAIL}</li>
         *     <li>{@link ActionType#URL}</li>
         * </ul>
         */
        public Builder setParameters(@NonNull AylaActionParameters parameters) {
            _parameters = parameters;
            return this;
        }

        /**
         * Adds a destination to be notified when the action is triggered.
         * @param destination the destination to be added.
         */
        public Builder addDestination(@NonNull AylaDestination destination) {
            _destinations.add(destination);
            return this;
        }

        /**
         * Adds an array of destinations to be notified when the action is triggered.
         * @param destinations the destinations to be added.
         */
        public Builder addDestinations(@NonNull List<AylaDestination> destinations) {
            _destinations.addAll(destinations);
            return this;
        }

        /**
         * Add the rule this action is to be associated with.
         */
        public Builder addRuleId(@NonNull String ruleId) {
            _ruleIds.add(ruleId);
            return this;
        }

        /**
         * Add the rules this action is to be associated with.
         */
        public Builder addRuleIds(@NonNull List<String> ruleIds) {
            _ruleIds.addAll(ruleIds);
            return this;
        }

        /**
         * Builds a new action with provided info. Note that destinations that
         * hasn't been created, say without a valid UUID, will be ignored. Use
         * {@link #create(Response.Listener, ErrorListener)} instead to create
         * and associated it with provided destinations.
         *
         * Once built, the new action should be created via a call to
         * {@link AylaRulesService#createAction(AylaAction, Response.Listener, ErrorListener)}
         * so as to be added to a rule.
         *
         * @see {@link #create(Response.Listener, ErrorListener)} on how to create
         * the ation in the cloud instread of create locally.
         */
        public AylaAction build() {
            AylaAction newAction = new AylaAction();
            newAction.setName(_name);
            newAction.setType(_type);
            newAction.destinationIds = getDestinationIDs();
            newAction.ruleIds = getRuleIDs();

            if (_parameters == null) {
                _parameters = new AylaActionParameters();
            }
            newAction.setParameters(_parameters);
            return newAction;
        }

        /**
         * Creates the action as well as the associated destinations specified by the caller.
         *
         * @param successListener Listener to be called when the action creation succeeds.
         * @return An AylaAPIRequest, or null if an error occurred. If an error occurred, this
         * method will return null and the error listener will be called. The returned AylaAPIRequest
         * may be canceled.
         *
         * @see {@link #build()} if the action is only reuired to be created in memory instead
         * of creatd in the cloud.
         */
        public AylaAPIRequest create(final Response.Listener<AylaAction> successListener,
                                     final ErrorListener errorListener) {
            try {
                // Make sure we have everything we need to create the action
                Preconditions.checkNotNull(_rulesService, "rules service is null");
                Preconditions.checkNotNull(_messageService, "messages service is null");
                Preconditions.checkNotNull(_type, "Action type is required");
            } catch (NullPointerException | IllegalArgumentException e) {
                errorListener.onErrorResponse(new PreconditionError(e.getMessage()));
                return null;
            }

            AylaAPIRequest dummyRequest = AylaAPIRequest.dummyRequest(
                        AylaDestination[].class, successListener, errorListener);
            createDestinations(dummyRequest, getDestinationsToCreate());

            return dummyRequest;
        }

        private List<String> getDestinationIDs() {
            List<String> ids = new ArrayList<>();
            for (AylaDestination destination : _destinations) {
                if (!TextUtils.isEmpty(destination.getUUID())) {
                    ids.add(destination.getUUID());
                }
            }
            return ids;
        }

        private List<String> getRuleIDs() {
            List<String> ids = new ArrayList<>();
            for (String rid : _ruleIds) {
                if (!TextUtils.isEmpty(rid)) {
                    ids.add(rid);
                }
            }
            return ids;
        }

        private List<AylaDestination> getDestinationsToCreate() {
            List<AylaDestination> destinationsToCreate = new ArrayList<>();
            for (AylaDestination destination : _destinations) {
                if (TextUtils.isEmpty(destination.getUUID())) {
                    destinationsToCreate.add(destination);
                }
            }
            return destinationsToCreate;
        }

        private void createDestinations(final AylaAPIRequest<AylaAction> request,
                                        final List<AylaDestination> destinations) {
            if (request.isCanceled()) {
                return;
            }

            if (destinations.isEmpty()) {
                createAction(request);
                return;
            }

            AylaDestination nextDestination = destinations.remove(0);
            _messageService.createDestination(nextDestination, response -> {
                // update especially destination UUID so that the destination
                // can finally be associated with the action.
                nextDestination.updateFrom(response);
                createDestinations(request, destinations);
            }, error -> {
                AylaLog.e(LOG_TAG, "failed to create destination " + nextDestination
                        + " " + error.getMessage());
                request._errorListener.onErrorResponse(error);
            });
        }

        private void createAction(AylaAPIRequest<AylaAction> request) {
            AylaAction newAction = build();

            if (newAction.getName() == null) {
                // provide a device name if not specified to simplify the API usage as well
                // as to avoid action name conflicts, specifically, to avoid "Action with
                // given name already exists" error.
                newAction.setName(ObjectUtils.generateRandomToken(16));
            }
            _rulesService.createAction(newAction, request._successListener, request._errorListener);
        }
    }


}
