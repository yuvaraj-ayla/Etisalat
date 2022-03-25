package com.aylanetworks.aylasdk;/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.ota.AylaLanOTADevice;
import com.aylanetworks.aylasdk.ota.AylaOTAImageInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class LANOTATest {
    private final static String LOG_TAG = "LANOTATest";

    @Before
    public void setUp() {
        TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
    }

    @Test
    public void testLanMode() {
        AylaDeviceManager deviceManager = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        RequestFuture<AylaOTAImageInfo> future = RequestFuture.newFuture();
        AylaLanOTADevice lanOTADevice = new AylaLanOTADevice(deviceManager,
                TestConstants.OTA_DEVICE_DSN,
                TestConstants.OTA_DEVICE_SSID);
        lanOTADevice.fetchOTAImageInfo(future,future);
        AylaOTAImageInfo imageInfo=null;
        AylaLog.d(LOG_TAG, "Calling fetchOTAImageInfo");
        try {
            int API_TIMEOUT_MS = 20000;
            imageInfo=future.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }

        lanOTADevice.setLanModePermitted(true);
        TestConstants.waitForTestDeviceLANMode(TestConstants.API_TIMEOUT_MS);

        File path = new File(InstrumentationRegistry.getContext().getDataDir(),
                TestConstants.OTA_DEVICE_DSN + ".image");
        if (!path.exists()) {
            try {
                path.createNewFile();
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }

        AylaLog.d(LOG_TAG, "Download Image to path: " + path.getAbsolutePath());

        RequestFuture<AylaAPIRequest.EmptyResponse> futureFetchFile = RequestFuture.newFuture();
        lanOTADevice.fetchOTAImageFile(imageInfo, path.getAbsolutePath(),null,futureFetchFile,futureFetchFile);
        AylaLog.d(LOG_TAG, "Calling fetchOTAImageFile");
        try {
            int API_TIMEOUT_MS = 30000;
            futureFetchFile.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }

        AylaLog.d(LOG_TAG, "Calling pushOTAImageToDevice");
        RequestFuture<AylaAPIRequest.EmptyResponse> futurePushImage = RequestFuture.newFuture();
        lanOTADevice.pushOTAImageToDevice(futurePushImage,futurePushImage);
        try {
            int API_TIMEOUT_MS = 90000;
            futurePushImage.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail(e.getMessage());
        } catch (ExecutionException e) {
            fail(e.getMessage());
        } catch (TimeoutException e) {
            fail(e.getMessage());
        }
    }
}