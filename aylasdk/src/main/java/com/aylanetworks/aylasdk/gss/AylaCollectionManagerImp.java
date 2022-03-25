package com.aylanetworks.aylasdk.gss;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.AylaAPIRequest.EmptyResponse;
import com.aylanetworks.aylasdk.AylaJsonRequest;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.AylaNetworks;
import com.aylanetworks.aylasdk.AylaSchedule;
import com.aylanetworks.aylasdk.AylaSessionManager;
import com.aylanetworks.aylasdk.AylaShare;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.InvalidArgumentError;
import com.aylanetworks.aylasdk.gss.model.AylaChildCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection.CollectionShareWrapper;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionTriggerResponse;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionDevice;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionProperty;
import com.aylanetworks.aylasdk.metrics.AylaFeatureMetric;
import com.aylanetworks.aylasdk.metrics.AylaMetric;
import com.aylanetworks.aylasdk.util.ServiceUrls;
import com.aylanetworks.aylasdk.util.URLHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AylaCollectionManagerImp implements AylaCollectionManager {

    final static String LOG_TAG = "AylaCollectionManagerImp";

    private final WeakReference<AylaSessionManager> _sessionManagerRef;

    final private static String endPointForCollections =
            "groupsceneservice/v1/collections.json";
    final private static String endPointForCollectionResources =
            "groupsceneservice/v1/collections/%s/devices_collections.json";
    final private static String endPointForAttributes =
            "groupsceneservice/v1/collections/%s/collection_metadata.json";
    final private static String endPointForDevice =
            "groupsceneservice/v1/collections/%s.json";
    final private static String endPointForCollection =
            "groupsceneservice/v1/collections/%s.json";
    final private static String endPointForResourcesDeletion =
            "groupsceneservice/v1/collections/%s/delete_devices_collections.json";
    final private static String endPointForAttributesDeletion =
            "groupsceneservice/v1/collections/%s/delete_collection_metadata.json";
    final private static String endPointForPropertyChange =
            "groupsceneservice/v1/collections/%s/datapoint.json";
    final private static String endPointForActivateSceneManually =
            "groupsceneservice/v1/collections/%s/activate.json";
    final private static String endPointForSceneAutomation =
            "/groupsceneservice/v1/collections/%s/automation.json";
    final private static String endPointForSchedule =
            "groupsceneservice/v1/collections/%s/schedule.json";
    final private static String endPointForScheduleDeletion =
            "groupsceneservice/v1/collections/%s/delete_schedule.json";
    final private static String endPointForScheduleUpdate =
            "groupsceneservice/v1/collections/%s/update_schedule.json";
    final private static String endPointForShare =
            "groupsceneservice/v1/collections/shares.json";
    final private static String endPointForReceivedShare=
            "groupsceneservice/v1/collections/shares/received.json";
    final private static String endPointForShareUpdate =
            "groupsceneservice/v1/collections/shares/%s.json";
    final private static String endPointForRemoveDevice =
            "groupsceneservice/v1/collections/%s/remove_devices_from_hierarchy.json";

    public AylaCollectionManagerImp(@NonNull AylaSessionManager sessionManager) {
        _sessionManagerRef = new WeakReference<>(sessionManager);
    }

    @Override
    public AylaAPIRequest createCollection(
            @NonNull String name,
            @NonNull String type,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        return createCollection(name, type, null, null, null, null,
                successListener, errorListener);
    }

    @Override
    public AylaAPIRequest createCollection(
            @NonNull String name,
            @NonNull String type,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @Nullable Map<String, String> customAttributes,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        AylaCollection newCollection = new AylaCollection();
        newCollection.name = name;
        newCollection.type = type;
        newCollection.devices = devices;
        newCollection.childCollections = childCollections;
        newCollection.custom_attributes = customAttributes;
        newCollection.schedule = schedule;

        AylaCollection.CollectionWrapper wrapper = new AylaCollection.CollectionWrapper();
        wrapper.collection = newCollection;

        String url = getServiceUrl(endPointForCollections);
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(wrapper);

        AylaAPIRequest<AylaCollection> request = new AylaJsonRequest<>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                new Response.Listener<AylaCollection>() {
                    @Override
                    public void onResponse(AylaCollection response) {
                        AylaNetworks.sharedInstance().getMetricsManager().createFeatureMetric(
                                AylaFeatureMetric.AylaFeatureMetricType.GROUPSANDSCENES,
                                AylaMetric.Result.SUCCESS,
                                type + "_createCollection",
                                null
                        );
                        successListener.onResponse(response);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        AylaNetworks.sharedInstance().getMetricsManager().createFeatureMetric(
                                AylaFeatureMetric.AylaFeatureMetricType.GROUPSANDSCENES,
                                AylaMetric.Result.FAILURE,
                                type + "_createCollection",
                                error
                        );
                        errorListener.onErrorResponse(error);
                    }
                });
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForCollection, collectionUUID));
        AylaAPIRequest<AylaCollection> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaCollection.class,
                getSessionManager(),
                new Response.Listener<AylaCollection>() {
                    @Override
                    public void onResponse(AylaCollection response) {
                        successListener.onResponse(response);
                    }
                }, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchCollections(
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return fetchCollections(new HashMap<>(), successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchCollections(
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(endPointForCollections);
        if (queryParams != null && queryParams.size() > 0) {
            url = URLHelper.appendParameters(url, queryParams);
        }

        AylaAPIRequest<AylaCollection.CollectionsWrapper> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaCollection.CollectionsWrapper.class,
                getSessionManager(),
                new Response.Listener<AylaCollection.CollectionsWrapper>() {
                    @Override
                    public void onResponse(AylaCollection.CollectionsWrapper wrapper) {
                        if (wrapper == null || wrapper.collections == null) {
                            successListener.onResponse(new AylaCollection[]{});
                        } else {
                            successListener.onResponse(wrapper.collections);
                        }
                    }
                }, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchCollections(
            @NonNull AylaCollectionFiltersBuilder filtersBuilder,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {
        return fetchCollections(filtersBuilder.build(), successListener, errorListener);
    }

    @Override
    public AylaAPIRequest fetchCollectionsHavingDSN(
            @NonNull String dsn,
            @NonNull String type,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForDevice, dsn));

        AylaCollectionFiltersBuilder filtersBuilder = new AylaCollectionFiltersBuilder()
                .withCollectionType(type);
        Map<String, String> filters = filtersBuilder.build();
        if (filters != null && filters.size() > 0) {
            url = URLHelper.appendParameters(url, filters);
        }

        AylaAPIRequest<AylaCollection.CollectionsWrapper> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaCollection.CollectionsWrapper.class,
                getSessionManager(),
                new Response.Listener<AylaCollection.CollectionsWrapper>() {
                    @Override
                    public void onResponse(AylaCollection.CollectionsWrapper wrapper) {
                        if (wrapper != null && wrapper.collections != null) {
                            successListener.onResponse(wrapper.collections);
                        } else {
                            successListener.onResponse(new AylaCollection[]{});
                        }
                    }
                }, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchCollectionsHavingDSN(
            @NonNull String dsn,
            @NonNull String type,
            @Nullable Map<String, String> queryParams,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForDevice, dsn));

        if (queryParams != null && queryParams.size() > 0) {
            url = URLHelper.appendParameters(url, queryParams);
        }

        AylaAPIRequest<AylaCollection.CollectionsWrapper> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaCollection.CollectionsWrapper.class,
                getSessionManager(),
                new Response.Listener<AylaCollection.CollectionsWrapper>() {
                    @Override
                    public void onResponse(AylaCollection.CollectionsWrapper wrapper) {
                        if (wrapper != null && wrapper.collections != null) {
                            successListener.onResponse(wrapper.collections);
                        } else {
                            successListener.onResponse(new AylaCollection[]{});
                        }
                    }
                }, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchCollections(
            @NonNull String type,
            @NonNull Response.Listener<AylaCollection[]> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(endPointForCollections);

        AylaCollectionFiltersBuilder filtersBuilder = new AylaCollectionFiltersBuilder()
                .withCollectionType(type);
        Map<String, String> filters = filtersBuilder.build();
        if (filters != null && filters.size() > 0) {
            url = URLHelper.appendParameters(url, filters);
        }

        AylaAPIRequest<AylaCollection.CollectionsWrapper> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaCollection.CollectionsWrapper.class,
                getSessionManager(),
                new Response.Listener<AylaCollection.CollectionsWrapper>() {
                    @Override
                    public void onResponse(AylaCollection.CollectionsWrapper wrapper) {
                        if (wrapper != null && wrapper.collections != null) {
                            successListener.onResponse(wrapper.collections);
                        } else {
                            successListener.onResponse(new AylaCollection[]{});
                        }
                    }
                }, errorListener);
        sendRequest(request);

        return request;
    }

    public AylaAPIRequest fetchCollectionAttributeMap(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForAttributes, collectionUUID));
        AylaAPIRequest<Map> request = new AylaAPIRequest<Map>(
                Request.Method.GET, url, null,
                Map.class,
                getSessionManager(),
                new Response.Listener<Map>() {
                    @Override
                    public void onResponse(Map response) {
                        successListener.onResponse(response);
                    }
                }, errorListener);
                sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchAttributesForCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<Map<String, String>> successListener,
            @NonNull ErrorListener errorListener) {
        return fetchCollectionAttributeMap(collectionUUID, new Response.Listener<Map<String, String>>() {
            @Override
            public void onResponse(Map<String, String> response) {
                if (response == null)
                    successListener.onResponse(new HashMap<>());
                else
                    successListener.onResponse(response);
            }
        }, errorListener);
    }

    @Override
    public AylaAPIRequest updateStatesOfCollectionResources(
            @NonNull String collectionUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForCollectionResources, collectionUUID));
        AylaCollection collection = new AylaCollection();
        collection.devices = devices;
        collection.childCollections = childCollections;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest<AylaCollection> request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest updateStatesOfCollectionResources(
            @NonNull AylaCollection collection,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        if (collection.collectionUuid == null) {
            errorListener.onErrorResponse(new InvalidArgumentError("null uuid"));
            return null;
        }

        return updateStatesOfCollectionResources(
                collection.collectionUuid,
                devices,
                childCollections,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateAttributesForCollection(
            @NonNull String collectionUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForAttributes, collectionUUID));
        AylaCollection collection = new AylaCollection();
        collection.custom_attributes = attributes;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest<AylaCollection> request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest updateAttributesForCollection(
            @NonNull AylaCollection collection,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        if (collection.collectionUuid == null) {
            errorListener.onErrorResponse(new InvalidArgumentError("null uuid"));
            return null;
        }

        return updateAttributesForCollection(
                collection.collectionUuid,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest updateCollectionName(
            @NonNull String collectionUUID,
            @NonNull String name,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForCollection, collectionUUID));
        AylaCollection newCollection = new AylaCollection();
        newCollection.name = name;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(newCollection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest deleteCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForCollection, collectionUUID));
        AylaAPIRequest<EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null,
                EmptyResponse.class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest deleteCollections(
            @NonNull List<String> collectionUUIDs,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        AylaAPIRequest originalRequest = AylaAPIRequest.dummyRequest(
                EmptyResponse.class, successListener, errorListener);
        deleteCollectionsRecursively(originalRequest, collectionUUIDs,
                successListener, errorListener);
        return originalRequest;
    }

    @Override
    public AylaAPIRequest deleteAllCollections(
            @NonNull String type,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(endPointForCollections);
        url = url + "?type=" + type;

        AylaAPIRequest<EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE,
                url,
                null,
                EmptyResponse.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest deleteResourcesFromCollection(
            @NonNull String collectionUUID,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForResourcesDeletion, collectionUUID));
        AylaCollection collection = new AylaCollection();
        collection.devices = devices;
        collection.childCollections = childCollections;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest deleteAttributesFromCollection(
            @NonNull String collectionUUID,
            @Nullable Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForAttributesDeletion, collectionUUID));
        AylaCollection collection = new AylaCollection();
        JSONObject attrObject = new JSONObject();
        JSONArray attrArray = new JSONArray();
        JSONObject attrMainObject = new JSONObject();
        try {
            if(attributes.keySet().toArray()[0] != null) {
                attrObject.put("key", attributes.keySet().toArray()[0]);
                attrArray.put(attrObject);
                attrMainObject.put("custom_attributes", attrArray);
            }
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();
        }
        String jsonBody = attrMainObject.toString();

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    //TODO make this concurrent, return list of failed deletions
    final void deleteCollectionsRecursively(
            @NonNull AylaAPIRequest originalRequest,
            @NonNull List<String> collectionUUIDs,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        if (originalRequest.isCanceled()) {
            AylaLog.d(LOG_TAG, "deleteCollections request was cancelled");
        } else if (collectionUUIDs.size() == 0) {
            successListener.onResponse(new EmptyResponse());
        } else {
            String uuid = collectionUUIDs.remove(0);
            originalRequest.setChainedRequest(deleteCollection(uuid,
                    new Response.Listener<EmptyResponse>() {
                        @Override
                        public void onResponse(EmptyResponse response) {
                            if (collectionUUIDs.size() > 0) {
                                deleteCollectionsRecursively(originalRequest, collectionUUIDs,
                                        successListener, errorListener);
                            } else {
                                successListener.onResponse(new EmptyResponse());
                            }
                        }
                    }, errorListener));
        }
    }

    @Override
    public AylaAPIRequest addResourcesToCollection(
            @NonNull String collectionUUID,
            @NonNull String type,
            @Nullable AylaCollectionDevice[] devices,
            @Nullable AylaChildCollection[] childCollections,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForCollectionResources, collectionUUID)) + "?type=" + type;

        AylaCollection collection = new AylaCollection();
        collection.devices = devices;
        collection.childCollections = childCollections;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest addAttributesToCollection(
            @NonNull AylaCollection collection,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        return addAttributesToCollection(
                collection.collectionUuid,
                attributes,
                successListener,
                errorListener);
    }

    @Override
    public AylaAPIRequest addAttributesToCollection(
            @NonNull String collectionUUID,
            @NonNull Map<String, String> attributes,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForAttributes, collectionUUID));

        AylaCollection alyaCollection = new AylaCollection();
        alyaCollection.custom_attributes = attributes;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(alyaCollection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest updateValueForCollectionProperties(
            @NonNull String collectionUUID,
            @NonNull AylaCollectionProperty[] properties,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForPropertyChange, collectionUUID));

        AylaCollectionDevice collectionDevice = new AylaCollectionDevice();
        collectionDevice.properties = properties;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collectionDevice);

        AylaAPIRequest request = new AylaJsonRequest<AylaCollectionTriggerResponse[]>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                AylaCollectionTriggerResponse[].class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);
        return request;
    }

    final AylaSessionManager getSessionManager() {
        return _sessionManagerRef.get();
    }

    final void sendRequest(AylaAPIRequest request) {
        if (getSessionManager() != null && getSessionManager().getDeviceManager() != null) {
            getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        } else {
            AylaLog.w(LOG_TAG, "unable to send request." + request.toString());
        }
    }

    final String getServiceUrl(String path) {
        return AylaNetworks.sharedInstance().getServiceUrl(ServiceUrls.CloudService.GSS, path);
    }

    @Override
    public AylaAPIRequest triggerCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<AylaCollectionTriggerResponse[]> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForActivateSceneManually, collectionUUID));
        AylaCollection aylaCollection = new AylaCollection();
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(aylaCollection);

        AylaAPIRequest request = new AylaJsonRequest<AylaCollectionTriggerResponse[]>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                AylaCollectionTriggerResponse[].class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest enableTriggerCollection(
            @NonNull String collectionUUID,
            @NonNull boolean enable,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForSceneAutomation, collectionUUID))
                + "?is_active=" + enable;

        AylaCollection aylaCollection = new AylaCollection();
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(aylaCollection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest addScheduleToCollection(
            @NonNull String collectionUUID,
            @Nullable AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForSchedule, collectionUUID));
        AylaCollection collection = new AylaCollection();
        collection.schedule = schedule;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);
        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest updateScheduleForCollection(
            @NonNull String collectionUUID,
            @NonNull AylaSchedule schedule,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForScheduleUpdate, collectionUUID));
        AylaCollection collection = new AylaCollection();
        collection.schedule = schedule;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest<AylaCollection> request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest deleteScheduleFromCollection(
            @NonNull String collectionUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForScheduleDeletion, collectionUUID));
        AylaAPIRequest<EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null,
                EmptyResponse.class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest shareCollection(
            @NonNull AylaShare share,
            @NonNull Response.Listener<AylaShare> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(endPointForShare);
        AylaCollection collection = new AylaCollection();
        collection.share = share;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.POST,
                url,
                jsonBody,
                null,
                CollectionShareWrapper.class,
                getSessionManager(),
                new Response.Listener<CollectionShareWrapper>() {
                    @Override
                    public void onResponse(CollectionShareWrapper response) {
                        successListener.onResponse(response.share);
                    }
                },
                errorListener);
        sendRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest fetchOwnedShares(
            @NonNull Response.Listener<AylaShare[]> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(endPointForShare);
        AylaAPIRequest<AylaShare[]> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaShare[].class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchReceivedShares(
            @NonNull Response.Listener<AylaShare[]> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(endPointForReceivedShare);
        AylaAPIRequest<AylaShare[]> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaShare[].class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest updateShare(
            @NonNull String shareUUID,
            @NonNull AylaShare share,
            @NonNull Response.Listener<AylaShare> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForShareUpdate, shareUUID));
        AylaCollection collection = new AylaCollection();
        collection.share = share;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                CollectionShareWrapper.class,
                getSessionManager(),
                new Response.Listener<CollectionShareWrapper>() {
                    @Override
                    public void onResponse(CollectionShareWrapper response) {
                        successListener.onResponse(response.share);
                    }
                }, errorListener);
        sendRequest(request);

        return request;

    }

    @Override
    public AylaAPIRequest deleteShare(
            @NonNull String shareUUID,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getServiceUrl(String.format(endPointForShareUpdate, shareUUID));
        AylaAPIRequest<EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null,
                EmptyResponse.class,
                getSessionManager(),
                successListener,
                errorListener);
        sendRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest removeDevicesFromCollection(
            @NonNull String collectionUUID,
            @NonNull AylaCollectionDevice[] devices,
            @NonNull boolean recursively,
            @NonNull Response.Listener<AylaCollection> successListener,
            @NonNull ErrorListener errorListener) {

        String url = getServiceUrl(String.format(endPointForRemoveDevice, collectionUUID));
        AylaCollection collection = new AylaCollection();
        collection.devices = devices;
        String jsonBody = AylaNetworks.sharedInstance().getGson().toJson(collection);

        AylaAPIRequest request = new AylaJsonRequest<>(
                Request.Method.PUT,
                url,
                jsonBody,
                null,
                AylaCollection.class,
                getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }
}
