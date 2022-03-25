package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;


public class ScheduleMetadataTest {

    private AylaDevice _device;

    @Before
    public void setUp() throws Exception {
        TestConstants.signIn(InstrumentationRegistry.getContext());
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());
        AylaDeviceManager dm = AylaNetworks.sharedInstance()
                .getSessionManager(TestConstants.TEST_SESSION_NAME).getDeviceManager();
        _device = dm.deviceWithDSN(TestConstants.TEST_DEVICE_DSN);
        assertNotNull(_device);
    }

    @Test
    public void testWriteMetadata() {

        AylaSchedule[] aylaSchedules = null;
        RequestFuture<AylaSchedule[]> futureFetch = RequestFuture.newFuture();
        _device.fetchSchedules(futureFetch, futureFetch);
        int API_TIMEOUT_MS = 20000;
        try {
//            int API_TIMEOUT_MS = 20000;
            aylaSchedules = futureFetch.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Fetch " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Fetch " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Fetch " + e);
        }
//        assertNotNull(aylaSchedules);
        if (aylaSchedules.length > 0) {
            //first we check if the schedule supports metadata
            AylaSchedule aylaSchedule = null;
            RequestFuture<AylaSchedule> futureUpdate = RequestFuture.newFuture();
            HashMap<String, Object> scheduleMetadata = new HashMap<>();
            int one=1;
            float oneFloat=1.0f;
            String string = "StringObject";
            scheduleMetadata.put("Integer",one);
            scheduleMetadata.put("float", oneFloat);
            scheduleMetadata.put("String", string);
            scheduleMetadata.put("Boolean", true);
            aylaSchedules[0].setMetadata(scheduleMetadata);
            _device.updateSchedule(aylaSchedules[0], futureUpdate, futureUpdate);
            try {
                aylaSchedule = futureUpdate.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                fail("Error in Schedule Fetch " + e);
            } catch (ExecutionException e) {
                fail("Error in Schedule Fetch " + e);
            } catch (TimeoutException e) {
                fail("Error in Schedule Fetch " + e);
            }
            try {
                Object metadataIntObj = scheduleMetadata.get("Integer");
                boolean result = (Class.forName("java.lang.Integer").isInstance(metadataIntObj));
                assertEquals("Wrote integer, but read different type",true,result);
                Object metadataFloatObj = scheduleMetadata.get("float");
                result = (Class.forName("java.lang.Float").isInstance(metadataFloatObj));
                assertEquals("Wrote float, but read different type",true,result);
                Object metadataIntString = scheduleMetadata.get("String");
                result = (Class.forName("java.lang.String").isInstance(metadataIntString));
                assertEquals("Wrote String, but read different type",true,result);
            } catch (ClassNotFoundException e) {
                fail("Exception Ocurred " + e);
            }
            int oneFromMetadata = (Integer) scheduleMetadata.get("Integer");
            float floatMetadata = (float) scheduleMetadata.get("float");
            String metadataString = (String) scheduleMetadata.get("String");
            boolean metadataObject = (boolean) scheduleMetadata.get("Boolean");
            assertEquals("Wrote boolean, but read different type :",true,metadataObject);
            assertEquals("Metadata has a wrong int value after read",one,oneFromMetadata);
            assertEquals("Metadata has a wrong float value after read",oneFloat,floatMetadata);
            assertEquals("Metadata has a wrong String value after read",string,metadataString);

        }

    }


    @Test
    public void testScheduleUpdateSRSSSuccess() {
        AylaSchedule[] aylaSchedules = null;
        RequestFuture<AylaSchedule[]> futureFetch = RequestFuture.newFuture();
        _device.fetchSchedules(futureFetch, futureFetch);
        int API_TIMEOUT_MS = 20000;
        try {
//            int API_TIMEOUT_MS = 20000;
            aylaSchedules = futureFetch.get(API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            fail("Error in Schedule Fetch " + e);
        } catch (ExecutionException e) {
            fail("Error in Schedule Fetch " + e);
        } catch (TimeoutException e) {
            fail("Error in Schedule Fetch " + e);
        }
//        assertNotNull(aylaSchedules);
        if (aylaSchedules.length > 0) {
            //first we check if the schedule supports metadata
            if (aylaSchedules[0].getMetadata() == null || !aylaSchedules[0].getMetadata().containsKey("sunrise_sunset")) {
                fail("Schedule doesn't support sunrise sunset feature");
            } else if ( (boolean)aylaSchedules[0].getMetadata().get("sunrise_sunset") )  {
                AylaSchedule aylaSchedule = null;
                RequestFuture<AylaSchedule> futureUpdate = RequestFuture.newFuture();
                aylaSchedules[0].setStartTimeEachDay("00:35:00 before sunrise");
                _device.updateSchedule(aylaSchedules[0], futureUpdate, futureUpdate);
                try{
                    aylaSchedule = futureUpdate.get(API_TIMEOUT_MS,TimeUnit.MILLISECONDS);
                }catch (InterruptedException e) {
                    fail("Error in Schedule Fetch " + e);
                } catch (ExecutionException e) {
                    fail("Error in Schedule Fetch " + e);
                } catch (TimeoutException e) {
                    fail("Error in Schedule Fetch " + e);
                }
                assertEquals("00:35:00 before sunrise",aylaSchedule.getStartTimeEachDay());
            } else {
                fail("Schedule doesn't support sunrise sunset feature");
            }
        }
    }

}
