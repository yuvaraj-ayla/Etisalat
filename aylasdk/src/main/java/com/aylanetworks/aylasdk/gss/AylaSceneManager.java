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
import com.aylanetworks.aylasdk.AylaSchedule;
import com.aylanetworks.aylasdk.AylaShare;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.gss.model.AylaChildCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionDevice;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionTriggerResponse;

import java.util.List;
import java.util.Map;

public interface AylaSceneManager {

    String COLLECTION_TYPE_SCENE = "SCENE";

    /**
     * Creates a new scene for the current user.
     *
     * @param  name  Name of the scene.
     * @param  devices  An optional array of devices to associate with the scene.
     * @param  childCollections  An optional array of child collections to associate with the scene.
     * @param  customAttributes  An optional map of attributes to set for the scene.
     * @param  successListener  Listener called upon success with the created scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest createScene(
            @NonNull String name,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Creates a new scene for the current user.
     *
     * @param  name  Name of the scene.
     * @param  devices  An optional array of devices to associate with the scene.
     * @param  childCollections  An optional array of child collections to associate with the scene.
     * @param  customAttributes  An optional map of attributes to set for the scene.
     * @param  schedule  An optional schedule to associate with the scene.
     * @param  successListener  Listener called upon success with the created scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest createScene(
            @NonNull String name,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Creates a new scene for current user.
     *
     * @param  name  Name of the scene.
     * @param  successListener  Listener called upon success with the created scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest createScene(
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches all scenes created for current user.
     *
     * @param  successListener  Listener called upon success, with an array of
     *                          scenes created, or an empty array if none.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchAllScenes(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates devices or child scene for the specified scene.
     * @param  sceneUUID  UUID of the scene.
     * @param  devices  An optional array of devices to be re-associated with.
     * @param  childCollections  An optional array of child scenes to be re-associated with.
     * @param  successListener  Listener called upon success with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateStatesOfSceneResources(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates devices or child scene for the specified scene.
     * @param  scene  the scene to update, it must have a valid scene uuid.
     * @param  devices  An optional array of devices to be re-associated with.
     * @param  childCollections  An optional array of child scenes to be re-associated with.
     * @param  successListener  Listener called upon success with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateStatesOfSceneResources(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches all scene created for current user.
     *
     * @param  successListener  Listener called upon success, with an array of
     *                          scenes created, or an empty array if none.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchScenes(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches scenes that match the specified query conditions.
     *
     * @param  filters params used for fetching the expected scene.
     * @param  successListener  Listener called upon success, with an array of
     *                          scene fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchScenes(
            @Nullable Map<String, String> filters,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches a scene with specific UUID.
     *
     * @param  sceneUUID The UUID of the scene to fetch.
     * @param  successListener  Listener called upon success.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches a list of scenes each contains the given device DSN, and
     * matches the given type. One device can be part of multiple scenes.
     *
     * @param  dsn The unique serial number of the device to be matched.
     * @param  successListener  Listener called upon success, with an array of matched
     *                         scenes, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchScenesHavingDSN(
            @NonNull String dsn,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches scenes that match the specified query conditions.
     * Check {@link AylaCollectionFiltersBuilder} for more details
     * about the query parameters.
     *
     * @param  queryParams params used for fetching the expected scenes.
     * @param  successListener  Listener called upon success, with an array of
     *                          scenes fetched, or an empty array if none matched.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchScenesHavingDSN(
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches custom attributes for given scene with specified UUID.
     * @param sceneUUID UUID of the scene for which the attributes will be fetched.
     * @param  successListener  Listener called upon success, with an map of attributes that
     *                          have been associated with the scene. Or returns an empty
     *                          map of attributes if no attributes have been associated yet.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest fetchAttributesForScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Fetches custom attributes for the given scene.
     * @param scene the scene for which the attributes will be fetched, it must have a valid scene uuid .
     * @param  successListener  Listener called upon success, with an map of attributes that
     *                          have been associated with the scene. Or returns an empty
     *                          map of attributes if no attributes have been associated yet.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest fetchAttributesForScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates custom attributes to a scene.
     *
     * @param  scene the scene to update, it must have a valid scene uuid.
     * @param  attributes  map of attributes to be associated with the scene. Optional param.
     * @param  successListener  Listener called upon success, with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest updateAttributesForScene(
            @NonNull AylaCollection scene,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates custom attributes to a scene.
     *
     * @param  sceneUUID UUID of the scene for which the attributes will be fetched.
     * @param  attributes  map of attributes to be associated with the scene. Optional param.
     * @param  successListener  Listener called upon success, with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateAttributesForScene(
            @NonNull String sceneUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Modifies the name of the existing scene.
     *
     * @param sceneUUID the UUID of the scene to update.
     * @param name the new name of the scene.
     * @param successListener  Listener called upon success, with the updated scene.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateSceneName(
            @NonNull String sceneUUID,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Modifies the name of the existing scene.
     *
     * @param scene the scene to update, it must have a valid scene uuid.
     * @param name the new name of the scene.
     * @param successListener  Listener called upon success, with the updated scene.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateSceneName(
            @NonNull AylaCollection scene,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a scene associated with current user.
     *
     * @param sceneUUID UUID of the scene to delete.
     * @param  successListener  Listener called upon success.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a list of scenes associated with the given user.
     *
     * @param sceneUUIDs  UUIDs of the scenes to delete.
     * @param  successListener  Listener called upon success, with an empty response.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteScenes(
            @NonNull List<String> sceneUUIDs,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a scene associated with current user.
     *
     * @param scene the scene, it must have a valid scene uuid.
     * @param  successListener  Listener called upon success.
     * @param  errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes a list of scenes associated with the given user.
     *
     * @param successListener  Listener called upon success, with an empty response.
     * @param errorListener  Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteAllScenes(
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes specific devices or child collections from a scene.
     * @param  sceneUUID  UUID of the scene from which the resources will be deleted.
     * @param  devices  array of devices to be de-associated from the scene. Optional param.
     * @param  childCollections  array of child collections to be de-associated with the scene. Optional param.
     * @param  successListener  Listener called upon success, with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     */
    AylaAPIRequest deleteResourcesFromScene(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes specific devices or child collections from a scene.
     * @param  scene the scene, it must have a valid scene uuid.
     * @param  devices  array of devices to be de-associated from the scene. Optional param.
     * @param  childCollections  array of child collections to be de-associated with the scene. Optional param.
     * @param  successListener  Listener called upon success, with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     */
    AylaAPIRequest deleteResourcesFromScene(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes custom attributes from a scene.
     * @param  sceneUUID  UUID of the scene from which the resources will be deleted.
     * @param  attributes map of attributes to be deleted from the scene.
     * @param  successListener  Listener called upon success, with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     */

    AylaAPIRequest deleteAttributesFromScene(
            @NonNull String sceneUUID,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Deletes custom attributes from a scene.
     * @param  scene the scene, it must have a valid scene uuid.
     * @param  attributes map of attributes to be deleted from the scene.
     * @param  successListener  Listener called upon success, with the updated scene.
     * @param  errorListener  Listener called with an error should one occur.
     */
    AylaAPIRequest deleteAttributesFromScene(
            @NonNull AylaCollection scene,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices to a scene.
     * @param sceneUUID UUID of the scene to which devices will be added.
     * @param devices array of devices to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addDevicesToScene(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices to a scene.
     * @param scene the scene, it must have a valid scene uuid.
     * @param devices array of devices to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addDevicesToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds child collections to a scene.
     * @param sceneUUID UUID of the scene to which child collections will be added.
     * @param childCollections array of child collections to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addChildCollectionsToScene(
            @NonNull String sceneUUID,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds child collections to a scene.
     * @param scene the scene, it must have a valid scene uuid.
     * @param childCollections array of child collections to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addChildCollectionsToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices or child collections to a scene.
     * @param scene the scene, it must have a valid scene uuid.
     * @param devices array of devices to be added to the scene. Optional param.
     * @param childCollections array of child collections to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addResourcesToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds devices or child collections to a scene.
     * @param sceneUUID UUID of the scene to which resources will be added.
     * @param devices array of devices to be added to the scene. Optional param.
     * @param childCollections array of child collections to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addResourcesToScene(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds custom attributes to a scene.
     * @param scene the scene, it must have a valid scene uuid.
     * @param attributes map of attributes to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */

    AylaAPIRequest addAttributesToScene(
            @NonNull AylaCollection scene,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Adds custom attributes to a scene.
     * @param sceneUUID UUID of the scene to which attributes will be added.
     * @param attributes map of attributes to be added to the scene. Optional param.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addAttributesToScene(
            @NonNull String sceneUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Trigger a scene manually (Tap-to-Run)
     *
     * @param sceneUUID UUID of the scene to be triggered
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest triggerScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Trigger a scene manually (Tap-to-Run)
     *
     * @param scene the scene, it must have a valid scene uuid.
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest triggerScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Enable/disable a scene from executing
     *
     * @param sceneUUID UUID of the scene
     * @param enable boolean to toggle the execution.
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur.
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest enableTriggerScene(
            @NonNull String sceneUUID,
            @NonNull boolean enable,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Enable/disable a scene from executing
     *
     * @param scene the scene, it must have a valid scene uuid.
     * @param enable boolean to toggle the execution.
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest enableTriggerScene(
            @NonNull AylaCollection scene,
            @NonNull boolean enable,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Add a schedule to the scene
     *
     * @param sceneUUID UUID of the scene
     * @param schedule Schedule to be associated with the scene
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest addScheduleToScene(
            @NonNull String sceneUUID,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Add a schedule to the scene
     *
     * @param scene the scene, it must have a valid scene uuid
     * @param schedule Schedule to be associated with the scene
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest addScheduleToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Update the schedule of the scene
     *
     * @param scene the scene, it must have a valid scene uuid
     * @param schedule Schedule to be updated in the scene
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateScheduleForScene(
            @NonNull AylaCollection scene,
            @NonNull AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Update the schedule of the scene
     *
     * @param sceneUUID UUID of the scene
     * @param schedule Schedule to be updated in the scene
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest updateScheduleForScene(
            @NonNull String sceneUUID,
            @NonNull AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Delete the schedule of the scene
     *
     * @param scene the scene, it must have a valid scene uuid
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest deleteScheduleFromScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Delete the schedule of the scene
     *
     * @param sceneUUID UUID of the scene
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request.
     */
    AylaAPIRequest deleteScheduleFromScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Share a scene
     *
     * @param share Share object to be added to the scene
     * @param successListener Listener called upon success
     * @param errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest shareScene(
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
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener);

    /**
     *
     * Remove  associated devices from a scene and its child collection if recursively set to
     * true else remove only directly associated devices from given scene.
     *
     * @param  sceneUUID  The UUID of the scene from which device has to be removed.
     * @param  devices  An array of devices to be removed from the scene.
     * @param  recursively  boolean indicates whether to remove device from child collections or
     *                     not along with given parent scene.
     * @param  successListener Listener called upon success
     * @param  errorListener Listener called with an error should one occur
     *
     * @return An AylaAPIRequest object representing this request
     */
    AylaAPIRequest removeDevicesFromScene(
            @NonNull String sceneUUID,
            @NonNull AylaCollectionDevice[] devices,
            @NonNull boolean recursively,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener);
}
