package com.aylanetworks.aylasdk;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
        ApplicationTriggerTest.class, AylaDeviceNotificationAppTest.class,
        BatchDatapointTest.class, ContactTest.class, DatapointTest.class,
        DeviceDatumTest.class, DeviceManagerTest.class,
        DeviceNotificationTest.class, DevicePollingTest.class,
        LanModeTest.class, PropertyTest.class, PropertyTriggerTest.class,
        ScheduleActionTest.class, ScheduleTest.class,
        ServiceUrlsTest.class, SignInTest.class, TimeZoneTest.class,
        URLHelperTest.class, UserDatumTest.class, UserProfileTest.class, RulesTest.class,
        DatastreamThroughputTest.class, MessageDatapointTest.class, SetupTest.class
})

public class CoreTests {
}


