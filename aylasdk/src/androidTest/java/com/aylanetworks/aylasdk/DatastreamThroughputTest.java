package com.aylanetworks.aylasdk;

import android.text.TextUtils;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.volley.DefaultRetryPolicy;
import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.change.Change;
import com.aylanetworks.aylasdk.change.PropertyChange;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Android_AylaSDK
 * <p>
 * Copyright 2016 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class DatastreamThroughputTest {

    private static final String LOG_TAG = "DSSThroughputTest";

    private static final int DATA_POINT_SIZE = 100;
    private static final int TARGET_SUCCESS_RATE = 100;
    private static final int MAX_ALLOWED_MISMATCHED_SEQUENCE_COUNT = 0;

    private static final int DEFAULT_LOGIN_IN_TIMEOUT = 10000;
    private static final int DEFAULT_DATAPOINT_BATCH_CREATE_TIMEOUT = 60000;
    private static final int WAITING_TIME_FOR_DSS_DATAPOINTS_UPDATE = 5000;

    private static final int DATAPOINT_CREATED_SUCCESS = 201;

    private AylaTestConfig          _testConfig;
    private AylaTestAccountConfig   _testAccountConfig;
    private AylaSystemSettings      _testSystemSettings;
    private AylaAuthorization       _testAuthorization;
    private AylaSessionManager      _testSessionManager;

    private SendSequences _sendSequences;
    private RecvSequences _recvSequences;

    private Object _sync = new Object();

    @Before
    public void setUp() throws Exception {

        _sendSequences = new SendSequences(DATA_POINT_SIZE);
        _recvSequences = new RecvSequences(DATA_POINT_SIZE, MAX_ALLOWED_MISMATCHED_SEQUENCE_COUNT);

        _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(DEFAULT_LOGIN_IN_TIMEOUT);

        _testSystemSettings = new AylaSystemSettings(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);
        _testSystemSettings.allowDSS = true;
        _testConfig.setTestSystemSettings(_testSystemSettings);
        assertTrue(_testSystemSettings.allowDSS);

        _testAccountConfig = new AylaTestAccountConfig(TestConstants.TEST_USERNAME, TestConstants
                .TEST_PASSWORD, TestConstants.TEST_DEVICE_DSN, TestConstants.TEST_DSS_SESSION_NAME);
        _testAuthorization = _testConfig.signIn(_testAccountConfig, InstrumentationRegistry.getContext());

        assertNotNull("Failed to sign-in", _testAuthorization);
        _testSessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_testAccountConfig.getTestSessionName());
        assertNotNull("Failed to get session manager", _testSessionManager);

        _testConfig.waitForDeviceManagerInitComplete(_testAccountConfig.getTestSessionName());

        // Has to wait for websocket turning to be connected before issuing any
        // DSS requests, otherwise the first few DSS results from wss server may
        // get dropped because the socket is not ready to receive.
        _testConfig.waitForDSManagerInitComplete(_testAccountConfig.getTestSessionName());
        assertTrue(_testSessionManager.getDSManager().isConnected());

        AylaLog.initAylaLog(_testAccountConfig.getTestSessionName(), "ayla_logs_test",
                AylaLog.LogLevel.Verbose, AylaLog.LogLevel.Verbose);
    }

    @Test
    public void testDssDataPointsThroughput() {
        AylaDeviceManager deviceManager = _testSessionManager.getDeviceManager();
        assertNotNull(deviceManager);

        AylaDevice ledEVB = deviceManager.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(ledEVB);

        final RequestFuture<AylaProperty[]> propertiesFuture = RequestFuture.newFuture();
        String[] propertiesToFetch = new String[] {TestConstants.TEST_DEVICE_PROPERTY_STRING};
        ledEVB.fetchPropertiesCloud(propertiesToFetch, propertiesFuture, propertiesFuture);
        try {
            AylaProperty[] properties = propertiesFuture.get();
            assertNotNull(properties);
            assertEquals("mismatched properties length", properties.length, propertiesToFetch.length);
        } catch (InterruptedException e1) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e2) {
            fail("Failed to fetch all properties");
        }

        final AylaProperty<String> cmdProperty = ledEVB.getProperty(TestConstants.TEST_DEVICE_PROPERTY_STRING);
        assertNotNull(cmdProperty);

        ledEVB.addListener(new AylaDevice.DeviceChangeListener() {
            @Override
            public void deviceChanged(AylaDevice device, Change change) {
                if (change instanceof PropertyChange) {
                    PropertyChange propertyChange = (PropertyChange) change;
                    String propertyName = propertyChange.getPropertyName();
                    AylaDevice.DataSource source = cmdProperty.getLastUpdateSource();
                    String changedValue = String.valueOf(propertyChange.getValue());
                    AylaLog.d(LOG_TAG, "device property changed, name:" + propertyName
                                + ", source:" + (source != null ? source.name() : "")
                                + ", value:" + changedValue);

                    if (TestConstants.TEST_DEVICE_PROPERTY_STRING.equals(propertyName)
                            && source == AylaDevice.DataSource.DSS) {
                        String expectedRecvSeq = _recvSequences.nextSequence();
                        String actualRecvSeq = changedValue;
                        if (!TextUtils.equals(expectedRecvSeq, actualRecvSeq)) {
                            AylaLog.d(LOG_TAG, "Increase mismatched seq count. expected "
                                    + expectedRecvSeq + " but found " + actualRecvSeq);
                            _recvSequences.increaseMismatchedSeqCount();
                            int count = _recvSequences.getMismatchedSeqCount();
                            if (count >= _recvSequences.maxAllowedMismatchedSequnceCount()) {
                                ledEVB.removeListener(this);
                                Log.e(LOG_TAG, "Found " + count + " mismatched seq.");
                                synchronized (_sync) {
                                    _sync.notify();
                                }

                            }
                        }
                    }
                }
            }

            @Override
            public void deviceError(AylaDevice device, AylaError error) {
                AylaLog.e(LOG_TAG, "deviceError:" + error);
                synchronized (_sync) {
                    _sync.notify();
                }
            }

            @Override
            public void deviceLanStateChanged(AylaDevice device, boolean lanModeEnabled, AylaError error) {
                AylaLog.i(LOG_TAG, "deviceLanStateChanged, lanModeEnabled=" + lanModeEnabled);
            }
        });

        final int batchRequestSize = _sendSequences.maxSequenceSize();
        AylaDatapointBatchRequest<String> requests[] = new AylaDatapointBatchRequest[batchRequestSize];
        for (int i = 0; i < batchRequestSize; i++) {
            String seq = _sendSequences.nextSequence();
            requests[i] = new AylaDatapointBatchRequest<>(seq, cmdProperty);
        }
        RequestFuture<AylaDatapointBatchResponse[]> batchResponseFuture = RequestFuture.newFuture();
        AylaAPIRequest request = deviceManager.createDatapointBatch(requests, batchResponseFuture, batchResponseFuture);
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_DATAPOINT_BATCH_CREATE_TIMEOUT,0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        try {
            AylaDatapointBatchResponse[] responseArray = batchResponseFuture.get(DEFAULT_DATAPOINT_BATCH_CREATE_TIMEOUT, TimeUnit.MILLISECONDS);
            AylaLog.d(LOG_TAG, "got AylaDatapointBatchResponse, batchRequestSize=" + responseArray.length);

            // Wait a few more seconds, in case there are DSS messages that haven't been received yet.
            // In addition, to make sure mismatching sequences received in deviceChanged() won't
            // crash the whole test process, as the callback works on the main thread.
            synchronized (_sync) {
                _sync.wait(WAITING_TIME_FOR_DSS_DATAPOINTS_UPDATE);
            }

            int createdDatapointsFromRestfulAPI = 0;
            for (AylaDatapointBatchResponse response : responseArray) {
                if (response.getStatus() == DATAPOINT_CREATED_SUCCESS) {
                    createdDatapointsFromRestfulAPI++;
                }
            }

            int createdDatapointsFromDSS = batchRequestSize - _recvSequences.getMismatchedSeqCount();
            assertEquals(createdDatapointsFromRestfulAPI, createdDatapointsFromDSS);

        } catch (TimeoutException e1) {
            AylaLog.e(LOG_TAG, "Caught TimeoutException " + e1);
        } catch(ExecutionException e2) {
            AylaLog.e(LOG_TAG, "Caught ExecutionException " + e2);
        } catch (InterruptedException e3) {
            AylaLog.e(LOG_TAG, "Caught InterruptedException " + e3);
        } finally {
            int successRate = _recvSequences.successRate();
            if (successRate < TARGET_SUCCESS_RATE) {
                fail("Failed with success rate " + successRate);
            }
        }
    }

    @After
    public void tearDown() throws Exception {}

    static class Sequences {

        private static final int MAX_SEQUENCE_SIZE = 100;

        private static final String SEQUENCE_PREFIX = "seq";

        private int _max;
        private int _count;


        public Sequences(int max) {
            _count = 1;
            _max = max <= 0 ? MAX_SEQUENCE_SIZE : max;
        }

        public String nextSequence() {
            if (_count <= _max) {
                return SEQUENCE_PREFIX + _count++;
            }

            return "";
        }

        public void reset() {
            _count = 1;
        }

        public int maxSequenceSize() {
            return _max;
        }

    }

    final static class SendSequences extends Sequences {

        public SendSequences(int max) {
            super(max);
        }
    }

    final static class RecvSequences extends Sequences {

        private int _mismatchedSequenceCount;
        private int _maxAllowedMismatchedSequenceCount;

        public RecvSequences(int max, int maxAllowed) {
            super(max);
            _mismatchedSequenceCount = 0;
            _maxAllowedMismatchedSequenceCount = maxAllowed;
        }

        public int maxAllowedMismatchedSequnceCount() {
            return _maxAllowedMismatchedSequenceCount;
        }

        public void increaseMismatchedSeqCount() {
            _mismatchedSequenceCount++;
        }

        public int getMismatchedSeqCount() {
            return _mismatchedSequenceCount;
        }

        public int successRate() {
            return 100 * (maxSequenceSize() - getMismatchedSeqCount()) / maxSequenceSize();
        }
    }
}
