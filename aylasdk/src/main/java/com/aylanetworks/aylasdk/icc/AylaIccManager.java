package com.aylanetworks.aylasdk.icc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.aylanetworks.aylasdk.AylaAPIRequest;
import com.aylanetworks.aylasdk.AylaDevice;
import com.aylanetworks.aylasdk.AylaDeviceManager;
import com.aylanetworks.aylasdk.AylaJsonRequest;
import com.aylanetworks.aylasdk.AylaLog;
import com.aylanetworks.aylasdk.AylaNetworks;
import com.aylanetworks.aylasdk.AylaSessionManager;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.InvalidArgumentError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.icc.userconsent.AylaUserConsentJob;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.aylanetworks.aylasdk.util.ServiceUrls;
import com.aylanetworks.aylasdk.util.URLHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Ayla IoT Command Center service implementation that is dedicated for device jobs
 * management, especially user consent jobs.
 */
public class AylaIccManager {

    private static final String LOG_TAG = "IccMgr";

    private WeakReference<AylaSessionManager> _sessionManagerRef;

    public AylaIccManager(@NonNull AylaSessionManager sessionManager) {
        _sessionManagerRef = new WeakReference<>(sessionManager);
    }

    /**
     * Fetches all available user consent OTA jobs that are associated with the devices
     * owned by current user.
     *
     * Note that for an OEM Admin user, all available user consent OTA jobs under all
     * users within the OEM scope will be returned, use {@link #fetchUserConsentOTAJobs(String[],
     * Response.Listener, ErrorListener)} instead to fetch jobs in particular for a set
     * of specified devices.
     *
     * @param successListener Listener called upon success, with an array of devices which
     *                        each contains available user consent jobs, or an empty array
     *                        if none jobs that match the query parameters were available.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    public AylaAPIRequest fetchUserConsentOTAJobs(
            @NonNull Response.Listener<AylaUserConsentJob.Device[]> successListener,
            @NonNull ErrorListener errorListener) {
        return fetchUserConsentOTAJobs(null, successListener, errorListener);
    }

    /**
     * Fetches all available user consent OTA jobs that are associated with the specified devices.
     * @param dsns Device DSNs for the target user consent jobs, passing null to
     *             fetch all device jobs that belong to the current user. Note that passing null
     *             for an OEM:Admin user may return OEM-wide jobs that might span multiple users.
     *             Note also, for cases where available jobs might span multiple pages, only the
     *             first page of the result jobs will be returned, use {@link #fetchDeviceJobs(
     *             AylaUserConsentJob.FiltersBuilder, Response.Listener, ErrorListener)} instead to
     *             specify more detailed query criteria so as to return page specific jobs.
     * @param successListener Listener called upon success, with an array of devices which
     *                        each contains available user consent jobs, or an empty array
     *                        if none jobs that match the query parameters were available.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    public AylaAPIRequest fetchUserConsentOTAJobs(
            @Nullable String[] dsns,
            @NonNull  Response.Listener<AylaUserConsentJob.Device[]> successListener,
            @NonNull  ErrorListener errorListener) {
        AylaUserConsentJob.FiltersBuilder builder = new AylaUserConsentJob.FiltersBuilder()
                .withStatus(new String[]{AylaUserConsentJob.DeviceStatus.CONSENT})
                .withJobType(AylaUserConsentJob.JobType.OTA)
                .withDSNs(dsns);
        return fetchDeviceJobs(builder.map(),successListener, errorListener);
    }


    /**
     * Fetches device jobs that match the specified query parameters.
     * @param filters a map of pre-defined job filters in a key-value format.
     *        see {@link AylaUserConsentJob.FiltersBuilder} for the full list of
     *        available query parameters.
     * @param successListener Listener called upon success, with an array of devices which
     *                        each contains available user consent jobs, or an empty array
     *                        if none jobs that match the query parameters were available.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    protected AylaAPIRequest fetchDeviceJobs(
            @Nullable Map<String, String> filters,
            @NonNull  Response.Listener<AylaUserConsentJob.Device[]> successListener,
            @NonNull  ErrorListener errorListener) {
        String url = getICCServiceUrl("icc/v1/users/device_jobs");
        if (filters != null && filters.size() > 0) {
            url = URLHelper.appendParameters(url, filters);
        }

        AylaAPIRequest<AylaUserConsentJob> request = new AylaAPIRequest<>(
                Request.Method.GET, url, null,
                AylaUserConsentJob.class, getSessionManager(),
                new Response.Listener<AylaUserConsentJob>() {
                    @Override
                    public void onResponse(AylaUserConsentJob response) {
                        List<AylaUserConsentJob.Device> devices = new ArrayList<>();
                        if (response != null && response.devices != null
                                && response.devices.length > 0) {
                            for (AylaUserConsentJob.Device device : response.devices) {
                                if (device.device_jobs != null && device.device_jobs.length > 0) {
                                    devices.add(device);
                                }
                            }
                        }
                        int len = devices.size();
                        AylaUserConsentJob.Device[] resultsArray = new AylaUserConsentJob.Device[len];
                        successListener.onResponse(devices.toArray(resultsArray));
                    }
                }, errorListener);

        sendRequest(request);
        return request;
    }

    /**
     * Fetch device jobs with query parameters from the given filters builder.
     * @param filtersBuilder builder for query parameters.
     * @param successListener Listener called upon success, with an array of devices which
     *                        each contains available user consent jobs, or an empty array
     *                        if none jobs that match the query parameters were available.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    protected AylaAPIRequest fetchDeviceJobs(
            @NonNull AylaUserConsentJob.FiltersBuilder filtersBuilder,
            @NonNull Response.Listener<AylaUserConsentJob.Device[]> successListener,
            @NonNull ErrorListener errorListener) {

        try {
            Preconditions.checkNotNull(filtersBuilder, "filters is null");
            Preconditions.checkNotNull(successListener, "success listener is null");
            Preconditions.checkNotNull(errorListener, "error listener is null");
        } catch (NullPointerException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
            }
            return null;
        }

        return fetchDeviceJobs(filtersBuilder.map(), successListener, errorListener);
    }


    /**
     * Fetch device jobs with query parameters from the given filters builder.
     * @param filtersBuilder builder for query parameters.
     * @param successListener Listener called upon success, with an array of devices which
     *                        each contains available user consent jobs, or an empty array
     *                        if none jobs that match the query parameters were available.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    public AylaAPIRequest fetchDeviceJobsWithQueryParams(
            @NonNull AylaUserConsentJob.FiltersBuilder filtersBuilder,
            @NonNull Response.Listener<AylaUserConsentJob.Device[]> successListener,
            @NonNull ErrorListener errorListener) {

        try {
            Preconditions.checkNotNull(filtersBuilder, "filters is null");
            Preconditions.checkNotNull(successListener, "success listener is null");
            Preconditions.checkNotNull(errorListener, "error listener is null");
        } catch (NullPointerException e) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
            }
            return null;
        }

        return fetchDeviceJobs(filtersBuilder.map(), successListener, errorListener);
    }

    /**
     * Schedules the specified user consent job to be executed at a given timestamp.
     * @param job the job to be scheduled for execution.
     * @param deviceDSN DSN of the device with which the job is associated.
     * @param scheduleTimestamp the time at which the job is going to be executed.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    public AylaAPIRequest scheduleUserConsentJob(
            @NonNull AylaUserConsentJob.DeviceJob job,
            @NonNull String deviceDSN,
            @NonNull Date scheduleTimestamp,
            @NonNull Response.Listener<AylaUserConsentJob.DeviceJob> successListener,
            @NonNull ErrorListener errorListener) {
        return cancelOrScheduleDeviceJob(job, deviceDSN, false,
                scheduleTimestamp, successListener, errorListener);
    }

    /**
     * Cancels (or stops) the specified user consent job.
     * @param job the job to be cancelled.
     * @param deviceDSN DSN of the device with which the job is associated.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    public AylaAPIRequest cancelUserConsentJob(
            @NonNull AylaUserConsentJob.DeviceJob job,
            @NonNull String deviceDSN,
            @NonNull Response.Listener<AylaUserConsentJob.DeviceJob> successListener,
            @NonNull ErrorListener errorListener) {
        return cancelOrScheduleDeviceJob(job, deviceDSN, true,
                null, successListener, errorListener);
    }

    /**
     * Cancels or schedules the specified device job.
     * @param job the job to be cancelled or scheduled.
     * @param cancel, mandatory, set true to cancel the job, and false to schedule jobs for execution
     *                at the timestamp as specified in the <code>scheduleTimestamp</code> parameter.
     * @param scheduleTimestamp, optional, the target time the jobs are scheduled for execution.
     *                           set to null to execute the job immediately. It's meaningful
     *                           only when the <code>cancel</code> parameter is false.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    private AylaAPIRequest cancelOrScheduleDeviceJob(
            @NonNull  AylaUserConsentJob.DeviceJob job,
            @NonNull  String deviceDSN,
            @NonNull  Boolean cancel,
            @Nullable Date scheduleTimestamp,
            @NonNull  Response.Listener<AylaUserConsentJob.DeviceJob> successListener,
            @NonNull  ErrorListener errorListener) {

        try {
            Preconditions.checkNotNull(job, "job is null");
            Preconditions.checkNotNull(deviceDSN, "device DSN is null");
            Preconditions.checkNotNull(successListener, "success listener is null");
            Preconditions.checkNotNull(errorListener, "error listener is null");
        } catch (NullPointerException e) {
            errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
            return null;
        }

        JSONObject body = new JSONObject();
        try {
            boolean cancelJob = cancel.booleanValue();
            long secondsSinceEpoch = scheduleTimestamp == null ? 0
                    : scheduleTimestamp.getTime()/1000;
            body.put("cancel_job", cancelJob);
            body.put("schedule_timestamp", secondsSinceEpoch);
        } catch (JSONException e) {
            AylaLog.e(LOG_TAG, e.getMessage());
            errorListener.onErrorResponse(new InvalidArgumentError(e.getMessage()));
            return null;
        }

        String path = String.format("icc/v1/dsns/%s/device_jobs/%s", deviceDSN, job.id);
        String url = getICCServiceUrl(path);

        AylaJsonRequest<AylaUserConsentJob.DeviceJob> request = new AylaJsonRequest<>(
                Request.Method.PUT, url, body.toString(), null,
                AylaUserConsentJob.DeviceJob.class, getSessionManager(),
                successListener, errorListener);
        sendRequest(request);

        return request;
    }

    /**
     * Cancels or schedules a batch of specified device jobs.
     * @param devices an array of devices the jobs associated with.
     * @param cancel, mandatory, set true to cancel the jobs, and false to schedule the jobs for
     *                execution at the timestamp as specified in the <code>scheduleTimestamp</code> parameter.
     * @param scheduleTimestamp, optional, the target time the jobs are scheduled for execution.
     *                           set to null to execute the jobs immediately. It's meaningful
     *                           only when the <code>cancel</code> parameter is false.
     * @param successListener Listener called upon success.
     * @param errorListener Listener called with an error should one occur.
     * @return the AylaAPIRequest, which may be used to cancel the operation.
     */
    protected AylaAPIRequest cancelOrScheduleDeviceJobBatch(
            @NonNull  AylaUserConsentJob.Device[] devices,
            @NonNull  Boolean cancel,
            @Nullable Date scheduleTimestamp,
            @Nullable Response.Listener<AylaUserConsentJob.UpdateUserConsentJobsResult> successListener,
            @Nullable ErrorListener errorListener) {
        if (devices == null || devices.length == 0) {
            return null;
        }


        boolean cancelJob = cancel.booleanValue();
        long secondsSinceEpoch = scheduleTimestamp == null ? 0 : scheduleTimestamp.getTime()/1000;

        JSONObject devicesJobsJSON = new JSONObject();
        JSONArray devicesArray = new JSONArray();

        for (AylaUserConsentJob.Device device : devices) {
            AylaUserConsentJob.DeviceJob[] jobs = device.device_jobs;
            if (jobs != null && jobs.length > 0) {
                JSONObject deviceJobsJSON = new JSONObject();
                JSONArray jobsArray = new JSONArray();
                try {
                    deviceJobsJSON.put("dsn", device.dsn);
                    for (AylaUserConsentJob.DeviceJob job : jobs) {
                        JSONObject jobJSON = new JSONObject();
                        jobJSON.put("id", job.id);
                        jobJSON.put("cancel_job", cancelJob);
                        jobJSON.put("schedule_timestamp", secondsSinceEpoch);
                        jobsArray.put(jobJSON);
                    }
                    deviceJobsJSON.put("device_jobs", jobsArray);
                    devicesArray.put(deviceJobsJSON);
                } catch (JSONException e) {
                    AylaLog.e(LOG_TAG, e.getMessage());
                }
            }
        }

        if (devicesArray.length() == 0) {
            if (errorListener != null) {
                errorListener.onErrorResponse(new PreconditionError("no valid device jobs"));
                return null;
            }
        }

        try {
            devicesJobsJSON.put("devices", devicesArray);
        } catch (JSONException e) {
            AylaLog.e(LOG_TAG, e.getMessage());
        }

        String url = getICCServiceUrl("icc/v1/users/device_jobs");
        AylaJsonRequest<AylaUserConsentJob.UpdateUserConsentJobsResult> request = new AylaJsonRequest<>(
                Request.Method.PUT, url, devicesJobsJSON.toString(), null,
                AylaUserConsentJob.UpdateUserConsentJobsResult.class, getSessionManager(),
                successListener, errorListener);
        sendRequest(request);
        return request;
    }

    /**
     * Return the session manager associated with this service, maybe null
     * if the session manager is no longer valid.
     */
    protected AylaSessionManager getSessionManager() {
        return _sessionManagerRef.get();
    }

    /**
     * Returns the URL of a service endpoint ended with the specified path.
     */
    protected String getICCServiceUrl(String path) {
        return AylaNetworks.sharedInstance().getServiceUrl(ServiceUrls.CloudService.ICC, path);
    }

    /**
     * Enqueues the provided request to the Ayla Cloud ICC Service.
     *
     * @param request the request to send
     * @return the AylaAPIRequest for this operation
     */
    protected AylaAPIRequest sendRequest(AylaAPIRequest<?> request) {
        if (getSessionManager() == null) {
            AylaLog.w(LOG_TAG, "ignore request " + request
                    + " as session manager is no longer active");
        }

        return getSessionManager().sendUserServiceRequest(request);
    }
}
