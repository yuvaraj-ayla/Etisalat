package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */

import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.error.ServerError;
import com.aylanetworks.aylasdk.gss.AylaGroupManager;
import com.aylanetworks.aylasdk.gss.model.AylaChildCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollection;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionDevice;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionProperty;
import com.aylanetworks.aylasdk.gss.model.AylaCollectionTriggerResponse;
import com.aylanetworks.aylasdk.util.EmptyListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static com.aylanetworks.aylasdk.TestConstants.TEST_EMAIL_ID;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class GroupsTest {
    private static final String TAG = "GroupsTest";

    private AylaCollection _testGroup;
    private AylaGroupManager _testGroupManager;
    private List<AylaCollection> _recyclableGroups;
    public static final String TEST_DEVICE_DSN = "AC000W000000001";
    public static final String TEST_DEVICE_DSN1 = "AC000W000000002";

    @Before
    public void setUp() {
        assertTrue(TestConstants.signIn(InstrumentationRegistry.getContext()));
        AylaSessionManager sm = AylaNetworks.sharedInstance().getSessionManager(
                TestConstants.TEST_SESSION_NAME);
        assertNotNull(sm);
        _testGroupManager = sm.getGroupManager();
        _recyclableGroups = new ArrayList<>();
    }

    @After
    public void tearDown() {
        if (_recyclableGroups != null && _recyclableGroups.size() > 0) {
            List<String> groupUUIDs = new ArrayList<>();
            for (AylaCollection group : _recyclableGroups) {
                groupUUIDs.add(group.collectionUuid);
            }

            EmptyListener emptyListener = new EmptyListener();
            _testGroupManager.deleteGroups(groupUUIDs, emptyListener, emptyListener);

            try {
                // there may still ongoing delete group calls
                // before the test process was killed, so we should
                // wait some to make sure all groups get deleted.
                Thread.sleep(_recyclableGroups.size() * 2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (_testGroup != null && _testGroup.collectionUuid != null) {
            _testGroupManager.deleteGroup(
                    _testGroup.collectionUuid,
                    new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                        @Override
                        public void onResponse(AylaAPIRequest.EmptyResponse response) {
                            Log.d(TAG, "deleted group: " + _testGroup.name
                                    + ", uuid:" + _testGroup.collectionUuid);
                        }
                    }, new ErrorListener() {
                        @Override
                        public void onErrorResponse(AylaError error) {
                            Log.d(TAG, "failed to delete collection: " + _testGroup.name);
                        }
                    });
        }
    }

    @Test
    public void testCreateEmptyGroup() {
        String name = "An empty group";
        AylaCollection createdCollection = createEmptyGroup(name);
        assertNotNull(createdCollection);
        assertNotNull(createdCollection.collectionUuid);
        assertEquals(createdCollection.name, name);
        _recyclableGroups.add(createdCollection);
    }

    @Test
    public void testGroupNameLength() {
        String name = "Group name greater than 32 characters";
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(name,
                null,
                null,
                null,
                future, future);
        try {
            AylaCollection createdGroup = future.get();
            _recyclableGroups.add(createdGroup);
        } catch (Exception e) {
            try{
                ServerError error = (ServerError) e.getCause();
                assertTrue(error.getServerResponseCode() == 422);
                assertTrue(error.getMessage().contains(
                        "Collection name should contain alphanumeric characters, whitespaces, " +
                                "underscore(_) and hyphen(-) and should be between " +
                                "1-32 characters"));
            } catch (ClassCastException ex){
                fail("testGroupNameLength failed with unexpected " +
                        "error code " + e);
            }
        }
    }

    @Test
    public void testCreateGroupWithResources() {
        AylaCollection group = new AylaCollection();
        group.name = "A group with ayla device";
        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(2, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device = new AylaCollectionDevice();
            device.dsn = TEST_DEVICE_DSN;
            devicesToCreate = new AylaCollectionDevice[]{device};
        }
        group.devices = devicesToCreate;
        AylaChildCollection[] aylaChildCollection = new AylaChildCollection[0];
        group.childCollections = aylaChildCollection;

        Map<String, String> custom_attributes= new HashMap<String, String>();
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testGroupManager.createGroup(
                group.name,
                group.devices,
                group.childCollections,
                custom_attributes,
                future, future);
        try {
            AylaCollection createdGroup = future.get();
            assertNotNull(createdGroup.collectionUuid);
            if (group.devices != null && group.devices.length>0)
                assertEquals(createdGroup.devices.length, group.devices.length);
            assertEquals(createdGroup.childCollections.length, group.childCollections.length);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getMessage() != null && !e.getMessage().contains("Child collection is not found")) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testCreateGroup() {
        AylaCollection group = new AylaCollection();
        group.name = "A group with ayla device";
        AylaCollectionDevice[] aylaCollectionDevice = new AylaCollectionDevice[1];
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            int size = deviceManager.getDevices().size();
            //aylaCollectionDevice = new AylaCollectionDevice[size];
            AylaCollectionDevice device = new AylaCollectionDevice();
            device.dsn = deviceManager.getDevices().get(0).dsn;
            aylaCollectionDevice[0] = device;
        } else {
            // Select two fake devices to update
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;

            aylaCollectionDevice = new AylaCollectionDevice[]{device1};
        }
        group.devices = aylaCollectionDevice;
        AylaChildCollection[] aylaChildCollection = new AylaChildCollection[0];
        group.childCollections = aylaChildCollection;

        Map<String, String> custom_attributes = new HashMap<>();;
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(
                group.name,
                group.devices,
                group.childCollections,
                custom_attributes,
                future, future);
        try {
            AylaCollection createdGroup = future.get();
            assertNotNull(createdGroup.collectionUuid);
            assertEquals(createdGroup.name, group.name);
            assertEquals(createdGroup.devices.length, group.devices.length);
            assertEquals(createdGroup.childCollections.length, group.childCollections.length);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getMessage() != null && !e.getMessage().contains("Child collection is not found")) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testTriggerGroup() {
        AylaCollection group = new AylaCollection();
        group.name = "A group with ayla device";

        AylaCollectionDevice[] aylaCollectionDevices;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(2, size / 2));
            aylaCollectionDevices = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i=0 ; i<num ; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                aylaCollectionDevices[i] = device;
            }
        } else {
            // Select two fake devices to update
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;

            aylaCollectionDevices = new AylaCollectionDevice[]{device1};
        }

        AylaCollectionProperty property1 = new AylaCollectionProperty();
        property1.propertyName = "Blue_LED";
        property1.propertyValue = "1";
        //TODO fails for boolean, decimal, integer - Server error: 400
        AylaCollectionProperty[] properties = new AylaCollectionProperty[]{property1};

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(
                "A group with ayla device",
                aylaCollectionDevices,
                null,
                null,
                future, future);

        AylaCollection createdGroup = null;
        try {
            createdGroup = future.get();
            assertNotNull(createdGroup.collectionUuid);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        RequestFuture<AylaCollectionTriggerResponse[]> future2 = RequestFuture.newFuture();
        _testGroupManager.triggerGroup(createdGroup.collectionUuid, properties, future2, future2);

        try {
            AylaCollectionTriggerResponse[] updatedGroup = future2.get();
            assertNotNull(updatedGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchAttributesForGroup() {
        Map<String, String> custom_attributes = new HashMap<>();;
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");
        Map<String, String> attributes = new HashMap<>();;
        attributes.put("tag", "houseHoldTag");
        attributes.put("pincode", "123456");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(
                "A sample group collection",
                null,
                null,
                custom_attributes,
                future, future);

        AylaCollection createdGroup = null;
        try {
            createdGroup = future.get();
            assertNotNull(createdGroup);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to create collection due to error:" + e);
        }

        RequestFuture<Map<String, String>> future2 = RequestFuture.newFuture();
        _testGroupManager.fetchAttributesForGroup(createdGroup, future2, future2);
        try {
            Map<String, String> fetchedAttr = future2.get();
            assertNotNull(fetchedAttr);
            assertEquals(fetchedAttr.toString(), ("{custom_attributes="+custom_attributes.toString()+"}"));
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch collection due to error:" + e);
        }
    }

    @Test
    public void testAddResourcesToGroup() {
        AylaCollection parentCollection = createEmptyGroup(
                "group collection");

        AylaCollectionDevice[] devicesToAdd = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to add
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(2, size / 2));  //  1 <= num <= 3
            devicesToAdd = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                devicesToAdd[i] = device;
            }
        } else {
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;
            AylaCollectionProperty property = new AylaCollectionProperty();
            property.propertyName = "Blue_LED";
            property.propertyValue = 1;
            device1.properties = new AylaCollectionProperty[]{property};

            devicesToAdd = new AylaCollectionDevice[]{device1};
        }

        AylaCollection updatedParentCollection = null;
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.addResourcesToGroup(
                parentCollection.collectionUuid,
                devicesToAdd,
                null,
                future, future);
        try {
            updatedParentCollection = future.get();
            assertNotNull(updatedParentCollection);
            assertEquals(updatedParentCollection.devices.length, devicesToAdd.length);
        } catch (InterruptedException | ExecutionException e) {
            Log.d(TAG, "failed to add collection devices due to error:" + e);
            fail(e.getMessage());
        }

        AylaCollection childCollection = createEmptyGroup(
                "child collection");
        AylaChildCollection aylaChildCollection = new AylaChildCollection();
        aylaChildCollection.collectionUuid = childCollection.collectionUuid;
        AylaChildCollection[] childCollectionsToAdd = new AylaChildCollection[]{aylaChildCollection};
        future = RequestFuture.newFuture();
        _testGroupManager.addResourcesToGroup(
                parentCollection.collectionUuid,
                parentCollection.devices,
                childCollectionsToAdd,
                future, future);
        try {
            updatedParentCollection = future.get();
            assertNotNull(updatedParentCollection.collectionUuid);
            assertEquals(updatedParentCollection.childCollections.length, childCollectionsToAdd.length);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to add child collection due to error:" + e);
        }
    }

    @Test
    public void testAddAttributesToGroup() {
        AylaCollection createdCollection = createEmptyGroup(
                "Empty Group Test");
        assertNotNull(createdCollection.collectionUuid);

        Map<String, String> custom_attributes = new HashMap<>();
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testGroupManager.addAttributesToGroup(createdCollection, custom_attributes, future, future);
        try {
            AylaCollection updatedCollection = future.get();
            assertNotNull(updatedCollection.collectionUuid);
            assertEquals(updatedCollection.custom_attributes.get("color"), custom_attributes.get("color"));
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to add custom attributes due to error:" + e);
        }
    }

    @Test
    public void testFetchGroupsHavingDSN() {
        AylaCollectionDevice device = new AylaCollectionDevice();
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            int size = deviceManager.getDevices().size();
            Random rnd = new Random(System.currentTimeMillis());
            device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
        } else {
            device.dsn = TEST_DEVICE_DSN;
        }
        AylaCollectionDevice[] devicesToCreate = new AylaCollectionDevice[]{device};

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(
                "group test fetch using dsn",
                devicesToCreate,
                null,
                null,
                future, future);
        try {
            AylaCollection createdCollection = future.get();
            assertNotNull(createdCollection);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to create collection due to error:" + e);
        }

        RequestFuture<AylaCollection[]> future2 = RequestFuture.newFuture();
        _testGroupManager.fetchGroupsHavingDSN(device.dsn, future2, future2);
        try {
            AylaCollection[] fetchedCollections = future2.get();
            assertNotNull(fetchedCollections);
            assertTrue(fetchedCollections.length >= 1);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch group due to error:" + e);
        }
    }

    @Test
    public void testFetchGroup() {
        AylaCollection collection = createEmptyGroup("fetch a group");
        assertNotNull(collection);
        assertNotNull(collection.collectionUuid);

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.fetchGroup(collection.collectionUuid, future, future);
        try {
            AylaCollection fetchedCollection = future.get();
            assertNotNull(fetchedCollection.collectionUuid);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch group due to error:" + e);
        }
    }

    @Test
    public void testFetchGroups() {
        createEmptyGroup("group1");
        createEmptyGroup("group2");

        RequestFuture<AylaCollection[]> future = RequestFuture.newFuture();
        _testGroupManager.fetchAllGroups(future, future);
        try {
            AylaCollection[] fetchedCollections = future.get();
            assertNotNull(fetchedCollections);
            assertTrue(fetchedCollections.length >= 2);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch group due to error:" + e);
        }
    }

    @Test
    public void testUpdateGroupAttributes() {
        AylaCollection group = new AylaCollection();
        group.name = "group update attribute state";

        Map <String, String> custom_attributes = new HashMap<>();
        custom_attributes.put("pincode", "600001");
        custom_attributes.put("tag", "houseHoldTag");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        AylaCollection createdGroup = null;

        _testGroupManager.createGroup(
                group.name,
                null,
                null,
                custom_attributes,
                future, future);

        try {
            createdGroup = future.get();
            assertNotNull(createdGroup.collectionUuid);
            assertEquals(createdGroup.name, group.name);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        custom_attributes.put("pincode", "600444");
        custom_attributes.put("tag", "officeTag");
        RequestFuture<AylaCollection> futureUpdate = RequestFuture.newFuture();
        _testGroupManager.updateAttributesForGroup(createdGroup,
                custom_attributes,
                futureUpdate, futureUpdate);

        try {
            AylaCollection updatedCollection = futureUpdate.get();
            assertNotNull(updatedCollection.collectionUuid);

            assertEquals(updatedCollection.custom_attributes.get("pincode"), custom_attributes.get("pincode"));
            assertEquals(updatedCollection.custom_attributes.get("tag"), custom_attributes.get("tag"));
            assertEquals(updatedCollection.custom_attributes.get("pincode"), "600444");
            assertEquals(updatedCollection.custom_attributes.get("tag"), "officeTag");
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to update custom attributes due to error:" + e);
        }
    }

    @Test
    public void testUpdateGroupName() {
        String name = "Dining Group";
        String newName = "Meeting Group";

        AylaCollection room = createEmptyGroup(name);

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.updateGroupName(room.collectionUuid, newName, future, future);
        try {
            AylaCollection updatedRoom = future.get();
            assertNotNull(updatedRoom.collectionUuid);
            assertEquals(updatedRoom.name, newName);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to update collection name due to error:" + e);
        }
    }

    @Test
    public void testDeleteGroup() {
        AylaCollection createdGroup = createEmptyGroup(
                "Empty group delete test");
        assertNotNull(createdGroup.collectionUuid);
        RequestFuture future = RequestFuture.newFuture();
        _testGroupManager.deleteGroup(createdGroup.collectionUuid,
                future, future);
        try {
            Object deleteGroup = future.get();
            assertNull(deleteGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to delete group due to error:" + e);
        }
    }

    @Test
    public void testDeleteAllGroups() {
        RequestFuture future = RequestFuture.newFuture();
        _testGroupManager.deleteAllGroups(future, future);
        try {
            Object deleteGroup = future.get();
            assertNull(deleteGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to delete group due to error:" + e);
        }
    }

    @Test
    public void testDeleteResourcesFromGroup() {
        AylaCollection collection = new AylaCollection();
        collection.name = "Living-Room";

        AylaCollectionDevice device1 = new AylaCollectionDevice();
        device1.dsn = TEST_DEVICE_DSN;
        collection.devices = new AylaCollectionDevice[]{device1};

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(
                collection.name,
                collection.devices,
                collection.childCollections,
                collection.custom_attributes,
                future, future);

        AylaCollection createdGroup = null;
        try {
            createdGroup = future.get();
            assertNotNull(createdGroup.collectionUuid);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        future = RequestFuture.newFuture();
        _testGroupManager.deleteResourcesFromGroup(
                createdGroup.collectionUuid,
                collection.devices,
                null,
                future, future);

        try {
            AylaCollection updatedCollection = future.get();
            assertNotNull(updatedCollection);
            assertEquals(0, updatedCollection.devices.length);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDeleteAttributesFromGroup() {
        AylaCollection collection = new AylaCollection();
        collection.name = "Living-Room";

        Map<String, String> custom_attributes = new HashMap<>();;
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testGroupManager.createGroup(
                collection.name,
                null,
                null,
                custom_attributes,
                future, future);

        AylaCollection createdGroup = null;
        try {
            createdGroup = future.get();
            assertNotNull(createdGroup.collectionUuid);
            _recyclableGroups.add(createdGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        Map<String, String> deleteAttributes = new HashMap<>();;
        deleteAttributes.put("color", "#F08082");
        future = RequestFuture.newFuture();

        _testGroupManager.deleteAttributesFromGroup(
                createdGroup,
                deleteAttributes,
                future, future);
        try {
            AylaCollection updatedCollection = future.get();
            assertNotNull(updatedCollection);

        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUpdateFrom() {

        AylaCollection collection = new AylaCollection();
        collection.name = "Living-Room";
        AylaCollectionDevice device = new AylaCollectionDevice();
        List<AylaDevice> devices = getSessionManager().getDeviceManager().getDevices();
        if (devices.size() > 0) {
            device.updateFrom(devices.get(0), null);

            collection.devices = new AylaCollectionDevice[]{device};
            RequestFuture<AylaCollection> future = RequestFuture.newFuture();

            _testGroupManager.createGroup(
                    collection.name,
                    collection.devices,
                    null,
                    collection.custom_attributes,
                    future, future);

            try {
                AylaCollection createdGroup = future.get();
                assertNotNull(createdGroup);
                _recyclableGroups.add(createdGroup);
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, "failed to add collection devices due to error:" + e);
                fail(e.getMessage());
            }
        } else {
            fail("Test failed - No device registered to the user");
        }
    }

    @Test
    public void testAddShare() {
        String name = "An empty group";
        AylaCollection createdCollection = createEmptyGroup(name);
        assertNotNull(createdCollection);
        assertNotNull(createdCollection.collectionUuid);
        assertEquals(createdCollection.name, name);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        Date _startDate = calendar.getTime();

        AylaShare share = new AylaShare(TEST_EMAIL_ID, "write",
                createdCollection.collectionUuid, "GROUP",
                null, null, null);
        RequestFuture<AylaShare> future = RequestFuture.newFuture();
        _testGroupManager.shareGroup(share, future, future);
        try {
            AylaShare shareAdded = future.get();
            //Log.d(TAG, "shareAdded:" + shareAdded);
            assertNotNull(shareAdded);
            assertNotNull(shareAdded.getId());
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchOwnedShares() {
        RequestFuture<AylaShare[]> future = RequestFuture.newFuture();

        _testGroupManager.fetchOwnedShares(future, future);
        AylaShare[] share = null;
        try {
            share = future.get();
            assertNotNull(share);
            assertNotEquals(0, share.length);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchReceivedShares() {
        RequestFuture<AylaShare[]> future = RequestFuture.newFuture();
        _testGroupManager.fetchReceivedShares(future, future);
        AylaShare[] share = null;
        try {
            share = future.get();
            assertNotNull(share);
            assertNotEquals(0, share.length);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUpdateShare() {
        AylaCollectionDevice device1 = new AylaCollectionDevice();
        device1.dsn = TEST_DEVICE_DSN;
        AylaCollectionDevice[] devicesToCreate = null;
        devicesToCreate = new AylaCollectionDevice[]{device1};
        AylaCollection createdCollection = createGroup(
                "Group to update schedule",
                devicesToCreate,
                null,
                null);
        assertNotNull(createdCollection);
        assertNotNull(createdCollection.collectionUuid);
        AylaShare share = new AylaShare(TEST_EMAIL_ID, "write",
                createdCollection.collectionUuid, "GROUP",
                null, null, null);
        AylaShare createdShare = null;
        RequestFuture<AylaShare> future = RequestFuture.newFuture();
        _testGroupManager.shareGroup(share,future,future);
        try {
            createdShare = future.get();
            assertNotNull(createdShare);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        AylaShare shareToUpdate = new AylaShare(TEST_EMAIL_ID, "read",
                null, null,
                null, null, null);
        shareToUpdate.setRoleName("user");
        shareToUpdate.setStartDateAt("2021-02-24");

        RequestFuture<AylaShare> futureUpdate = RequestFuture.newFuture();
        _testGroupManager.updateShare(createdShare.getId(),shareToUpdate,futureUpdate,futureUpdate);
        AylaShare shareUpdate = null;
        try {
            shareUpdate = futureUpdate.get();
            assertNotNull(shareUpdate);
            assertEquals(shareUpdate.getOperation(), shareToUpdate.getOperation());
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDeleteShare() {
        String name = "An empty group";
        AylaCollection collection = createEmptyGroup(name);
        assertNotNull(collection);
        assertNotNull(collection.collectionUuid);
        assertEquals(collection.name, name);

        AylaShare share = new AylaShare(TEST_EMAIL_ID, "write",
                collection.collectionUuid, "GROUP",
                null, null, null);
        AylaShare createdShare;
        RequestFuture<AylaShare> future = RequestFuture.newFuture();
        _testGroupManager.shareGroup(share, future, future);
        AylaCollection createdCollection = null;
        RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
        try {
            createdShare = future.get();
            assertNotNull(createdShare);
            _testGroupManager.deleteShare(createdShare.getId(), futureDelete, futureDelete);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRemoveDevicesFromGroup() {
        AylaCollection parentCollection = new AylaCollection();
        parentCollection.name = "A group with ayla device";
        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(2, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device = new AylaCollectionDevice();
            device.dsn = TEST_DEVICE_DSN;
            devicesToCreate = new AylaCollectionDevice[]{device};
        }
        parentCollection.devices = devicesToCreate;

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testGroupManager.createGroup(
                parentCollection.name,
                devicesToCreate,
                null,
                null,
                future, future);

        AylaCollection createdParentGroup = null;
        try {
            createdParentGroup = future.get();
            assertNotNull(createdParentGroup);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        AylaCollection childCollection = createEmptyGroup(
                "child collection");
        AylaChildCollection aylaChildCollection = new AylaChildCollection();
        aylaChildCollection.collectionUuid = childCollection.collectionUuid;

        AylaCollectionDevice childDevice = new AylaCollectionDevice();
        childDevice.dsn = TEST_DEVICE_DSN;
        AylaCollectionDevice[] childDevices = new AylaCollectionDevice[]{childDevice};
        aylaChildCollection.devices = childDevices;

        AylaChildCollection[] childCollectionsToAdd = new AylaChildCollection[]{aylaChildCollection};
        RequestFuture<AylaCollection>  futureAdd = RequestFuture.newFuture();
        _testGroupManager.addResourcesToGroup(
                createdParentGroup.collectionUuid,
                null,
                childCollectionsToAdd,
                futureAdd, futureAdd);

        AylaCollectionDevice deviceRemove = new AylaCollectionDevice();
        deviceRemove.dsn = TEST_DEVICE_DSN1;
        AylaCollectionDevice[] devicesRemove = new AylaCollectionDevice[]{deviceRemove};
        RequestFuture<AylaCollection> futureRemove = RequestFuture.newFuture();
        _testGroupManager.removeDevicesFromGroup(createdParentGroup.collectionUuid,
                devicesRemove, true, futureRemove, futureRemove);

        AylaCollection collectionRemove = null;
        try {
            collectionRemove = futureRemove.get();
            assertEquals(0, collectionRemove.devices.length);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }
    ///////////////////////////////////////////////////////////////////////////////
    private AylaSessionManager getSessionManager() {
        return AylaNetworks.sharedInstance().getSessionManager(
                TestConstants.TEST_SESSION_NAME);
    }

    private AylaCollection createEmptyGroup(String name) {
        return createGroup(name, null, null, null);
    }

    private AylaCollection createGroup(
            String name,
            AylaCollectionDevice[] devices,
            AylaChildCollection[] childCollections,
            Map<String, String> attributes) {
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testGroupManager.createGroup(name, devices,
                childCollections, attributes, future, future);
        try {
            AylaCollection createdGroup = future.get();
            assertNotNull(createdGroup);
            _recyclableGroups.add(createdGroup);
            return createdGroup;
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to create Group " + name + " due to: " + e);
            return null;
        }
    }
}
