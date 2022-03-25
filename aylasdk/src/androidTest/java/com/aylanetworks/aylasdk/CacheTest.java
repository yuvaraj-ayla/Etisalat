package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.AylaCache.CacheType;
import com.aylanetworks.aylasdk.auth.AylaAuthorization;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * Android_Aura
 * <p/>
 * Copyright 2016 Ayla Networks, all rights reserved
 */
@RunWith(AndroidJUnit4.class)
public class CacheTest {

    private AylaTestAccountConfig _user;
    private AylaSystemSettings _testSystemSettings;
    private AylaSessionManager _sessionManager;
    private String _dsnValue = TestConstants.TEST_DEVICE_DSN;
    private String _deviceJson = "[{\"device\":{\"dsn\":\"AC000W000021725\",\"mac\": null," +
            "\"model\":" +
            "\"model_demo2\"," + "\"oem_model\":\"\",\"product_name\":\"demo2\"," +
            "\"template_id\": null,\"connected_at\":\"2012-07-03T05:12:10Z\",\"key\":2," +
            "\"has_properties\":false,\"product_class\":\"\",\"connection_status\":\"Online\"," +
            "\"grant\":{\"user_id\":1,\"start_date_at\":\"2014-06-17T23:14:33Z\"," +
            "\"end_date_at\":null,\"operation\":\"write\"}}}]";
    private String _propertiesJson = "{\"property\": {\"base_type\": \"boolean\",\"value\": 0," +
            "\"data_updated_at\": \"2011-11-14T22:23:49Z\",\"device_key\": 11," +
            "\"name\": \"Blue_LED\",\"product_name\": \"proto 1\"}," +
            "\"property\":{\"base_type\": \"boolean\",\"value\":0," +
            "\"data_updated_at\": \"2011-11-14T22:23:49Z\"," +
            "\"device_key\": 11,\"name\":\"Green_LED\",\"key\":57,\"direction\":\"output\"," +
            "\"read_only\":true,\"product_name\":\"proto 1\"}}";


    @Before
    public void setUp() throws Exception {
        AylaTestConfig _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(10000);
        _testSystemSettings = new AylaSystemSettings(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);
        _testConfig.setTestSystemSettings(_testSystemSettings);
        _user = new AylaTestAccountConfig(TestConstants.TEST_USERNAME, TestConstants.TEST_PASSWORD,
                TestConstants.TEST_DEVICE_DSN, "session1");
        AylaAuthorization _aylaAylaAuthorization = _testConfig.signIn(_user,
                InstrumentationRegistry.getContext());
        assertNotNull("Failed to sign-in", _aylaAylaAuthorization);
        _sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_user.getTestSessionName());
        assertNotNull("Failed to get session manager", _sessionManager);
        AylaLog.initAylaLog( _sessionManager.getSessionName(), "test", AylaLog.LogLevel.Debug,
                AylaLog.LogLevel.Error);
    }

    @Test
    public void testCacheWrite(){
        AylaCache cache = _sessionManager.getCache();
        cache.save(CacheType.DEVICE, "", _dsnValue);
        String readValue = cache.getData(cache.getKey(CacheType.DEVICE, ""));

        assertNotNull(readValue);
        assertEquals(" Write dsnValue same as read dsnValue ", _dsnValue, readValue);
    }

    @Test
    public void testClearAll(){
        //first write dsnValue to test
        AylaCache cache = _sessionManager.getCache();
        cache.save(CacheType.DEVICE, "", _deviceJson);
        String readValue = cache.getData(cache.getKey(CacheType.DEVICE, ""));

        //verify that calue was written
        assertNotNull(readValue);
        assertEquals(" Write dsnValue same as read dsnValue ", _deviceJson, readValue);

        //clear cache
        cache.clearAll();

        //verify that cache was cleared
        readValue = cache.getData(_sessionManager.getCache().getKey(CacheType.DEVICE, ""));
        assertNull(readValue);

    }

    @Test
    public void testClearCacheType(){
        //first write dsnValue to test
        AylaCache cache = _sessionManager.getCache();
        cache.save(CacheType.DEVICE, "", _deviceJson);
        String readValue = cache.getData(cache.getKey(CacheType.DEVICE, ""));

        //verify that value was written
        assertNotNull(readValue);
        assertEquals(" Write dsnValue same as read dsnValue ", _deviceJson, readValue);

        //clear this cache type
        cache.save(CacheType.DEVICE, null, null);

        //verify cache was cleared.
        readValue = cache.getData(cache.getKey(CacheType.DEVICE, ""));
        assertNull(readValue);

    }

    @Test
    public void testCacheClearWithId(){
        //first write dsnValue to test
        AylaCache cache = _sessionManager.getCache();
        cache.save(CacheType.DEVICE, "", _deviceJson);

        //verify that calue was written
        String readValue = cache.getData(cache.getKey(CacheType.DEVICE, ""));
        assertNotNull(readValue);
        assertEquals(" Write devices same as read devices ", _deviceJson, readValue);

        readValue = null;
        //write properties json
        cache.save(CacheType.PROPERTY, _dsnValue, _propertiesJson);
        readValue = cache.getData(cache.getKey(CacheType.PROPERTY, _dsnValue));
        assertNotNull(readValue);
        assertEquals(" Write properties same as read properties ", _propertiesJson, readValue);

        cache.save(CacheType.PROPERTY, _dsnValue, _propertiesJson);

        //clear properties cache
        readValue = null;
        cache.save(CacheType.PROPERTY, _dsnValue, null);
        readValue = cache.getData(cache.getKey(CacheType.PROPERTY, _dsnValue));
        assertNull(readValue);

        //verify that only this cache type was cleared.
        readValue = cache.getData(cache.getKey(CacheType.DEVICE, ""));
        assertNotNull(readValue);
        assertEquals(" Write dsnValue same as read dsnValue ", _deviceJson, readValue);
    }

    //Verify that same value does not get written
    @Test
    public void testDisableCaching(){
        _sessionManager.getCache().disable();
        _sessionManager.getCache().save(CacheType.DEVICE, "", _deviceJson);

        //verify that calue was not written
        String readValue = _sessionManager.getCache().getData(_sessionManager.getCache().getKey(CacheType.DEVICE, ""));
        assertNull(readValue);

        _sessionManager.getCache().enable();
        //verify caching success
        _sessionManager.getCache().save(CacheType.DEVICE, "", _deviceJson);
        readValue = _sessionManager.getCache().getData(_sessionManager.getCache().getKey(CacheType.DEVICE, ""));
        assertNotNull(readValue);
        assertEquals(" Write devices same as read devices ", _deviceJson, readValue);
    }

    @After
    public void tearDown() throws Exception {
        _sessionManager.getCache().clearAll();
    }
}
