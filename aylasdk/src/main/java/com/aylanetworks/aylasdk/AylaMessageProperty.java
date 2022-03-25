package com.aylanetworks.aylasdk;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.change.PropertyChange;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.util.EmptyListener;
import com.aylanetworks.aylasdk.util.Preconditions;

import java.util.Locale;
import java.util.Map;

/**
 * Message property is of a property type that's especially for properties
 * with "message" base type. It can store either JSON string or normal text
 * content. While the size of a normal string property is limited to 1024
 * characters, the size of a message property can be as large as 512 KB.
 */
public class AylaMessageProperty extends AylaProperty<String > {

    private static final String LOG_TAG = "AylaMessageProperty";

    private String _messageDatapoiontId;
    private String _messageContent;

    /**
     * Predefined message value format: "/<property_name>/<datapoiont_id>"
     * example value: "/json_out/47ff9708-7d2e-11e9-6f0e-24d28b2c44c8"
     */
    private static final String MESSAGE_VALUE_TEMPLATE = "/%s/%s";

    /**
     * Returns the local message content which was updated from a successful call to
     * {@link #fetchMessageContent(Response.Listener, ErrorListener)}, or returns null
     * if no such message content was ever fetched.
     *
     * @see #fetchMessageContent(Response.Listener, ErrorListener) on how to fetch
     * online message content.
     */
    @Nullable
    public String getMessageContent() {
        return _messageContent;
    }

    /**
     * Returns the message datapoint Id.
     */
    @Nullable
    protected String getMessageDatapoiontId() {
        return (_messageDatapoiontId != null) ? _messageDatapoiontId
                : parseDatapointIdFromValue(getValue());
    }

    /**
     * Set the datapoint id of this message property.
     */
    protected void setMessageDatapoiontId(String id) {
        _messageDatapoiontId = id;
    }

    /**
     * Fetches the message content from the cloud through the stored datapoint ID.
     * <p>
     * Note that the message value return from {@link #getValue()} differs from the real
     * message content that it only holds an reference to the real message content in the
     * cloud. Internally, two steps are needed to fetch the real message content.
     * </p>
     * <ol>
     *     <li>Get the datapoiont ID</li>
     *     <li>Fetch message content with the datapoiont ID</li>
     * </ol>
     *
     * @param successListener Listener to receive the message content on success.
     * @param errorListener   Listener to receive error information should one occurred.
     * @return The AylaAPIRequest object queued to send for this request, can be used to
     * cancel the request if it has not been sent yet.
     */
    public AylaAPIRequest fetchMessageContent(@NonNull Response.Listener<String> successListener,
                                              @NonNull ErrorListener errorListener) {
        String propertyName = getName();
        String datapointId = getMessageDatapoiontId();

        try {
            Preconditions.checkNotNull(datapointId, "datapoint id is null");
            Preconditions.checkNotNull(propertyName, "property name is null");
        } catch (NullPointerException e) {
            errorListener.onErrorResponse(new PreconditionError(e.getMessage()));
        }

        return fetchDatapointWithID(datapointId, new Response.Listener<AylaDatapoint<String>>() {
            @Override
            public void onResponse(AylaDatapoint<String> response) {
                String responseValue = response.getValue();
                AylaLog.d(LOG_TAG, "got message content for " + getValue());
                _messageContent = responseValue;
                successListener.onResponse(_messageContent);
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(AylaError error) {
                AylaLog.e(LOG_TAG, "failed to get message content for " + getValue()
                        + ", cause " + error.getMessage());
            }
        });
    }

    /**
     * Parses the value passed in and returns the message datapoint Id if found,
     * otherwise returns null.
     * @param value the value that may contains a valid message datapoint Id.
     */
    @Nullable
    protected String parseDatapointIdFromValue(@Nullable String value) {
        if (value == null) {
            return null;
        }

        // value format:  "/<property_name>/<datapoiont_id>"
        // example value: "/json_out/47ff9708-7d2e-11e9-6f0e-24d28b2c44c8"
        int pos = value.lastIndexOf("/");
        return (pos != -1) ? value.substring(pos + 1) : null;
    }

    @Override
    protected String createDatapointEndpoint() {
        return String.format(Locale.US, "apiv1/dsns/%s/properties/%s/message_datapoints.json",
                getOwner().getDsn(), getName());
    }

    @Override
    protected String createDatapointPayload(String value, Map<String, String> metadata) {
        //  Here is an example payload. for application/json mime type property,
        //  the value field in the payload should be an valid escaped JSON string.
        //  {
        //       "datapoint": {
        //           "value": "string, JSON or escaped bytes in the format \u0000"
        //           "metadata": {}
        //       }
        //  }
        return super.createDatapointPayload(value, metadata);
    }

    @Override
    public boolean isLanModeSupported() {
        return false;
    }

    @Override
    public PropertyChange updateFrom(AylaDatapoint<String> dp, AylaDevice.DataSource dataSource) {
        if (!TextUtils.equals(dp.getId(), getMessageDatapoiontId())) {
            setMessageDatapoiontId(dp.getId());
            if (dataSource == AylaDevice.DataSource.DSS) {
                // The value in a mDSS datapoint is the message content itself, so we
                // don't need to fetch the message content again, but need to cache it
                // and replace it with the formatted value.
                _messageContent = dp.value;
                dp.value = String.format(Locale.US, MESSAGE_VALUE_TEMPLATE, getName(), dp.getId());
            } else if (AylaNetworks.sharedInstance().getSystemSettings().autoFetchMessageContent) {
                EmptyListener emptyListener = new EmptyListener();
                fetchMessageContent(emptyListener, emptyListener);
            }
        }

        return super.updateFrom(dp, dataSource);
    }

    @Override
    public PropertyChange updateFrom(AylaProperty otherProperty, AylaDevice.DataSource dataSource) {
        updateValueIfNeeded(String.valueOf(otherProperty.getValue()));
        return super.updateFrom(otherProperty, dataSource);
    }

    @Override
    public PropertyChange updateFrom(String value, Map<String, String> metadata,
                                     AylaDevice.DataSource updateSource) {
        updateValueIfNeeded(value);
        return super.updateFrom(value, metadata, updateSource);
    }

    private void updateValueIfNeeded(String newValue) {
        String curDatapointId = parseDatapointIdFromValue(getValue());
        String newDatapointId = parseDatapointIdFromValue(newValue);
        if (!TextUtils.equals(curDatapointId, newDatapointId)) {
            setMessageDatapoiontId(newDatapointId);
            if (AylaNetworks.sharedInstance().getSystemSettings().autoFetchMessageContent) {
                EmptyListener emptyListener = new EmptyListener();
                fetchMessageContent(emptyListener, emptyListener);
            }
        }
    }
}
