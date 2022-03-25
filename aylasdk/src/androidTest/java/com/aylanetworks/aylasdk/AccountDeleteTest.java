package com.aylanetworks.aylasdk;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Android_Aura
 * <p/>
 * Copyright 2016 Ayla Networks, all rights reserved
 */
@RunWith(AndroidJUnit4.class)
public class AccountDeleteTest {

    private AylaTestAccountConfig _user;
    private AylaTestConfig _testConfig;
    AylaSystemSettings _testSystemSettings;
    AylaAuthorization _aylaAuth;
    AylaSessionManager _sessionManager;

    @Before
    public void setUp() throws Exception {
        _user = new AylaTestAccountConfig(TestConstants.TEST_USERNAME2,
                TestConstants.TEST_PASSWORD2, null, "session1");
        _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(10000);
        _testSystemSettings = new AylaSystemSettings(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);
        _testConfig.setTestSystemSettings(_testSystemSettings);
        Context context = InstrumentationRegistry.getContext();

        _aylaAuth = _testConfig.signIn(_user, context);
        assertNotNull(_aylaAuth);
        _sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_user.getTestSessionName());
        assertNotNull(_sessionManager);
    }

    @Test
    public void testAccountDeleteSuccess(){
        // Deleting account is dangerous, which will delete the test account and all
        // registered test devices under the account, so just skip this test case now.
        assertTrue(true);

        /*
        RequestFuture<AylaAPIRequest.EmptyResponse> requestFuture = RequestFuture.newFuture();
        _sessionManager.deleteAccount(requestFuture, requestFuture);
        try {
            requestFuture.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in testAccountDeleteSuccess " + e);
        } catch (ExecutionException e) {
            fail("Error in testAccountDeleteSuccess " + e);
        } catch (TimeoutException e) {
            fail("Error in testAccountDeleteSuccess " + e);
        }
        assertTrue("Delete success",  requestFuture.isDone());
        */
    }

}
