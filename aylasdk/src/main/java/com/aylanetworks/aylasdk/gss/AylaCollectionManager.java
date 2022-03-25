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
import com.aylanetworks.aylasdk.AylaSchedule;
import com.aylanetworks.aylasdk.AylaShare;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.gss.model.AylaChildCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionTriggerResponse;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionDevice;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionProperty;

import java.util.List;
import java.util.Map;

public interface AylaCollectionManager {

    /**
     * Creates a new collection viz. group or scene.
     * No devices or child collections will be associated with the new
     * collection created, use corresponding {@link #addResourcesToCollection}
     * methods to add new devices or child collections to the collection,
     * or use {@link #updateAttributesForCollection} to associate a set
     * of customizable attributes with the new collection.
     *
     * @param  name  Identifiable name of the collection. Collection name should contain
     *               alphanumeric characters, whitespaces, underscore(_) and hyphen(-)
     *               and should be between 1-32 characters.
     * @param  type  Type of the collection.
     * @param  successListener  Listener called upon success, with the created
     *                          collection in the response.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest createCollection(
            @NonNull String name,
            @NonNull String type,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Creates a new collection viz. group or scene.
     * Provided devices or child collections will be associated with the new
     * collection created. See also {@link #updateAttributesForCollection}
     * on how to associate a set of customizable attributes with the new collection.
     *
     * @param  name  Identifiable name of the collection. Collection name should contain
     *               alphanumeric characters, whitespaces, underscore(_) and hyphen(-)
     *               and should be between 1-32 characters.
     * @param  type  Type of the collection.
     * @param  devices  An array of devices to be associated with the collection at the
     *                  time of its creation. Optional param.
     * @param  childCollections  An array of child collection to be associated with
     *                           the collection at the time of its creation. Optional param.
     * @param  customAttributes  Attributes to be associated with the
     *                           collection at the time of its creation. Optional param.
     * @param  schedule  Schedule to be associated with the
     *      *                    collection at the time of its creation. Optional param.
     * @param  successListener  Listener called upon success, with the created
     *                          collection in the response.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest createCollection(
            @NonNull String name,
            @NonNull String type,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches the collection identified by given UUID from the cloud service.
     *
     * @param  collectionUUID The UUID of the collection to be fetched.
     * @param  successListener  Listener called upon success, with a collection.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches all collections owned by current user.
     * Check {@link AylaCollectionFiltersBuilder} for more details
     * about the query parameters.
     *
     * @param  successListener  Listener called upon success, with an array of
     *                          collections fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollections(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches collections that match the specified query conditions.
     * Check {@link AylaCollectionFiltersBuilder} for more details
     * about the query parameters.
     *
     * @param  queryParams params used for fetching the expected collections.
     * @param  successListener  Listener called upon success, with an array of
     *                          collections fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollections(
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches collections that match the specified query conditions.
     * Check {@link AylaCollectionFiltersBuilder} for more details
     * about the query parameters.
     *
     * @param  filtersBuilder query params builder.
     * @param  successListener  Listener called upon success, with an array of
     *                          collections fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollections(
            @NonNull AylaCollectionFiltersBuilder filtersBuilder,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches a list of collections each contains the given device DSN, and
     * matches the given type.
     *
     * @param  dsn The unique serial number of the device to be matched.
     * @param  type The type of the collection to be matched.
     * @param  successListener  Listener called upon success, with an array of matched
     *                         collections, or an array of empty collections if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollectionsHavingDSN(
            @NonNull String dsn,
            @NonNull String type,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches a list of collections each contains the given device DSN, and
     * matches the given type. One device can be part of multiple collection.
     *
     * @param  dsn The unique serial number of the device to be matched.
     * @param  type The type of the collection to be matched.
     * @param  queryParams params used for fetching the expected collections.
     * @param  successListener  Listener called upon success, with an array of matched
     *                         collections, or an array of empty collections if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollectionsHavingDSN(
            @NonNull String dsn,
            @NonNull String type,
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches a list of collections with the given type.
     * One device can be part of multiple collection.
     *
     * @param  type The type of the collection to be matched.
     * @param  successListener  Listener called upon success, with an array of matched
     *                         collections, or an array of empty collections if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchCollections(
            @NonNull String type,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches custom attributes for given collection with specified UUID.
     * @param collectionUUID UUID of the collection for which the attributes will be fetched.
     * @param  successListener  Listener called upon success, with attributes that
     *                          have been associated with the collection. Or returns an empty
     *                          map if no attributes have been associated yet.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest fetchAttributesForCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates devices or child collections for the specified type of collection.
     * @param  collectionUUID  UUID of the collection to update.
     * @param  devices  new array of devices to be associated with the collection. Optional param.
     * @param  childCollections  new array of child collections to be associated with the collection. Optional param.
     * @param  successListener  Listener called upon success, with the updated collection.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateStatesOfCollectionResources(
            @NonNull String collectionUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates devices or child collections for the specified collection.
     * @param  collection  the collection to update which must own a valid collection uuid.
     * @param  devices  new array of devices to be associated with the collection. Optional param.
     * @param  childCollections  new array of child collections to be associated with the collection. Optional param.
     * @param  successListener  Listener called upon success, with the updated collection.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateStatesOfCollectionResources(
            @NonNull AylaCollection collection,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates custom attributes for the specified collection.
     * @param  collectionUUID  UUID of the collection to update.
     * @param  attributes  Attributes to be associated with the collection. Optional param.
     * @param  successListener  Listener called upon success, with the updated collection.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest updateAttributesForCollection(
            @NonNull String collectionUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates custom attributes to a collection.
     *
     * @param  collection the collection to update, it must have a valid collection uuid.
     * @param  attributes  Attributes to be associated with the collection. Optional param.
     * @param  successListener  Listener called upon success, with the updated collection.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest updateAttributesForCollection(
            @NonNull AylaCollection collection,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Modifies the name of the existing collection.
     *
     * @param collectionUUID the UUID of the collection to update.
     * @param name the new name of the collection.
     * @param successListener  Listener called upon success, with the updated collection.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
     AylaAPIRequest updateCollectionName(
            @NonNull String collectionUUID,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes the collection with the given uuid.
     *
     * @param collectionUUID UUID of the collection to delete.
     * @param  successListener  Listener called upon success, with an empty response.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes all collections associated with the given user.
     *
     * @param collectionUUIDs  UUIDs of the collections to delete.
     * @param  successListener  Listener called upon success, with an empty response.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteCollections(
            @NonNull List<String> collectionUUIDs,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a list of collections associated with the given user.
     *
     * @param type The type of the collection
     * @param successListener  Listener called upon success, with an empty response.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteAllCollections(
            @NonNull String type,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes specific devices or child collections from a collection.
     * @param  collectionUUID  UUID of the collection from which the resources will be deleted.
     * @param  devices  array of devices to be de-associated from the collection. Optional param.
     * @param  childCollections  array of child collections to be de-associated with the collection. Optional param.
     * @param  successListener  Listener called upon success, with the updated collection.
     * @param  errorListener  Listener called with an error should one occur.
     */
    AylaAPIRequest deleteResourcesFromCollection(
        @NonNull String collectionUUID,
        @Nullable AylaCollectionDevice[] devices,
        @Nullable AylaChildCollection[] childCollections,
        @NonNull Response.Listener<AylaCollection> successListener,
        @NonNull ErrorListener errorListener);

    /**
     * Deletes custom attributes from a collection.
     * @param  collectionUUID  UUID of the collection from which the resources will be deleted.
     * @param  attributes Attributes to be deleted from the collection.
     * @param  successListener  Listener called upon success, with the updated collection.
     * @param  errorListener  Listener called with an error should one occur.
     */

    AylaAPIRequest deleteAttributesFromCollection(
            @NonNull String collectionUUID,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);


    /**
     * Adds devices or child collections to a collection.
     * @param collectionUUID UUID of the collection to which the resources will be added.
     * @param type The type of the collection
     * @param devices array of devices to be added to the collection. Optional param.
     * @param childCollections array of child collections to be added to the collection. Optional param.
     * @param successListener Listener called upon success, with the updated collection.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addResourcesToCollection(
            @NonNull String collectionUUID,
            @NonNull String type,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds custom attributes to a collection.
     * @param collection the collection, it must have a valid collection uuid.
     * @param attributes Attributes to be added to the collection. Optional param.
     * @param successListener Listener called upon success, with the updated collection.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest addAttributesToCollection(
            @NonNull AylaCollection collection,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds custom attributes to a collection.
     * @param collectionUUID UUID of the collection to be added.
     * @param attributes Attributes to be added to the collection. Optional param.
     * @param successListener Listener called upon success, with the updated collection.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
     AylaAPIRequest addAttributesToCollection(
            @NonNull String collectionUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates properties value for a collection.
     * @param collectionUUID UUID of the collection for which property will be changed
     * @param successListener Listener called upon success, with the changed collection.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateValueForCollectionProperties(
            @NonNull String collectionUUID,
            @NonNull AylaCollectionProperty[] properties,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Trigger a collection manually (Tap-to-Run)
     *
     * @param collectionUUID UUID of the collection to be triggered
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest triggerCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Enable/disable a collection from executing
     *
     * @param collectionUUID UUID of the collection
     * @param enable boolean to toggle the execution.
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest enableTriggerCollection(
            @NonNull String collectionUUID,
            @NonNull boolean enable,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Add a schedule to the collection
     *
     * @param collectionUUID UUID of the collection
     * @param schedule Schedule to be associated with the collection
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest addScheduleToCollection(
            @NonNull String collectionUUID,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Update the schedule of the collection
     *
     * @param collectionUUID UUID of the collection
     * @param schedule Schedule to be updated in the collection
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest updateScheduleForCollection(
            @NonNull String collectionUUID,
            @NonNull AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Delete the schedule of the collection
     *
     * @param collectionUUID UUID of the collection
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest deleteScheduleFromCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Share a collection
     *
     * @param share Share object to be added to the collection
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest shareCollection(
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
     * Remove  associated devices from a collection and its child collection if recursively set to
     * true else remove only directly associated devices from given collection.
     *
     * @param  collectionUUID  The UUID of the collection from which device has to be removed.
     * @param  devices  An array of devices to be removed from the collection.
     * @param  recursively  boolean indicates whether to remove device from child collections or
     *                     not along with given parent collection.
     * @param  successListener Listener called upon success
     * @param  errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest removeDevicesFromCollection(
            @NonNull String collectionUUID,
            @NonNull AylaCollectionDevice[] devices,
            @NonNull boolean recursively,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);
}
