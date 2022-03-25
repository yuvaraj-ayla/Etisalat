package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2019 Ayla Networks, all rights reserved
 */

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.NetworkError;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * AylaMessageDownloadTask is used to download the value of a message property, whose data
 * size may as much as 512kb in length.
 */
public class AylaMessageDownloadTask extends AsyncTask<Void, Long, String> {

    private static final String LOG_TAG = "AylaMessageDownloadTask";

    private AylaMessageProperty owningProperty;
    private ErrorListener errorListener;
    private Response.Listener<String> successListener;
    private MultipartProgressListener progressListener;

    private AylaError error;

    /**
     * Constructor for AylaMessageDownloadTask.
     *
     * @param owningProperty the message property to be downloaded. The base type of property must be message and
     *                       has valid property value in format "/<propery_key>/<dapoiont_id>".
     * @param progressListener Listener to receive the percentage of received data.
     * @param successListener  Listener to receive on successful download of data
     * @param errorListener    Listener to receive an Error should one occur
     */
    public AylaMessageDownloadTask(@NonNull AylaMessageProperty owningProperty,
                                   @Nullable MultipartProgressListener progressListener,
                                   @NonNull  Response.Listener<String> successListener,
                                   @NonNull  ErrorListener errorListener) {
        this.owningProperty = owningProperty;
        this.successListener = successListener;
        this.errorListener = errorListener;
        this.progressListener = progressListener;
    }

    @Override
    protected String doInBackground(Void... params) {

        //check if operation is cancelled by user
        if(progressListener != null && progressListener.isCanceled()) {
            return null;
        }

        if (owningProperty.getMessageDatapoiontId() == null) {
            error = new PreconditionError("Message datapoint ID is null");
            return null;
        }

        AylaDeviceManager deviceManager = owningProperty.getDeviceManager(errorListener);
        if (deviceManager == null) {
            error = new PreconditionError("No device manager is available");
            return null;
        }

        AylaSessionManager sessionManager = deviceManager.getSessionManager();
        if (sessionManager == null) {
            error = new PreconditionError("No session is active");
            return null;
        }

        HttpURLConnection connection = null;
        BufferedInputStream bis = null;
        ByteArrayOutputStream bos = null;

        try {
            String datapointId = owningProperty.getMessageDatapoiontId();
            String path = owningProperty.createDatapointEndpoint(datapointId);
            String url = deviceManager.deviceServiceUrl(path);

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", sessionManager.getAuthHeaderValue());
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                error = new NetworkError("Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage(), null);
                return null;
            }

            int read;
            byte buf[] = new byte[1024];
            long totalRead = 0;
            long totalSize = connection.getHeaderFieldInt("Content-Length", 0);
            Long progress[] = new Long[2];
            progress[0] = totalRead;
            progress[1] = totalSize;

            bis = new BufferedInputStream(connection.getInputStream());
            bos = new ByteArrayOutputStream();

            while ((read = bis.read(buf)) != -1) {
                if(progressListener != null && progressListener.isCanceled()) {
                    error = new PreconditionError("user canceled");
                    return null;
                }

                totalRead += read;
                progress[0] = totalRead;

                if (progressListener != null) {
                    publishProgress(progress);
                }

                bos.write(buf, 0, read);
            }

            Gson gson = AylaNetworks.sharedInstance().getGson();
            String payload = bos.toString();
            AylaLog.i(LOG_TAG, "message payload:" + payload);
            Type type = new TypeToken<AylaDatapoint.Wrapper>(){}.getType();
            AylaDatapoint.Wrapper wrapper = gson.fromJson(payload, type);
            String messageContent = String.valueOf(wrapper.datapoint.getValue());
            return messageContent;
        } catch (IOException e) {
            error = new NetworkError(e.getMessage(), e);
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e) {
                error = new NetworkError(e.getMessage(), e);
            }
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        super.onProgressUpdate(progress);
        if (progressListener != null) {
            progressListener.updateProgress(progress[0],progress[1]);
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (error == null && successListener != null) {
            successListener.onResponse(result);
        } else if (error != null) {
            errorListener.onErrorResponse(error);
        }
    }

}

