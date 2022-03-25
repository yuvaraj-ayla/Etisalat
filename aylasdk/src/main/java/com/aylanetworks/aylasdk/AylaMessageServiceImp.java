package com.aylanetworks.aylasdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest.EmptyResponse;
import com.aylanetworks.aylasdk.ams.dest.AylaDestination;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.InvalidArgumentError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.aylanetworks.aylasdk.util.ServiceUrls;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AylaMessageServiceImp implements AylaMessageService {

    public static final String LOG_TAG = "AMS";

    private static final String PATH_TO_DESTINATIONS = "messageservice/v1/destinations";
    private static final String PATH_TO_DESTINATION  = "messageservice/v1/destinations/%s";

    private final WeakReference<AylaSessionManager> _sessionManagerRef;

    protected AylaMessageServiceImp(AylaSessionManager sessionManager) {
        _sessionManagerRef = new WeakReference<>(sessionManager);
    }

    @Override
    public AylaAPIRequest createDestination(
            @NonNull AylaDestination destination,
            @NonNull Response.Listener<AylaDestination> successListener,
            @NonNull ErrorListener errorListener) {
        try {
            Preconditions.checkArgument(destination != null, "destination is null");
            Preconditions.checkState(getSessionManager() != null, "no active session");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
                return null;
            }
        }

        String url = getMessageServiceUrl(PATH_TO_DESTINATIONS);
        AylaDestination.Wrapper wrapper = new AylaDestination.Wrapper();
        wrapper.destination = destination;
        String payload = AylaNetworks.sharedInstance().getGson().toJson(
                wrapper, AylaDestination.Wrapper.class);

        AylaAPIRequest<AylaDestination.Wrapper> request = new AylaJsonRequest<>(
                Request.Method.POST, url, payload,
                null, AylaDestination.Wrapper.class, getSessionManager(),
                new Response.Listener<AylaDestination.Wrapper>() {
                    @Override
                    public void onResponse(AylaDestination.Wrapper response) {
                        successListener.onResponse(response.destination);
                    }
                }, errorListener);

        sendMessageServiceRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest createDestinations(@NonNull List<AylaDestination> destinations,
                                             @NonNull Response.Listener<List<AylaDestination>> successListener,
                                             @NonNull ErrorListener errorListener) {
        AylaAPIRequest originalRequest = AylaAPIRequest.dummyRequest(
                AylaDestination[].class, successListener, errorListener);
        int size = destinations.size();
        List<AylaDestination> sourceDestinations = destinations;
        List<AylaDestination> createdDestinations = new ArrayList<>(size);
        createDestinationsInternal(originalRequest, sourceDestinations,
                createdDestinations, successListener, errorListener);
        return originalRequest;
    }

    private void createDestinationsInternal(
            @NonNull AylaAPIRequest originalRequest,
            @NonNull List<AylaDestination> sourceDestinations,
            @NonNull List<AylaDestination> createdDestinations,
            @NonNull Response.Listener<List<AylaDestination>> successListener,
            @NonNull ErrorListener errorListener) {
        if (originalRequest.isCanceled()) {
            return;
        }

        if (sourceDestinations.size() == 0) {
            successListener.onResponse(createdDestinations);
            return;
        }

        AylaDestination nextDestination = sourceDestinations.remove(0);
        if (nextDestination.getUUID() != null) {
            createdDestinations.add(nextDestination);
            createDestinationsInternal(originalRequest, sourceDestinations,
                    createdDestinations, successListener, errorListener);
        } else {
            originalRequest.setChainedRequest(createDestination(nextDestination,
                    new Response.Listener<AylaDestination>() {
                        @Override
                        public void onResponse(AylaDestination response) {
                            createdDestinations.add(response);
                            createDestinationsInternal(originalRequest, sourceDestinations,
                                    createdDestinations, successListener, errorListener);
                        }
                    },
                    new ErrorListener() {
                        @Override
                        public void onErrorResponse(AylaError error) {
                            errorListener.onErrorResponse(error);
                        }
                    }));
        }
    }

    @Override
    public AylaAPIRequest fetchDestinations(@NonNull List<String> uuids,
                                            @NonNull Response.Listener<List<AylaDestination>> successListener,
                                            @NonNull ErrorListener errorListener) {
        AylaAPIRequest originalRequest = AylaAPIRequest.dummyRequest(
                AylaDestination[].class, successListener, errorListener);

        if (uuids == null || uuids.size() == 0) {
            errorListener.onErrorResponse(new PreconditionError("invalid uuids"));
            return originalRequest;
        }

        List<AylaDestination> destinationsFetched = new ArrayList<>(uuids.size());
        fetchDestinationsInternal(originalRequest, uuids, destinationsFetched,
                successListener, errorListener);

        return originalRequest;
    }

    private void fetchDestinationsInternal(
            AylaAPIRequest originalRequest,
            List<String> uuids,
            List<AylaDestination> destinationsFetched,
            Response.Listener<List<AylaDestination>> successListener,
            ErrorListener errorListener) {

        if (originalRequest.isCanceled()) {
            return;
        }

        if (uuids.size() == 0) {
            successListener.onResponse(destinationsFetched);
            return;
        }

        String uuid = uuids.remove(0);
        originalRequest.setChainedRequest(fetchDestinationWithId(uuid,
                new Response.Listener<AylaDestination>() {
                    @Override
                    public void onResponse(AylaDestination response) {
                        destinationsFetched.add(response);
                        fetchDestinationsInternal(originalRequest, uuids,
                                destinationsFetched, successListener, errorListener);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(AylaError error) {
                        errorListener.onErrorResponse(error);
                    }
                }));
    }

    @Override
    public AylaAPIRequest updateDestination(
            @NonNull AylaDestination destination,
            @NonNull Response.Listener<AylaDestination> successListener,
            @NonNull ErrorListener errorListener) {
        try {
            Preconditions.checkArgument(destination != null, "destination is null");
            Preconditions.checkArgument(destination.getUUID() != null, "destination uuid is null");
            Preconditions.checkState(getSessionManager() != null, "no active session");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
                return null;
            }
        }

        String path = String.format(PATH_TO_DESTINATION, destination.getUUID());
        String url = getMessageServiceUrl(path);
        AylaDestination.Wrapper wrapper = new AylaDestination.Wrapper();
        wrapper.destination = destination;
        String payload = AylaNetworks.sharedInstance().getGson().toJson(
                wrapper, AylaDestination.Wrapper.class);

        AylaAPIRequest<AylaDestination.Wrapper> request = new AylaJsonRequest<>(
                Request.Method.PUT, url, payload,
                null, AylaDestination.Wrapper.class, getSessionManager(),
                new Response.Listener<AylaDestination.Wrapper>() {
                    @Override
                    public void onResponse(AylaDestination.Wrapper response) {
                        successListener.onResponse(response.destination);
                    }
                }, errorListener);

        sendMessageServiceRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchDestinationWithId(
            @NonNull String uuid,
            @NonNull Response.Listener<AylaDestination> successListener,
            @NonNull ErrorListener errorListener) {
        try {
            Preconditions.checkArgument(uuid != null, "destination is null");
            Preconditions.checkState(getSessionManager() != null, "no active session");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
                return null;
            }
        }

        String path = String.format(PATH_TO_DESTINATION, uuid);
        String url = getMessageServiceUrl(path);

        AylaAPIRequest<AylaDestination.Wrapper> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaDestination.Wrapper.class, getSessionManager(),
                new Response.Listener<AylaDestination.Wrapper>() {
                    @Override
                    public void onResponse(AylaDestination.Wrapper response) {
                        successListener.onResponse(response.destination);
                    }
                }, errorListener);

        sendMessageServiceRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest fetchDestinationsWithTypes(
            @Nullable String[] types,
            @NonNull Response.Listener<AylaDestination[]> successListener,
            @NonNull ErrorListener errorListener) {
        String url = getMessageServiceUrl(PATH_TO_DESTINATIONS);
        if (types != null && types.length > 0) {
            boolean first = true;
            String firstFormat = "?type=";
            String nextFormat = "&type=";
            StringBuilder parameters = new StringBuilder();
            for (String type : types) {
                parameters.append(first ? firstFormat : nextFormat).append(type);
                first = false;
            }
            url += parameters.toString();
        }

        AylaAPIRequest<AylaDestination.DestinationsWrapper> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaDestination.DestinationsWrapper.class, getSessionManager(),
                new Response.Listener<AylaDestination.DestinationsWrapper>() {
                    @Override
                    public void onResponse(AylaDestination.DestinationsWrapper response) {
                        if (response.destinations != null) {
                            successListener.onResponse(response.destinations);
                        } else {
                            successListener.onResponse(new AylaDestination[]{});
                        }
                    }
                }, errorListener);
        sendMessageServiceRequest(request);

        return request;
    }

    @Override
    public AylaAPIRequest deleteDestinationWithUUID(
            @NonNull String uuid,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        try {
            Preconditions.checkArgument(uuid != null, "uuid is null");
            Preconditions.checkState(getSessionManager() != null, "no active session");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
                return null;
            }
        }

        String url = getMessageServiceUrl(String.format(PATH_TO_DESTINATION, uuid));
        AylaAPIRequest<AylaAPIRequest.EmptyResponse> request = new AylaAPIRequest<>(
                Request.Method.DELETE, url, null, AylaAPIRequest.EmptyResponse.class,
                getSessionManager(), successListener, errorListener);
        sendMessageServiceRequest(request);
        return request;
    }

    @Override
    public AylaAPIRequest deleteDestinations(
            @NonNull List<String> destinationUUIDs,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {
        try {
            Preconditions.checkArgument(destinationUUIDs != null, "destinationUUIDs is null");
            Preconditions.checkState(getSessionManager() != null, "no active session");
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
                return null;
            }
        }

        AylaAPIRequest dummyRequest = AylaAPIRequest.dummyRequest(
                EmptyResponse.class, successListener, errorListener);
        deleteDestinationsInternal(dummyRequest, destinationUUIDs, successListener, errorListener);
        return dummyRequest;
    }

    private void deleteDestinationsInternal(
            @NonNull AylaAPIRequest originalRequest,
            @NonNull List<String> uuids,
            @NonNull Response.Listener<EmptyResponse> successListener,
            @NonNull ErrorListener errorListener) {

        if (originalRequest.isCanceled()) {
            return;
        }

        if (uuids.size() == 0) {
            successListener.onResponse(new EmptyResponse());
            return;
        }

        String uuid = uuids.remove(0);
        originalRequest.setChainedRequest(deleteDestinationWithUUID(
                uuid,
                new Response.Listener<EmptyResponse>() {
                    @Override
                    public void onResponse(EmptyResponse response) {
                        deleteDestinationsInternal(originalRequest, uuids,
                                successListener, errorListener);
                    }
                }, errorListener));
    }

    private String getMessageServiceUrl(String path) {
        return AylaNetworks.sharedInstance().getServiceUrl(ServiceUrls.CloudService.MESSAGE, path);
    }

    private AylaSessionManager getSessionManager() {
        return _sessionManagerRef.get();
    }

    private void sendMessageServiceRequest(AylaAPIRequest request) {
        if (getSessionManager() != null && getSessionManager().getDeviceManager() != null) {
            getSessionManager().getDeviceManager().sendDeviceServiceRequest(request);
        } else {
            AylaLog.w(LOG_TAG, "unable to send request." + request.toString());
        }
    }
}
