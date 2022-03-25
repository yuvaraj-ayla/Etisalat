package com.aylanetworks.aylasdk;

import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;
import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.error.ServerError;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

/**
 * Android_AylaSDK
 * <p>
 * Copyright 2016 Ayla Networks, all rights reserved
 */

@RunWith(AndroidJUnit4.class)
public class SharesTest {
    private AylaTestAccountConfig _owner;
    private AylaTestAccountConfig _recipient;
    private AylaTestConfig _testConfig;
    AylaSystemSettings _testSystemSettings;

    private AylaAuthorization _receiverAylaAuthorization;
    private AylaSessionManager _receiverSessionManager;
    private AylaAuthorization _ownerAylaAuthorization;
    private AylaSessionManager _ownerSessionManager;
    private String _ownedShareId;
    private String _receivedShareId;
    AylaShare _ownerShare;
    AylaShare _receiveShare;
    AylaShare  _recipientdeviceShareObject;
    AylaShare _ownerdeviceShareObject;
    Date _startDate;
    Date _endDate;



    //Setup might fail due to SVC-2243. If grants did not get deleted in previous test, share will
    // not be created.
    @Before
    public void setUp() throws Exception {
        //sign in to 2 user accountts and set up owned and received shares for the 2 users
        _testConfig = new AylaTestConfig();
        _testConfig.setApiTimeOut(10000);
        _testSystemSettings = TestConstants.US_DEVICE_DEV_SYSTEM_SETTINGS;
        _testConfig.setTestSystemSettings(_testSystemSettings);


        //Define _owner and _recipient accounts
        _owner = new AylaTestAccountConfig(TestConstants.TEST_USERNAME, TestConstants.TEST_PASSWORD, TestConstants.TEST_DEVICE_DSN, "session1");
        _recipient = new AylaTestAccountConfig(TestConstants.TEST_USERNAME2, TestConstants.TEST_PASSWORD2, TestConstants.TEST_DEVICE2_DSN, "session2");
        _receiverAylaAuthorization = _testConfig.signIn(_recipient,
                InstrumentationRegistry.getContext());

        assertNotNull("Failed to sign-in", _receiverAylaAuthorization);
        _receiverSessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_recipient.getTestSessionName());
        assertNotNull("Failed to get session manager", _receiverSessionManager);
        _testConfig.waitForDeviceManagerInitComplete(_recipient.getTestSessionName());

        //create a share for _recipient
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        _startDate = calendar.getTime();
        calendar.add(Calendar.MONTH, 1);
        _endDate = calendar.getTime();
        AylaDevice recipientDevice = _receiverSessionManager.getDeviceManager().
                deviceWithDSN(_recipient.getDeviceDSN());
        _recipientdeviceShareObject = recipientDevice.shareWithEmail(_owner.getUserEmail(),
                "write",
                null, df.format(_startDate), df.format(_endDate));
        assertNotNull(_recipientdeviceShareObject);
        RequestFuture<AylaShare> futureCreate = RequestFuture.newFuture();
        _receiverSessionManager.createShare(_recipientdeviceShareObject, null, futureCreate, futureCreate);
        _receiveShare = futureCreate.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        assertNotNull(_receiveShare);
        _receivedShareId = _receiveShare.getId();

        _ownerAylaAuthorization = _testConfig.signIn(_owner, InstrumentationRegistry.getContext());
        assertNotNull("Failed to sign-in", _ownerAylaAuthorization);
        _ownerSessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_owner.getTestSessionName());
        assertNotNull("Failed to get session manager", _ownerSessionManager);
        _testConfig.waitForDeviceManagerInitComplete(_owner.getTestSessionName());

        //Create a share for _owner

        // set end share dateTime "2024-05-30 12:00:00"
        df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        RequestFuture<AylaShare> futureOwnerCreate = RequestFuture.newFuture();
        AylaDevice ownerDevice = _ownerSessionManager.getDeviceManager().
                deviceWithDSN(_owner.getDeviceDSN());
        _ownerdeviceShareObject = ownerDevice.shareWithEmail(_recipient.getUserEmail(), "write",
                null, df.format(_startDate), df.format(_endDate));
        _ownerSessionManager.createShare(_ownerdeviceShareObject, null,
                futureOwnerCreate, futureOwnerCreate);
        _ownerShare = futureOwnerCreate.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        assertNotNull(_ownerShare);
        _ownedShareId = _ownerShare.getId();
        Log.d("TEST", "_owner shared id "+_ownedShareId);

        //Now we have a _owner share and receive share for both test accounts.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    //fetch all owned shares
    @Test
    public void testFetchOwnedShares(){
        AylaShare shares[] = null;
        RequestFuture<AylaShare[]> futureFetchAll = RequestFuture.newFuture();
        _ownerSessionManager.fetchOwnedShares(futureFetchAll, futureFetchAll);
        try {
            shares = futureFetchAll.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            fail("Error in testFetchOwnedShares " + e);
        }
        assertNotNull("Shares fetch success ", shares);
        assertEquals("Fetched all shares ", 1, shares.length);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //fetch all received shares
    @Test
    public void testFetchReceivedShares(){
        AylaShare shares[] = null;
        RequestFuture<AylaShare[]> futureFetchAll = RequestFuture.newFuture();
        _ownerSessionManager.fetchReceivedShares(futureFetchAll, futureFetchAll);
        try {
            shares = futureFetchAll.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Error in testFetchReceivedShares " + e);
        }
        assertNotNull("Shares fetch success ", shares);
        assertEquals("Fetched all shares ", 1, shares.length);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //fetch owned share with id
    @Test
    public void testFetchOwnedShareWithId(){
        AylaShare share = null;
        RequestFuture<AylaShare> futureFetchAll = RequestFuture.newFuture();
        _ownerSessionManager.fetchShare(_ownedShareId, futureFetchAll, futureFetchAll);
        try {
            share = futureFetchAll.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Error in testFetchOwnedShareWithId " + e);
        }
        assertNotNull("Owned shares fetch success ", share);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //fetch received share with id
    @Test
    public void testFetchReceivedShareWithId(){
        AylaShare share = null;
        RequestFuture<AylaShare> futureFetchAll = RequestFuture.newFuture();
        _ownerSessionManager.fetchShare(_receivedShareId, futureFetchAll, futureFetchAll);
        try {
            share = futureFetchAll.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Error in testFetchReceivedShareWithId " + e);
        }
        assertNotNull("Received shares fetch success ", share);
        assertEquals("Fetched share is same as expected ", _receivedShareId, share.getId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //fetch share fail with incorrect shareId
    @Test
    public void testFetchShareWithWrongId(){
        AylaShare share = null;
        RequestFuture<AylaShare> futureFetchAll = RequestFuture.newFuture();
        _ownerSessionManager.fetchShare("test", futureFetchAll, futureFetchAll);
        try {
            share = futureFetchAll.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            fail("Error in testFetchShareWithWrongId " + e);
        } catch (ExecutionException e) {
            e.printStackTrace();
            try{
                ServerError error = (ServerError)e.getCause();
                assertEquals("Success. Server returned 404 for invalid sahre id ", 404,
                        error.getServerResponseCode());
            } catch (ClassCastException ex){
                fail("Share fetch with incorrect id returned unexpected error code " + e);
            }
        }
        assertNull(share);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //test update shares. Might fail due to SVC-2321
    @Test
    public void testUpdateShare(){
        AylaShare share = null;
        RequestFuture<AylaShare> futureUpdate = RequestFuture.newFuture();
        _ownerShare.setOperation("write");
        _ownerSessionManager.updateShare(_ownerShare, null, futureUpdate,
                futureUpdate);
        try {
            share = futureUpdate.get(_testConfig.getApiTimeOut(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            fail("Error in testUpdateShare " + e);
        } catch (ExecutionException e) {
            fail("Error in testUpdateShare " + e);
            try{
                ServerError error = (ServerError)e.getCause();
                assertEquals("Success. Server returned 404 for invalid key ", 404,
                        error.getServerResponseCode());
            } catch (ClassCastException ex){
                fail("Datum fetch with bad key returned unexpected error code " + e);
            }
        }
        assertNotNull(share);
        _ownedShareId = share.getId();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //createShares. This test case fails due to bug SVC-2269
   /* public void testCreateShares(){

        //delete the _owner share first
        if(_ownedShareId != null){
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
            _ownerSessionManager.deleteShare(_ownedShareId, futureDelete, futureDelete);
            try {
                futureDelete.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        AylaShare[] createdShares;
        createdShares = new AylaShare[1];
        RequestFuture<AylaShare[]> futureCreate = RequestFuture.newFuture();
        Date startDate = new GregorianCalendar(2016, Calendar.APRIL, 30).getTime();
        Date endDate = new GregorianCalendar(2016, Calendar.MAY, 30).getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        AylaShare testShare = new AylaShare(_recipient.getUserEmail(), "write", _owner.getDeviceDSN(),
                "device", null, df.format(startDate),
                df.format(endDate));
        AylaShare[] shares = new AylaShare[1];
        shares[0] = testShare;
        _ownerSessionManager.createShares(shares, null, futureCreate, futureCreate);
        try {
            createdShares = futureCreate.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail("Error in testCreateShares " + e);
        }
        assertNotNull(createdShares);
        assertEquals("Created share returned the correct number of shares ", shares.length,
                createdShares.length);
        _ownedShareId = createdShares[0].getId();

    }*/


    // delete in tearDown() may occasionally fail due to cloud bug SVC-2262 and SVC-2243
    @After
    public void tearDown() throws Exception {
        if(_ownedShareId != null){
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDeleteOwner =
                    RequestFuture.newFuture();
            _ownerSessionManager.deleteShare(_ownedShareId, futureDeleteOwner, futureDeleteOwner);
            try {
                futureDeleteOwner.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch(Exception e){
                fail("tear down failed for Owner. delete shares failed");
            }
            Thread.sleep(1000); //add a delay so grant is deleted in service

        }
        //get receiver's auth and delete the share.
        // Note: Open cloud bug here might cause received share delete to fail.

        _receiverAylaAuthorization = _testConfig.signIn(_recipient,
                InstrumentationRegistry.getContext());
        _receiverSessionManager = AylaNetworks.sharedInstance()
                .getSessionManager(_recipient.getTestSessionName());
        if(_receivedShareId != null){
            RequestFuture<AylaAPIRequest.EmptyResponse> futureDeleteRecipient = RequestFuture
                    .newFuture();
            _receiverSessionManager.deleteShare(_receivedShareId, futureDeleteRecipient,
                    futureDeleteRecipient);
            try{
                futureDeleteRecipient.get(TestConstants.API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch(Exception e){
                fail("tear down failed for _recipient. delete shares failed");
            }
            Thread.sleep(1000); //add a delay so grant is deleted in service
        }
    }

}
