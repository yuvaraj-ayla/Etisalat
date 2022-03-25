package com.aylanetworks.aylasdk.gss;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.AylaAPIRequest.EmptyResponse;
import com.aylanetworks.aylasdk.AylaShare;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.gss.model.AylaChildCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionTriggerResponse;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionDevice;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionProperty;

import java.util.List;
import java.util.Map;

public interface AylaGroupManager {

    String COLLECTION_TYPE_GROUP = "GROUP";

    /**
     * Creates a new group for current user.
     *
     * @param  name  Name of the group.
     * @param  devices  An optional array of devices to associate with the group.
     * @param  childCollections  An optional array of child collections to associate with the group.
     * @param  customAttributes  An optional map of attributes to set for the group.
     * @param  successListener  Listener called upon success with the created group.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest createGroup(
            @NonNull String name,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Creates a new group for current user.
     *
     * @param  name  Name of the group.
     * @param  successListener  Listener called upon success with the created group.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest createGroup(
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches all groups created for current user.
     *
     * @param  successListener  Listener called upon success, with an array of
     *                          groups created, or an empty array if none.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchAllGroups(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches groups that match the specified query conditions.
     *
     * @param  filters params used for fetching the expected collections.
     * @param  successListener  Listener called upon success, with an array of
     *                          groups fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchGroups(
            @Nullable Map<String, String> filters,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches the group with the specific UUID.
     *
     * @param  groupUUID The UUID of the group to fetch.
     * @param  successListener  Listener called upon success.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchGroup(
            @NonNull String groupUUID,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches a list of groups each contains the given device DSN, and
     * matches the given type. One device can be part of multiple groups.
     *
     * @param  dsn The unique serial number of the device to be matched.
     * @param  successListener  Listener called upon success, with an array of matched
     *                         groups, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchGroupsHavingDSN(
            @NonNull String dsn,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches groups that match the specified query conditions.
     * Check {@link AylaCollectionFiltersBuilder} for more details
     * about the query parameters.
     *
     * @param  queryParams params used for fetching the expected collections.
     * @param  successListener  Listener called upon success, with an array of
     *                          groups fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchGroupsHavingDSN(
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches custom attributes for given group with specified UUID.
     * @param groupUUID UUID of the group for which the attributes will be fetched.
     * @param  successListener  Listener called upon success, with an map of attributes that
     *                          have been associated with the group. Or returns an empty
     *                          map of attributes if no attributes have been associated yet.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchAttributesForGroup(
            @NonNull String groupUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches custom attributes for given group.
     * @param group the group for which the attributes will be fetched, it must have a valid group uuid .
     * @param  successListener  Listener called upon success, with an map of attributes that
     *                          have been associated with the group. Or returns an empty
     *                          map of attributes if no attributes have been associated yet.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest fetchAttributesForGroup(
            @NonNull AylaCollection group,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates custom attributes to a group.
     *
     * @param  group the group to update, it must have a valid group uuid.
     * @param  attributes  map of attributes to be associated with the group. Optional param.
     * @param  successListener  Listener called upon success, with the updated group.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest updateAttributesForGroup(
            @NonNull AylaCollection group,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates custom attributes to a group.
     *
     * @param  groupUUID UUID of the group for which the attributes will be fetched.
     * @param  attributes  map of attributes to be associated with the group. Optional param.
     * @param  successListener  Listener called upon success, with the updated group.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest updateAttributesForGroup(
            @NonNull String groupUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Modifies the name of the existing group.
     *
     * @param groupUUID the UUID of the group to update.
     * @param name the new name of the group.
     * @param successListener  Listener called upon success, with the updated group.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateGroupName(
            @NonNull String groupUUID,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Modifies the name of the existing group.
     *
     * @param group the group to update, it must have a valid group uuid.
     * @param name the new name of the group.
     * @param successListener  Listener called upon success, with the updated group.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateGroupName(
            @NonNull AylaCollection group,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a group associated with current user.
     *
     * @param groupUUID Unique UUID of the group to delete.
     * @param  successListener  Listener called upon success.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteGroup(
            @NonNull String groupUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a list of groups associated with the given user.
     *
     * @param groupUUIDs  UUIDs of the groups to delete.
     * @param  successListener  Listener called upon success, with an empty response.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteGroups(
            @NonNull List<String> groupUUIDs,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a group associated with current user.
     *
     * @param group the group, it must have a valid group uuid.
     * @param  successListener  Listener called upon success.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteGroup(
            @NonNull AylaCollection group,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes all groups associated with the given user.
     *
     * @param successListener  Listener called upon success, with an empty response.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteAllGroups(
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes specific devices or child collections from a group.
     * @param  groupUUID  UUID of the group from which the resources will be deleted.
     * @param  devices  array of devices to be de-associated from the group. Optional param.
     * @param  childCollections  array of child collections to be de-associated with the group. Optional param.
     * @param  successListener  Listener called upon success, with the updated group.
     * @param  errorListener  Listener called with an error should one occur.
     */
    AylaAPIRequest deleteResourcesFromGroup(
            @NonNull String groupUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes specific devices or child collections from a group.
     * @param  group the group, it must have a valid group uuid.
     * @param  devices  array of devices to be de-associated from the group. Optional param.
     * @param  childCollections  array of child collections to be de-associated with the group. Optional param.
     * @param  successListener  Listener called upon success, with the updated group.
     * @param  errorListener  Listener called with an error should one occur.
     */
    AylaAPIRequest deleteResourcesFromGroup(
            @NonNull AylaCollection group,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes custom attributes from a group.
     * @param  groupUUID  UUID of the group from which the resources will be deleted.
     * @param  attributes map of attributes to be deleted from the group.
     * @param  successListener  Listener called upon success, with the updated group.
     * @param  errorListener  Listener called with an error should one occur.
     */

    AylaAPIRequest deleteAttributesFromGroup(
            @NonNull String groupUUID,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes custom attributes from a group.
     * @param  group the group, it must have a valid group uuid.
     * @param  attributes map of attributes to be deleted from the group.
     * @param  successListener  Listener called upon success, with the updated group.
     * @param  errorListener  Listener called with an error should one occur.
     */

    AylaAPIRequest deleteAttributesFromGroup(
            @NonNull AylaCollection group,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices to a group.
     * @param groupUUID UUID of the group to which devices will be added.
     * @param devices array of devices to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addDevicesToGroup(
            @NonNull String groupUUID,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices to a group.
     * @param group the group, it must have a valid group uuid.
     * @param devices array of devices to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addDevicesToGroup(
            @NonNull AylaCollection group,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds child collections to a group.
     * @param groupUUID UUID of the group to which child collections will be added.
     * @param childCollections array of child collections to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addChildCollectionsToGroup(
            @NonNull String groupUUID,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds child collections to a group.
     * @param group the group, it must have a valid group uuid.
     * @param childCollections array of child collections to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addChildCollectionsToGroup(
            @NonNull AylaCollection group,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices or child collections to a group.
     * @param group the group, it must have a valid group uuid.
     * @param devices array of devices to be added to the group. Optional param.
     * @param childCollections array of child collections to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addResourcesToGroup(
            @NonNull AylaCollection group,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices or child collections to a group.
     * @param groupUUID UUID of the group to which resources will be added.
     * @param devices array of devices to be added to the group. Optional param.
     * @param childCollections array of child collections to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addResourcesToGroup(
            @NonNull String groupUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds custom attributes to a group.
     * @param group the group, it must have a valid group uuid.
     * @param attributes map of attributes to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addAttributesToGroup(
            @NonNull AylaCollection group,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds custom attributes to a group.
     * @param groupUUID UUID of the group to which attributes will be added.
     * @param attributes map of attributes to be added to the group. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addAttributesToGroup(
            @NonNull String groupUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Triggers a group with the properties.
     *
     * @param groupUUID UUID of the group for which property will be changed
     * @param successListener Listener called upon success, with the changed group.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest triggerGroup(
            @NonNull String groupUUID,
            @NonNull AylaCollectionProperty[] properties,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Triggers a group with the properties.
     *
     * @param group the group, it must have a valid group uuid.
     * @param successListener Listener called upon success, with the changed group.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest triggerGroup(
            @NonNull AylaCollection group,
            @NonNull AylaCollectionProperty[] properties,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Share a group
     *
     * @param share Share object to be added to the group
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest shareGroup(
            @NonNull AylaShare share,
            @NonNull Response.Listener<AylaShare> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetch the shares owned by the current user.
     *
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest fetchOwnedShares(
            @NonNull Response.Listener<AylaShare[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetch the shares received by the current user.
     *
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest fetchReceivedShares(
            @NonNull Response.Listener<AylaShare[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * update the share details.
     *
     * @param shareUUID UUID of the share object to be updated
     * @param share share object used to update
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest updateShare(
            @NonNull String shareUUID,
            @NonNull AylaShare share,
            @NonNull Response.Listener<AylaShare> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Delete a share
     *
     * @param shareUUID UUID of the share object to be deleted
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest deleteShare(
            @NonNull String shareUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     *
     * Remove  associated devices from a group and its child collection if recursively set to
     * true else remove only directly associated devices from given group.
     *
     * @param  groupUUID  The UUID of the group from which device has to be removed.
     * @param  devices  An array of devices to be removed from the group.
     * @param  recursively  boolean indicates whether to remove device from child collections or
     *                     not along with given parent group.
     * @param  successListener Listener called upon success
     * @param  errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest removeDevicesFromGroup(
            @NonNull String groupUUID,
            @NonNull AylaCollectionDevice[] devices,
            @NonNull boolean recursively,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);
}
