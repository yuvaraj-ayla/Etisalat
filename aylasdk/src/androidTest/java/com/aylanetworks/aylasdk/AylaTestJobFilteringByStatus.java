package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;

import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.icc.AylaIccManager;
import com.aylanetworks.aylasdk.icc.userconsent.AylaUserConsentJob;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.logging.Filter;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class AylaTestJobFilteringByStatus {

    private static final String TAG = "AylaUserConsentJobTest";

//    private static final String TARGET_DEVICE_DSN  = "AC000W000340292";
//    private static final String TARGET_DEVICE_HOST_OTA_JOB_NAME  = "DeviceJobTest10";

    AylaIccManager _iccManager;
    AylaUserConsentJob.Device targetDevice = null;
    AylaUserConsentJob.DeviceJob targetDeviceJob = null;

    @Before
    public void setUp() {
        assertTrue(TestConstants.signIn(InstrumentationRegistry.getContext()));
        assertTrue(TestConstants.waitForDeviceManagerInitComplete());

        AylaSessionManager sessionManager = AylaNetworks.sharedInstance().getSessionManager(
                TestConstants.TEST_SESSION_NAME);
        assertNotNull(sessionManager);

        AylaDeviceManager deviceManager = sessionManager.getDeviceManager();
        assertNotNull(deviceManager);

        _iccManager = new AylaIccManager(sessionManager);
    }

    @Test
    public void testFetchUserOTAJobsByStatus() {
        RequestFuture<AylaUserConsentJob.Device[]> future = RequestFuture.newFuture();
        AylaUserConsentJob.FiltersBuilder filtersBuilder = new AylaUserConsentJob.FiltersBuilder();
        @AylaUserConsentJob.DeviceStatus.AllowedType String [] statuses = new String[5];
        statuses[0] = AylaUserConsentJob.DeviceStatus.PENDING;
        statuses[1] = AylaUserConsentJob.DeviceStatus.CONSENT;
        statuses[2] = AylaUserConsentJob.DeviceStatus.PROCESSING;
        statuses[3] = AylaUserConsentJob.DeviceStatus.WAITING;
        statuses[4] = AylaUserConsentJob.DeviceStatus.QUEUED;
        filtersBuilder.withStatus(statuses);
        filtersBuilder.withDSNs(new String[]{TestConstants.TEST_DEVICE_DSN});
        _iccManager.fetchDeviceJobsWithQueryParams(filtersBuilder,future, future);

        try {
            AylaUserConsentJob.Device[] devices = future.get();
            assertTrue("No user OTA jobs available, need to " +
                            "create and start at least one user OTA job first",
                    devices.length != 0);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }



}
