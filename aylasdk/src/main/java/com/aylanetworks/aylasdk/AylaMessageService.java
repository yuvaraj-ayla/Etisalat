package com.aylanetworks.aylasdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest.EmptyResponse;
import com.aylanetworks.aylasdk.ams.dest.AylaDestination;
import com.aylanetworks.aylasdk.error.ErrorListener;

import java.util.List;

public interface AylaMessageService {

    /**
     * Creates a new destination in the cloud.
     *
     * @param destination  the destination to be created, could be any sub-type of AylaDestination.
     * @param successListener Listener called on successful creation of the destination.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to create the destination.
     */
    AylaAPIRequest createDestination(
            @NonNull AylaDestination destination,
            @NonNull Response.Listener<AylaDestination> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Creates a batch of destinations in the cloud.
     *
     * @param destinations the destinations to be created, only destinations that doesn't have
     *                     valid UUID will be created, otherwise, the destination is thought to
     *                     be created.
     * @param successListener Listener called on successful creation of the destinations.
     *                        The number of destinations actually created might be less than or
     *                        equal to the number to be created. The caller should check this to
     *                        make sure the destinations are created as expected.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to create the destination.
     */
    AylaAPIRequest createDestinations(
            @NonNull List<AylaDestination> destinations,
            @NonNull Response.Listener<List<AylaDestination>> successListener,
            @NonNull ErrorListener errorListener);

    /**
     * Updates the specified destination in the cloud.
     *
     * @param destination  the destination to be updated, could be any sub-type of AylaDestination.
     * @param successListener Listener called on successful update of the destination.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to update the destination.
     */
    AylaAPIRequest updateDestination(@NonNull AylaDestination destination,
                                     @NonNull Response.Listener<AylaDestination> successListener,
                                     @NonNull ErrorListener errorListener);

    /**
     * Fetches destination with the destination UUID.
     *
     * @param uuid unique ID of the destination to be fetched.
     * @param successListener Listener called on successful retrieval of the destination.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to fetch the destination.
     */
    AylaAPIRequest fetchDestinationWithId(@NonNull String uuid,
                                          @NonNull Response.Listener<AylaDestination> successListener,
                                          @NonNull ErrorListener errorListener);

    /**
     * Fetches destinations with a list of destination UUIDs.
     *
     * @param uuids IDs of the destinations to be fetched.
     * @param successListener Listener called on successful retrieval of the destinations.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to fetch the destination.
     */
    AylaAPIRequest fetchDestinations(@NonNull List<String> uuids,
                                     @NonNull Response.Listener<List<AylaDestination>> successListener,
                                     @NonNull ErrorListener errorListener);

    /**
     * Fetches destinations with the specified types.
     *
     * @param types Array of AylaDestinationTypes strings to fetch destination, if not provided all
     *              types will be returned.
     * @param successListener Listener called on successful retrieval of the destination.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to fetch the destination.
     */
    AylaAPIRequest fetchDestinationsWithTypes(@Nullable String[] types,
                                              @NonNull Response.Listener<AylaDestination[]> successListener,
                                              @NonNull ErrorListener errorListener);

    /**
     * Deletes the specified destination in the cloud.
     *
     * @param uuid  uuid of the destination to be deleted.
     * @param successListener Listener called on successful deletion of the destination.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to delete the destination.
     */
    AylaAPIRequest deleteDestinationWithUUID(@NonNull String uuid,
                                             @NonNull Response.Listener<EmptyResponse> successListener,
                                             @NonNull ErrorListener errorListener);

    /**
     * Deletes the specified destinations in the cloud.
     *
     * @param destinationUUIDs  UUIDs of the destinations to be deleted.
     * @param successListener Listener called on successful deletion of the destination.
     * @param errorListener Listener called with an <code>AylaError</code> should one occur.
     * @return the request used to delete the destination.
     */
    AylaAPIRequest deleteDestinations(@NonNull List<String> destinationUUIDs,
                                      @NonNull Response.Listener<EmptyResponse> successListener,
                                      @NonNull ErrorListener errorListener);
}
