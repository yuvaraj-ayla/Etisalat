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

public class AylaSceneManagerImp implements AylaSceneManager {

    private final AylaCollectionManager collectionManager;

    public AylaSceneManagerImp(AylaCollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public AylaAPIRequest createScene(
            @NonNull String name,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.createCollection(
                name,
                COLLECTION_TYPE_SCENE,
                devices,
                childCollections,
                customAttributes,
                null,
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest createScene(
            @NonNull String name,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.createCollection(
                name,
                COLLECTION_TYPE_SCENE,
                devices,
                childCollections,
                customAttributes,
                schedule,
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest createScene(
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.createCollection(
                name,
                COLLECTION_TYPE_SCENE,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchAllScenes(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollections(
                successListener,errorListener);
    }

    @Override
    public AylaAPIRequest updateStatesOfSceneResources(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateStatesOfCollectionResources(
                sceneUUID,
                devices,
                childCollections,
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest updateStatesOfSceneResources(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateStatesOfCollectionResources(
                scene.collectionUuid,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchScenes(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollections(
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchScenes(
            @Nullable Map<String, String> filters,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollections(filters, successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollection(sceneUUID, successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchScenesHavingDSN(
            @NonNull String dsn,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollectionsHavingDSN(dsn,
                COLLECTION_TYPE_SCENE,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchScenesHavingDSN(
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollections(
                queryParams,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchAttributesForScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchAttributesForCollection(
                sceneUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchAttributesForScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchAttributesForCollection(
                scene.collectionUuid,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateAttributesForScene(
            @NonNull AylaCollection scene,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateAttributesForCollection(
                scene,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateAttributesForScene(
            @NonNull String sceneUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateAttributesForCollection(
                sceneUUID,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateSceneName(
            @NonNull String sceneUUID,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateCollectionName(
                sceneUUID,
                name,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateSceneName(
            @NonNull AylaCollection scene,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateCollectionName(
                scene.collectionUuid,
                name,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteCollection(
                sceneUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteScenes(
            @NonNull List<String> sceneUUIDs,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteCollections(
                sceneUUIDs,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteCollection(
                scene.collectionUuid,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteAllScenes(
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteAllCollections(
                COLLECTION_TYPE_SCENE,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteResourcesFromScene(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteResourcesFromCollection(
                sceneUUID,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteResourcesFromScene(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteResourcesFromCollection(
                scene.collectionUuid,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteAttributesFromScene(
            @NonNull String sceneUUID,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteAttributesFromCollection(
                sceneUUID,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteAttributesFromScene(
            @NonNull AylaCollection scene,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteAttributesFromCollection(
                scene.collectionUuid,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addDevicesToScene
            (@NonNull String sceneUUID,
             @Nullable AylaCollectionDevice[] devices,
             @NonNull Response.Listener<AylaCollection> successListener,
             @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                sceneUUID,
                COLLECTION_TYPE_SCENE,
                devices,
                null,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addDevicesToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                scene.collectionUuid,
                COLLECTION_TYPE_SCENE,
                devices,
                null,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addChildCollectionsToScene(
            @NonNull String sceneUUID,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                sceneUUID,
                COLLECTION_TYPE_SCENE,
                null,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addChildCollectionsToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                scene.collectionUuid,
                COLLECTION_TYPE_SCENE,
                null,childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addResourcesToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                scene.collectionUuid,
                COLLECTION_TYPE_SCENE,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addResourcesToScene(
            @NonNull String sceneUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                sceneUUID,
                COLLECTION_TYPE_SCENE,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addAttributesToScene(
            @NonNull AylaCollection scene,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addAttributesToCollection(
                scene,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addAttributesToScene(
            @NonNull String sceneUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addAttributesToCollection(
                sceneUUID,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest triggerScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.triggerCollection(
                sceneUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest triggerScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.triggerCollection(
                scene.collectionUuid,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest enableTriggerScene(
            @NonNull String sceneUUID,
            @NonNull boolean enable,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.enableTriggerCollection(
                sceneUUID,
                enable,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest enableTriggerScene(
            @NonNull AylaCollection scene,
            @NonNull boolean enable,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.enableTriggerCollection(
                scene.collectionUuid,
                enable,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addScheduleToScene(
            @NonNull String sceneUUID,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addScheduleToCollection(
                sceneUUID,
                schedule,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addScheduleToScene(
            @NonNull AylaCollection scene,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addScheduleToCollection(
                scene.collectionUuid,
                schedule,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateScheduleForScene(
            @NonNull AylaCollection scene,
            @NonNull AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateScheduleForCollection(
                scene.collectionUuid,
                schedule,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateScheduleForScene(
            @NonNull String sceneUUID,
            @NonNull AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateScheduleForCollection(
                sceneUUID,
                schedule,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteScheduleFromScene(
            @NonNull AylaCollection scene,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteScheduleFromCollection(
                scene.collectionUuid,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteScheduleFromScene(
            @NonNull String sceneUUID,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteScheduleFromCollection(
                sceneUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest shareScene(
            @NonNull AylaShare share,
            @NonNull Response.Listener<AylaShare> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.shareCollection(
                share, successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchOwnedShares(
            @NonNull Response.Listener<AylaShare[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchOwnedShares(
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchReceivedShares(
            @NonNull Response.Listener<AylaShare[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchReceivedShares(
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest updateShare(
            @NonNull String shareUUID,
            @NonNull AylaShare share,
            @NonNull Response.Listener<AylaShare> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateShare(
                shareUUID,share,successListener,errorListener);
    }

    @Override
    public AylaAPIRequest deleteShare(
            @NonNull String shareUUID,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteShare(
                shareUUID,successListener,errorListener);
    }

    @Override
    public AylaAPIRequest removeDevicesFromScene(
            @NonNull String sceneUUID,
            @NonNull AylaCollectionDevice[] devices,
            @NonNull boolean recursively,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.removeDevicesFromCollection(
                sceneUUID,
                devices,
                recursively,
                successListener,
                errorListener);
    }
}
