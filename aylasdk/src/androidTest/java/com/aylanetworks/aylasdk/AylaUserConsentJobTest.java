package com.aylanetworks.aylasdk;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.icc.AylaIccManager;
import com.aylanetworks.aylasdk.icc.userconsent.AylaUserConsentJob;
import com.aylanetworks.aylasdk.icc.userconsent.AylaUserConsentJob.DeviceJob;
import com.aylanetworks.aylasdk.icc.userconsent.AylaUserConsentJob.DeviceStatus;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AylaUserConsentJobTest {

    private static final String TAG = "AylaUserConsentJobTest";

    private static final String TARGET_DEVICE_DSN  = "AC000W000340292";
    private static final String TARGET_DEVICE_HOST_OTA_JOB_NAME  = "DeviceJobTest10";

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
    public void testFetchUserConsentOTAJobs() {
        RequestFuture<AylaUserConsentJob.Device[]> future = RequestFuture.newFuture();
        _iccManager.fetchUserConsentOTAJobs(future, future);
        try {
            AylaUserConsentJob.Device[] devices = future.get();
            assertTrue("No user consent OTA jobs available, need to " +
                    "create and start at least one user consent OTA job first",
                    devices.length != 0);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testCancelUserConsentOTAJob() {
        fetchTargetDeviceAndOTAJob();

        assertNotNull(String.format("Target device[%s] not available", TARGET_DEVICE_DSN), targetDevice);
        assertNotNull(String.format("Target device job[%s] not available, create or start the job first",
                TARGET_DEVICE_HOST_OTA_JOB_NAME), targetDeviceJob);

        try {
            RequestFuture<DeviceJob> future = RequestFuture.newFuture();
            _iccManager.cancelUserConsentJob(targetDeviceJob, targetDevice.dsn, future, future);
            DeviceJob resultJob = future.get();

            assertNotNull("No cancelled job returned", resultJob);
            assertEquals(String.format(Locale.US,"mismatched device job name"),
                    TARGET_DEVICE_HOST_OTA_JOB_NAME, resultJob.name);
            assertEquals(String.format(Locale.US,"unexpected job status"),
                    resultJob.device_status, DeviceStatus.CANCELLED);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testScheduleUserConsentOTAJob() {
        fetchTargetDeviceAndOTAJob();

        assertNotNull(String.format("Target device[%s] not available", TARGET_DEVICE_DSN), targetDevice);
        assertNotNull(String.format("Target device job[%s] not available, create or start the job first",
                TARGET_DEVICE_HOST_OTA_JOB_NAME), targetDeviceJob);

        try {
            // Schedule the target job for execution at a fake time, for example 3 days later
            RequestFuture<AylaUserConsentJob.DeviceJob> futureForJob = RequestFuture.newFuture();
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            cal.add(Calendar.DAY_OF_MONTH, 3);
            Date scheduledTimestamp = cal.getTime();

            _iccManager.scheduleUserConsentJob(targetDeviceJob, targetDevice.dsn, scheduledTimestamp,
                    futureForJob, futureForJob);
            AylaUserConsentJob.DeviceJob updatedJob = futureForJob.get();
            assertEquals(updatedJob.device_status, DeviceStatus.PENDING);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    // For user consent OTA tests, makes sure there should be at least
    // one user consent OTA job created and started for the target test device
    private void fetchTargetDeviceAndOTAJob() {
        RequestFuture<AylaUserConsentJob.Device[]> future = RequestFuture.newFuture();
        _iccManager.fetchUserConsentOTAJobs(new String[]{TARGET_DEVICE_DSN}, future, future);
        try {
            AylaUserConsentJob.Device[] devices = future.get();
            assertTrue("No user consent OTA jobs available",devices.length != 0);

            for (AylaUserConsentJob.Device device : devices) {
                if (TARGET_DEVICE_DSN.equals(device.dsn)) {
                    targetDevice = device;
                    for (AylaUserConsentJob.DeviceJob job : device.device_jobs) {
                        if (TARGET_DEVICE_HOST_OTA_JOB_NAME.equals(job.name)) {
                            targetDeviceJob = job;
                            break;
                        }
                    }
                }

            }

            assertTrue("Target user consent OTA job unavailable",targetDeviceJob != null);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

}
