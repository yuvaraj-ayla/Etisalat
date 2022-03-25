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
import com.aylanetworks.aylasdk.AylaShare;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.gss.model.AylaChildCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionTriggerResponse;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionDevice;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionProperty;

import java.util.List;
import java.util.Map;

public class AylaGroupManagerImp implements AylaGroupManager {

    private final AylaCollectionManager collectionManager;

    public AylaGroupManagerImp(@NonNull AylaCollectionManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    @Override
    public AylaAPIRequest createGroup(
            @NonNull String name,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.createCollection(
                name,
                COLLECTION_TYPE_GROUP,
                devices,
                childCollections,
                customAttributes,
                null,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest createGroup(
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.createCollection(
                name,
                COLLECTION_TYPE_GROUP,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchAllGroups(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        AylaCollectionFiltersBuilder filtersBuilder = new AylaCollectionFiltersBuilder()
                .withCollectionType(COLLECTION_TYPE_GROUP);
        Map<String, String> filters = filtersBuilder.build();
        return collectionManager.fetchCollections(
                filters,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchGroups(
            @Nullable Map<String, String> filters,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollections(
                filters,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchGroup(
            @NonNull String groupUUID,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollection(groupUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchGroupsHavingDSN(
            @NonNull String dsn,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollectionsHavingDSN(dsn,
                COLLECTION_TYPE_GROUP,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchGroupsHavingDSN(
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchCollections(
                queryParams,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchAttributesForGroup(
            @NonNull String groupUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchAttributesForCollection(
                groupUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest fetchAttributesForGroup(
            @NonNull AylaCollection group,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.fetchAttributesForCollection(
                group.collectionUuid,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateAttributesForGroup(
            @NonNull AylaCollection group,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateAttributesForCollection(
                group,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateAttributesForGroup(
            @NonNull String groupUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateAttributesForCollection(
                groupUUID,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateGroupName(
            @NonNull String groupUUID,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateCollectionName(groupUUID,
                name,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateGroupName(
            @NonNull AylaCollection group,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateCollectionName(
                group.collectionUuid,
                name,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteGroup(
            @NonNull String groupUUID,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteCollection(
                groupUUID,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteGroups(
            @NonNull List<String> groupUUIDs,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteCollections(
                groupUUIDs,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteGroup(
            @NonNull AylaCollection group,
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteCollection(
                group.collectionUuid,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteAllGroups(
            @NonNull Response.Listener<AylaAPIRequest.EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteAllCollections(
                COLLECTION_TYPE_GROUP,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteResourcesFromGroup(
            @NonNull String groupUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteResourcesFromCollection(
                groupUUID,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteResourcesFromGroup(
            @NonNull AylaCollection group,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteResourcesFromCollection(
                group.collectionUuid,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteAttributesFromGroup(
            @NonNull String groupUUID,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteAttributesFromCollection(
                groupUUID,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest deleteAttributesFromGroup(
            @NonNull AylaCollection group,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.deleteAttributesFromCollection(
                group.collectionUuid,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addDevicesToGroup(
            @NonNull String groupUUID,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                groupUUID,
                COLLECTION_TYPE_GROUP,
                devices,
                null,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addDevicesToGroup(
            @NonNull AylaCollection group,
            @Nullable AylaCollectionDevice[] devices,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                group.collectionUuid,COLLECTION_TYPE_GROUP,
                devices,
                null,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addChildCollectionsToGroup(
            @NonNull String groupUUID,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                groupUUID,
                COLLECTION_TYPE_GROUP,
                null,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addChildCollectionsToGroup(
            @NonNull AylaCollection group,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                group.collectionUuid,COLLECTION_TYPE_GROUP,
                null,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addResourcesToGroup(
            @NonNull AylaCollection group,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                group.collectionUuid,
                COLLECTION_TYPE_GROUP,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addResourcesToGroup(
            @NonNull String groupUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addResourcesToCollection(
                groupUUID,
                COLLECTION_TYPE_GROUP,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addAttributesToGroup(
            @NonNull AylaCollection group,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addAttributesToCollection(
                group,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addAttributesToGroup(
            @NonNull String groupUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.addAttributesToCollection(
                groupUUID,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest triggerGroup(
            @NonNull String groupUUID,
            @NonNull AylaCollectionProperty[] properties,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateValueForCollectionProperties(
                groupUUID,
                properties,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest triggerGroup(
            @NonNull AylaCollection group,
            @NonNull AylaCollectionProperty[] properties,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.updateValueForCollectionProperties(
                group.collectionUuid,
                properties,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest shareGroup(
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
    public AylaAPIRequest removeDevicesFromGroup(
            @NonNull String groupUUID,
            @NonNull AylaCollectionDevice[] devices,
            @NonNull boolean recursively,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return collectionManager.removeDevicesFromCollection(
                groupUUID,
                devices,
                recursively,
                successListener,
                errorListener);
    }
}
