package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaIDPAuthProvider;
import com.aylanetworks.aylasdk.auth.AylaPartnerAuthorization;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * AylaSDKTest
 *
 * Copyright 2017 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class AylaIDPTest {

    private static final String PARTNER_ID = "12dcef78";
    private static final String PARTNER_NAME ="TEST_PARTNER";
    private static final String PARTNER_APP_ID = "mobile-partner-app-id";
    private static final String PARTNER_APP_SECRET = "uUeLyGoHWqwOiF3WDuMHQM-3MnA";
    private static final String PARTNER_AUTH_URL = "https://partner-cloud-mob-prod.ayladev.com/api/v1/token_sign_in";
    private static final String PARTNER_REFRESH_URL = "";
    private static final String PARTNER_TOKEN_KEY_NAME = "token";

    private List<AylaIDPAuthProvider.IDPPartnerToken> partnerTokens;

    @Before
    public void setUp() throws Exception {
        TestConstants.signIn(InstrumentationRegistry.getContext());
    }

    @Test
    public void testGetPartnerTokensFromAylaIDPService() {
        if (true) {
            // FIXME: skip test due to unreachable server address
            // https://partner-cloud-mob-prod.ayladev.com/api/v1/token_sign_in
            return;
        }

        AylaSessionManager sessionManager = AylaNetworks.sharedInstance().getSessionManager(TestConstants.TEST_SESSION_NAME);
        AylaIDPAuthProvider authProvider = new AylaIDPAuthProvider(sessionManager);

        List<String> partnerIds = new ArrayList<>();
        partnerIds.add(PARTNER_ID);
        RequestFuture<List<AylaIDPAuthProvider.IDPPartnerToken>> future = RequestFuture.newFuture();
        authProvider.getPartnerTokensForPartnerIds(partnerIds, future, future);

        try {
            partnerTokens = future.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("GetPartnerTokensFromAylaIDPService interrupted: " + e);
        } catch (ExecutionException e) {
            fail("GetPartnerTokensFromAylaIDPService execution exception: " + e);
        } catch (TimeoutException e) {
            fail("GetPartnerTokensFromAylaIDPService timeout: " + e);
        }
        assertNotNull("No partner token was fetched", partnerTokens);
        assertEquals(partnerIds.size(), partnerTokens.size());

        RequestFuture<AylaPartnerAuthorization> authFuture = RequestFuture.newFuture();
        AylaPartnerAuthorization aylaPartnerAuthorization = new AylaPartnerAuthorization
                (PARTNER_ID,PARTNER_NAME,PARTNER_AUTH_URL,PARTNER_APP_ID,PARTNER_APP_SECRET, PARTNER_REFRESH_URL);
        aylaPartnerAuthorization.loginToPartnerCloud(partnerTokens.get(0).getPartnerToken(),
                PARTNER_TOKEN_KEY_NAME,true,PARTNER_AUTH_URL,authFuture, authFuture);
        AylaPartnerAuthorization authorization = null;
        try {
            authorization = authFuture.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("loginToPartnerCloud interrupted: " + e);
        } catch (ExecutionException e) {
            fail("loginToPartnerCloud execution exception: " + e);
        } catch (TimeoutException e) {
            fail("loginToPartnerCloud timeout: " + e);
        }
        assertNotNull("No partner authorization returned", authorization);
        assertNotNull("No partner authorization access token returned", authorization.getAuthToken());
        assertNotNull("No partner authorization refresh token returned", authorization.getRefreshToken());
    }
}
