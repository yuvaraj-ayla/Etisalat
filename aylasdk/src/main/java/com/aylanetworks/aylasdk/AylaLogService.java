/*
 * Copyright 2016 Ayla Networks, all rights reserved
 */
package com.aylanetworks.aylasdk;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.util.Preconditions;
import com.aylanetworks.aylasdk.util.ServiceUrls;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * AylaLogService class is responsible for sending log messages to Ayla Log service. This class
 * is used to send logs collected in the mobile phone when the device is not connected to the
 * internet.
 */
public class AylaLogService {

    private final static String LOG_TAG = "AYLA_LOG_SERVICE";
    private final static String LOG_URL_PATH = "api/v1/app/logs.json";

    private final RequestQueue _logServiceRequestQueue;
    private final WeakReference<AylaSessionManager> _sessionManagerRef;
    private final List<String> _logList;
    private String _dsn;

    public AylaLogService(@NonNull AylaSessionManager sessionManager) {
        Context context = AylaNetworks.sharedInstance().getContext();
        Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);
        Network network = new BasicNetwork(new HurlStack());
        _logServiceRequestQueue = new RequestQueue(cache, network, 1);
        _sessionManagerRef = new WeakReference<>(sessionManager);
        _logList = new LinkedList<>();
    }

    public void start() {
        _logServiceRequestQueue.start();
    }

    public void stop() {
        _logServiceRequestQueue.stop();
    }

    public void setDsn(String dsn) {
        _dsn = dsn;
    }

    /**
     * Add logs with given parameters to the AylaLogService queue.
     *
     * @param module Mobile library module from which the logs are posted.
     * @param level  Log level.
     * @param time   Current time
     * @param text   Log description.
     */
    public void addLog(String module, String level, String time, String text) {
        if (time == null) {
            time = String.valueOf(System.currentTimeMillis());
        }

        try {
            JSONObject logJson = new JSONObject();
            logJson.put("time", time);
            logJson.put("mod", module);
            logJson.put("text", text);
            logJson.put("level", level);
            _logList.add(logJson.toString());
        } catch (JSONException e) {
            AylaLog.d(LOG_TAG, "failed to add log message:" + text);
        }
    }

    /**
     * Add logs with given parameters to the AylaLogService queue, using
     * current time by default.
     *
     * @param module Mobile library module from which the logs are posted.
     * @param level  Log level.
     * @param text   Log description.
     */
    public void addLog(String module, String level, String text) {
        addLog(module, level, String.valueOf(System.currentTimeMillis()), text);
    }

    private String createLogsJSON(String dsn, List<String> logs) {
        try {
            Preconditions.checkArgument(dsn != null, "DSN is null, logs not created");
            Preconditions.checkArgument(logs.isEmpty(), "no logs available");
        } catch (IllegalArgumentException e) {
            AylaLog.e(LOG_TAG, e.getMessage());
            return null;
        }

        JSONArray logArray = new JSONArray();
        for (String log : logs) {
            try {
                JSONObject logJson = new JSONObject(log);
                logArray.put(logJson);
            } catch (JSONException e) {
                AylaLog.e(LOG_TAG, e.getMessage());
            }
        }

        JSONObject logsJson = new JSONObject();
        try {
            logsJson.put("dsn", dsn);
            logsJson.put("logs", logArray);
        } catch (JSONException e) {
            AylaLog.d(LOG_TAG, "JSONException in createLogs");
        }

        return logsJson.toString();
    }

    /**
     * Send queued up messages to the Ayla Log service, and clear the queue.
     * This method is to be called after the mobile reconnects to internet connected LAN.
     */
    public void sendToLogService() {
        List<String> backup = new ArrayList<>(_logList);
        String logJson = createLogsJSON(_dsn, backup);
        if (logJson == null) {
            AylaLog.d(LOG_TAG, "No logs to send");
            return;
        }

        if (_sessionManagerRef.get() == null) {
            AylaLog.e(LOG_TAG, "No session manager");
            return;
        }

        // Sending logs is a async process, clear existing logs
        // here so new logs can be accepted.
        _logList.clear();

        String url = AylaNetworks.sharedInstance().getServiceUrl(ServiceUrls.CloudService.Log, LOG_URL_PATH);
        AylaJsonRequest<AylaAPIRequest.EmptyResponse> request = new AylaJsonRequest<>(
                Request.Method.POST, url, logJson, null, AylaAPIRequest.EmptyResponse.class,
                _sessionManagerRef.get(), new Response.Listener<AylaAPIRequest.EmptyResponse>() {
            @Override
            public void onResponse(AylaAPIRequest.EmptyResponse response) {
                AylaLog.d(LOG_TAG, "Log upload success");
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(AylaError error) {
                AylaLog.d(LOG_TAG, "Log upload failed " + error.getLocalizedMessage());
                _logList.addAll(backup);
            }
        });

        request.setShouldCache(false);
        request.logResponse();
        _logServiceRequestQueue.add(request);
    }
}
