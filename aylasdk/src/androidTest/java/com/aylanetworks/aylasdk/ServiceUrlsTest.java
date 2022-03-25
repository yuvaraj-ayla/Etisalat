package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.util.ServiceUrls;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class ServiceUrlsTest {
    private AylaSystemSettings _systemSettings;

    @Before
    public void setUp() throws Exception {

        // Create
        _systemSettings = new AylaSystemSettings(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);
        _systemSettings.context = InstrumentationRegistry.getContext();
        AylaNetworks.initialize(_systemSettings);
    }

    @Test
    public void testLogDevelopChinaServiceUrl() {
        String expected = "https://log-dev.ayla.com.cn/";
        String actual = ServiceUrls.getBaseServiceURL(ServiceUrls.CloudService.Log,
                AylaSystemSettings.ServiceType.Development,
                AylaSystemSettings.ServiceLocation.China);
        assertEquals("China Development Log service is not valid", expected, actual);
    }

    @Test
    public void testLogDevelopUsServiceUrl() {
        String expected = "https://log-dev.aylanetworks.com/";
        String actual = ServiceUrls.getBaseServiceURL(ServiceUrls.CloudService.Log,
                AylaSystemSettings.ServiceType.Development,
                AylaSystemSettings.ServiceLocation.USA);
        assertEquals("US Development Log service is not valid", expected, actual);
    }

    @Test
    public void testUserFieldUsServiceUrl() {
        String expected = "https://user-field.aylanetworks.com/";
        String actual = ServiceUrls.getBaseServiceURL(ServiceUrls.CloudService.User,
                AylaSystemSettings.ServiceType.Field,
                AylaSystemSettings.ServiceLocation.USA);
        assertEquals("User Field US service is not valid", expected, actual);
    }

    @Test
    public void testUserFieldChinaServiceUrl() {
        String expected = "https://user-field.ayla.com.cn/";
        String actual = ServiceUrls.getBaseServiceURL(ServiceUrls.CloudService.User,
                AylaSystemSettings.ServiceType.Field,
                AylaSystemSettings.ServiceLocation.China);
        assertEquals("User Field China service is not valid", expected, actual);
    }

    @Test
    public void testUserFieldEuropeServiceUrl() {
        String expected = "https://user-field-eu.aylanetworks.com/";
        String actual = ServiceUrls.getBaseServiceURL(ServiceUrls.CloudService.User,
                AylaSystemSettings.ServiceType.Field,
                AylaSystemSettings.ServiceLocation.Europe);
        assertEquals("User Field Europe service is not valid", expected, actual);
    }
}

