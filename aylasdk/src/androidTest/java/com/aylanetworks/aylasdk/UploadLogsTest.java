package com.aylanetworks.aylasdk;

import android.content.Context;
import android.os.Build;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.error.PreconditionError;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static androidx.test.InstrumentationRegistry.getInstrumentation;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Android_Aura
 * <p>
 * Copyright 2017 Ayla Networks, all rights reserved
 */
@RunWith(AndroidJUnit4.class)
public class UploadLogsTest {
    private AylaTestAccountConfig _user;
    private AylaTestConfig _testConfig;
    AylaSystemSettings _testSystemSettings;
    AylaAuthorization _aylaAuth;
    AylaSessionManager _sessionManager;
    private static final String LOG_TAG = "UPLOAD_LOGS_TEST";

    @Before
    public void setup(){

        // Required permissions for running the test.
        String packageName = InstrumentationRegistry.getTargetContext()
                .getPackageName();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + packageName
                            + " android.permission.WRITE_EXTERNAL_STORAGE");
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + packageName
                            + " android.permission.READ_EXTERNAL_STORAGE");
        }

        Context context = InstrumentationRegistry.getContext();
        _user = new AylaTestAccountConfig(TestConstants.TEST_USERNAME, TestConstants.TEST_PASSWORD, null,
                "session1");
        _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(10000);
        _testSystemSettings = new AylaSystemSettings(TestConstants
                .US_DEVICE_DEV_SYSTEM_SETTINGS);
        _testConfig.setTestSystemSettings(_testSystemSettings);
        _aylaAuth = _testConfig.signIn(_user, context);
        assertNotNull(_aylaAuth);
        _sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_user.getTestSessionName());
        assertNotNull(_sessionManager);

        try {
            AylaLog.initAylaLog(_sessionManager.getSessionName(), "ayla_logs_test",
                    AylaLog.LogLevel.Debug, AylaLog.LogLevel.Debug);
        } catch (PreconditionError preconditionError) {
            preconditionError.printStackTrace();
            fail("Precondition error in AylaLogs initialization "+preconditionError
                    .getLocalizedMessage());
        }

        //Upload sample logs
        AylaLog.d(LOG_TAG, "Test Log 1");
        AylaLog.d(LOG_TAG, "Test Log 2");
        AylaLog.d(LOG_TAG, "Test Log 3");
    }


    @Test
    public void testUploadLogs(){
        RequestFuture<AylaAPIRequest.EmptyResponse> requestFuture = RequestFuture.newFuture();
        AylaLog.uploadCrashLogsToLogService(requestFuture, requestFuture);

        try {
            requestFuture.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in testUploadLogs " + e.getCause());
        } catch (ExecutionException e) {
            fail("Error in testUploadLogs " + e.getCause());
        } catch (TimeoutException e) {
            fail("Error in testUploadLogs " + e.getCause());
        }
        assertTrue(requestFuture.isDone());
    }
}
