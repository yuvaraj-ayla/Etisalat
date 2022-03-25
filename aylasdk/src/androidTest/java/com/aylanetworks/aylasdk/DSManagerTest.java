package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

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
public class DSManagerTest {

    private AylaTestAccountConfig _user;
    AylaSystemSettings _testSystemSettings;
    private AylaSessionManager _sessionManager;
    AylaDevice _device;
    AylaDSManager _dsManager;

    @Before
    public void setUp() throws Exception {
        AylaTestConfig _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(10000);
        _testSystemSettings = new AylaSystemSettings(TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS);
        assertTrue(_testSystemSettings.allowDSS);
        _testConfig.setTestSystemSettings(_testSystemSettings);

        _user = new AylaTestAccountConfig(TestConstants.TEST_USERNAME, TestConstants
                .TEST_PASSWORD, TestConstants.TEST_DEVICE_DSN, TestConstants.TEST_SESSION_NAME);
        AylaAuthorization _aylaAylaAuthorization = _testConfig.signIn(_user,
                InstrumentationRegistry.getContext());
        assertNotNull("Failed to sign-in", _aylaAylaAuthorization);
        _sessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_user.getTestSessionName());
        assertNotNull("Failed to get session manager", _sessionManager);
        _testConfig.waitForDeviceManagerInitComplete(_user.getTestSessionName());

        // We need to stop LAN mode for these tests
        _device = _sessionManager.getDeviceManager().deviceWithDSN(TestConstants
                .TEST_DEVICE_DSN);
        assertNotNull(_device);
        _device.setLanModePermitted(false);
        _dsManager = new AylaDSManager(_sessionManager);
        _dsManager.onResume();
        Thread.sleep(10000);

    }

    /**
     * Test init manager will set the right manager state.
     */
    @Test
    public void testInitManager(){
        assertEquals("state after initialize is correct", _dsManager.getState(),
                AylaDSManager.DSManagerState.Connected);

    }

    /**
     * Test onResume
     * If aylaSubscription is null, createSubscription() has to be called.
     * Then, connectToSocket() has to be called.
     * State changed to - Connected
     *
     */
    @Test
    public void testResumeComplete(){
        AylaTestConfig.InitDSSWaiter dssWaiter = new AylaTestConfig.InitDSSWaiter();
        _dsManager.addListener(dssWaiter);
        try {
            Field field = AylaDSManager.class.getDeclaredField("_aylaSubscription");
            field.setAccessible(true);
            field.set(_dsManager, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        _dsManager.onResume();
        boolean dssConnectionComplete = dssWaiter.waitForConnectionChange();
        assertTrue(dssConnectionComplete);
        assertEquals("DSManager state after resume ", AylaDSManager.DSManagerState.Connected,
                _dsManager.getState());

    }

    /**
     * Test dsManager state after onPause()
     * In onPause(), disconnect() is to be called.
     * State changed to - Disconnected
     *
     */
    @Test
    public void testPauseComplete(){

        AylaTestConfig.InitDSSWaiter dssWaiter = new AylaTestConfig.InitDSSWaiter();
        _dsManager.addListener(dssWaiter);
        try {
            Field field = AylaDSManager.class.getDeclaredField("_aylaSubscription");
            field.setAccessible(true);
            field.set(_dsManager, null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        _dsManager.onResume();
        boolean dssConnectionComplete = dssWaiter.waitForConnectionChange();
        assertTrue(dssConnectionComplete);
        assertEquals("DSManager state after resume ", AylaDSManager.DSManagerState.Connected,
                _dsManager.getState());


        _dsManager.onPause();
        boolean dssCloseComplete = dssWaiter.waitForConnectionChange();
        assertTrue(dssCloseComplete);
        assertEquals("DSManager state after resume ", AylaDSManager.DSManagerState.Disconnected,
                _dsManager.getState());

    }

    //Test if device is updated when datastream message is received by DSManager.
    // Verify that keep Alive messages do not result in updateFrom() call
    @Test
    public void testDeviceUpdateKeepAlive(){
        AylaProperty<Integer> propertyBlueBtn = _device.getProperty("Blue_button");
        propertyBlueBtn.updateFrom(0, null, AylaDevice.DataSource.DSS);
        int initialValue = propertyBlueBtn.getValue();
        assertEquals("Initiali value set ", 0, initialValue);
        String testKeepAlivePayload = "1|X";
        _dsManager.onMessage(testKeepAlivePayload);
        assertEquals("Property value remains same ", initialValue,
                _device.getProperty("Blue_button").getValue());


    }

    //Test if device not in LAN mode is updated when datastream message is received by DSManager.
    @Test
    public void testDeviceUpdateValidPayload(){
        AylaProperty<Integer> propertyBlueBtn  = _device.getProperty("Blue_button");
        propertyBlueBtn.updateFrom(0, null, AylaDevice.DataSource.DSS);
        int initialValue =propertyBlueBtn.getValue();
        assertEquals("Initiali value set ", 0, initialValue);
        int finalValue = 1;
        String testValidPayload = "426|{\"seq\":\"69\",\"metadata\":{\"oem_id\":\"0dfc7900\"," +
                "\"oem_model\":\"ledevb\",\"dsn\":\""+ TestConstants.TEST_DEVICE_DSN+"\"," +
                "\"property_name\":\"Blue_button\",\"display_name\":\"Blue_button\"," +
                "\"base_type\":\"boolean\",\"event_type\":\"datapoint\"}," +
                "\"datapoint\":{\"id\":\"2c7621d0-f836-11e5-b628-7170c488c054\"," +
                "\"created_at_from_device\":null,\"updated_at\":\"2016-04-01T18:18:46Z\"," +
                "\"created_at\":\"2016-04-01T18:18:46Z\",\"echo\":false,\"fetched_at\":null," +
                "\"closed\":false,\"value\":1,\"metadata\":{}}}";

        _dsManager.onMessage(testValidPayload);

        assertEquals("Property value changed ", finalValue, _device.getProperty("Blue_button")
                .getValue());


    }

    //Test if device in LAN mode is not updated when datastream message is received by DSManager.
    @Test
    public void testDeviceUpdateLanMode(){
        //All other tests delete LAN mode. So startLAN mode here
        _device.setLanModePermitted(true);
        TestConstants.waitForTestDeviceLANMode(10000);
        assertTrue(_device.isLanModeActive());

        AylaProperty<Integer> propertyBlueBtn  = _device.getProperty("Blue_button");
        propertyBlueBtn.updateFrom(0, null, AylaDevice.DataSource.DSS);
        int initialValue = propertyBlueBtn.getValue();
        assertEquals("Initiali value set ", 0, initialValue);
        String testValidPayload = "426|{\"seq\":\"69\",\"metadata\":{\"oem_id\":\"0dfc7900\"," +
                "\"oem_model\":\"ledevb\",\"dsn\":\""+TestConstants.TEST_DEVICE_DSN+"\"," +
                "\"property_name\":\"Blue_button\",\"display_name\":\"Blue_button\"," +
                "\"base_type\":\"boolean\",\"event_type\":\"datapoint\"}," +
                "\"datapoint\":{\"id\":\"2c7621d0-f836-11e5-b628-7170c488c054\"," +
                "\"created_at_from_device\":null,\"updated_at\":\"2016-04-01T18:18:46Z\"," +
                "\"created_at\":\"2016-04-01T18:18:46Z\",\"echo\":false,\"fetched_at\":null," +
                "\"closed\":false,\"value\":1,\"metadata\":{}}}";


        _dsManager.onMessage(testValidPayload);

        //Device shouldn't update in LAN mode
        assertEquals("Property value changed ", initialValue, _device.getProperty("Blue_button")
                .getValue());
    }

    @Test
    public void testDeviceUpdateStringCloudMode(){
        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        _device.fetchPropertiesCloud(null, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get();
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        }

        AylaProperty<String> propertyCmd  = _device.getProperty("cmd");
        propertyCmd.updateFrom("oldCmd", null, AylaDevice.DataSource.DSS);
        String initialValue = propertyCmd.getValue();
        assertEquals("Initial value set ","oldCmd", initialValue);
        String testValidPayload = "426|{\"seq\":\"69\",\"metadata\":{\"oem_id\":\"0dfc7900\"," +
                "\"oem_model\":\"ledevb\",\"dsn\":\""+TestConstants.TEST_DEVICE_DSN+"\"," +
                "\"property_name\":\"cmd\",\"display_name\":\"cmd\"," +
                "\"base_type\":\"string\",\"event_type\":\"datapoint\"}," +
                "\"datapoint\":{\"id\":\"2c7621d0-f836-11e5-b628-7170c488c054\"," +
                "\"created_at_from_device\":null,\"updated_at\":\"2016-04-01T18:18:46Z\"," +
                "\"created_at\":\"2016-04-01T18:18:46Z\",\"echo\":false,\"fetched_at\":null," +
                "\"closed\":false,\"value\":\"newCmd\",\"metadata\":{}}}";


        _dsManager.onMessage(testValidPayload);

        //Device shouldn't update in LAN mode
        assertEquals("Property value changed ", "newCmd", _device.getProperty(TestConstants.TEST_DEVICE_PROPERTY_STRING)
                .getValue());
    }

    @Test
    public void testDeviceUpdateDecimalCloudMode(){

        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        _device.fetchPropertiesCloud(null, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get();
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        }

        AylaProperty<Float> propertyCmd  = _device.getProperty(TestConstants
                .TEST_DEVICE_PROPERTY_DECIMAL);
        Float oldVal = (float)123.45;
        Float newVal = (float)125.45;
        propertyCmd.updateFrom(oldVal, null, AylaDevice.DataSource.DSS);
        Float initialValue = propertyCmd.getValue();
        assertEquals("Initial value set ", oldVal, initialValue);
        String testValidPayload = "426|{\"seq\":\"69\",\"metadata\":{\"oem_id\":\"0dfc7900\"," +
                "\"oem_model\":\"ledevb\",\"dsn\":\""+TestConstants.TEST_DEVICE_DSN+"\"," +
                "\"property_name\":\"decimal_in\",\"display_name\":\"decimal_in\"," +
                "\"base_type\":\"decimal\",\"event_type\":\"datapoint\"}," +
                "\"datapoint\":{\"id\":\"2c7621d0-f836-11e5-b628-7170c488c054\"," +
                "\"created_at_from_device\":null,\"updated_at\":\"2016-04-01T18:18:46Z\"," +
                "\"created_at\":\"2016-04-01T18:18:46Z\",\"echo\":false,\"fetched_at\":null," +
                "\"closed\":false,\"value\":"+newVal+",\"metadata\":{}}}";

        _dsManager.onMessage(testValidPayload);

        //Device shouldn't update in LAN mode
        assertEquals("Property value changed ", newVal, _device.getProperty(TestConstants.TEST_DEVICE_PROPERTY_DECIMAL)
                .getValue());
    }

    @Test
    public void testDeviceUpdateIntCloudMode(){
        RequestFuture<AylaProperty[]> futureProperty = RequestFuture.newFuture();
        _device.fetchPropertiesCloud(null, futureProperty, futureProperty);
        try {
            AylaProperty[] properties = futureProperty.get();
            assertNotNull(properties);
        } catch (InterruptedException e) {
            fail("Failed to fetch all properties");
        } catch (ExecutionException e) {
            fail("Failed to fetch all properties");
        }

        AylaProperty<Integer> propertyInt  = _device.getProperty(TestConstants
                .TEST_DEVICE_PROPERTY_INT);
        int oldVal = propertyInt.getValue();
        int newVal = oldVal + 1;
        propertyInt.updateFrom(oldVal, null, AylaDevice.DataSource.DSS);
        int initialValue = propertyInt.getValue();
        assertEquals("Initial value set ", oldVal, initialValue);
        String testValidPayload = "426|{\"seq\":\"69\",\"metadata\":{\"oem_id\":\"0dfc7900\"," +
                "\"oem_model\":\"ledevb\",\"dsn\":\""+TestConstants.TEST_DEVICE_DSN+"\"," +
                "\"property_name\":\"input\",\"display_name\":\"input\"," +
                "\"base_type\":\"integer\",\"event_type\":\"datapoint\"}," +
                "\"datapoint\":{\"id\":\"2c7621d0-f836-11e5-b628-7170c488c054\"," +
                "\"created_at_from_device\":null,\"updated_at\":\"2016-04-01T18:18:46Z\"," +
                "\"created_at\":\"2016-04-01T18:18:46Z\",\"echo\":false,\"fetched_at\":null," +
                "\"closed\":false,\"value\":"+newVal+",\"metadata\":{}}}";



        _dsManager.onMessage(testValidPayload);

        //Device shouldn't update in LAN mode
        assertEquals("Property value changed ", newVal, _device.getProperty(TestConstants
                .TEST_DEVICE_PROPERTY_INT)
                .getValue());
    }
}
