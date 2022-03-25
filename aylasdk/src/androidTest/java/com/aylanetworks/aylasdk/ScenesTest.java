package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */

import android.icu.text.SimpleDateFormat;
import android.util.Log;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.android.volley.Response;
import com.aylanetworks.aylasdk.error.AylaError;
import com.aylanetworks.aylasdk.error.ErrorListener;
import com.aylanetworks.aylasdk.error.RequestFuture;
import com.aylanetworks.aylasdk.error.ServerError;
import com.aylanetworks.aylasdk.gss.AylaGroupManager;
import com.aylanetworks.aylasdk.gss.AylaSceneManager;
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
import java.text.ParseException;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class ScenesTest {
    private static final String TAG = "ScenesTest";

    private AylaCollection _testScene;
    private AylaSceneManager _testSceneManager;
    private AylaGroupManager _testGroupManager;
    private List<AylaCollection> _recyclableScenes;
    public static final String TEST_DEVICE_DSN = "AC000W000000001";
    public static final String TEST_DEVICE_DSN1 = "AC000W000000001";

    @Before
    public void setUp() {
        assertTrue(TestConstants.signIn(InstrumentationRegistry.getContext()));
        AylaSessionManager sm = AylaNetworks.sharedInstance().getSessionManager(
                TestConstants.TEST_SESSION_NAME);
        assertNotNull(sm);
        _testSceneManager = sm.getSceneManager();
        _testGroupManager = sm.getGroupManager();
        _recyclableScenes = new ArrayList<>();
    }

    @After
    public void tearDown() {
        if (_recyclableScenes != null && _recyclableScenes.size() > 0) {
            List<String> sceneUUIDs = new ArrayList<>();
            for (AylaCollection scene : _recyclableScenes) {
                sceneUUIDs.add(scene.collectionUuid);
            }

            EmptyListener emptyListener = new EmptyListener();
            _testSceneManager.deleteScenes(sceneUUIDs, emptyListener, emptyListener);

            try {
                // there may still ongoing delete scene calls
                // before the test process was killed, so we should
                // wait some to make sure all scenes get deleted.
                Thread.sleep(_recyclableScenes.size() * 2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (_testScene != null && _testScene.collectionUuid != null) {
            _testSceneManager.deleteScene(
                    _testScene.collectionUuid,
                    new Response.Listener<AylaAPIRequest.EmptyResponse>() {
                        @Override
                        public void onResponse(AylaAPIRequest.EmptyResponse response) {
                            Log.d(TAG, "deleted scene: " + _testScene.name
                                    + ", uuid:" + _testScene.collectionUuid);
                        }
                    }, new ErrorListener() {
                        @Override
                        public void onErrorResponse(AylaError error) {
                            Log.d(TAG, "failed to delete scene: " + _testScene.name);
                        }
                    });
        }
    }

    @Test
    public void testCreateEmptyScene() {
        String name = "An empty scene";
        AylaCollection createdScene = createEmptyScene(name);
        assertNotNull(createdScene);
        assertNotNull(createdScene.collectionUuid);
        assertEquals(createdScene.name, name);
    }

    @Test
    public void testSceneNameLength() {
        String name = "Scene name greater than 32 characters";
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.createScene(
                name,
                null,
                null,
                null,
                future, future);
        try {
            AylaCollection createdScene = future.get();
            _recyclableScenes.add(createdScene);
        } catch (Exception e) {
            try{
                ServerError error = (ServerError) e.getCause();
                assertTrue(error.getServerResponseCode() == 422);
                assertTrue(error.getMessage().contains(
                        "Collection name should contain alphanumeric characters, whitespaces, " +
                                "underscore(_) and hyphen(-) and should be between " +
                                "1-32 characters"));
            } catch (ClassCastException ex){
                fail("testSceneNameLength failed with unexpected " +
                        "error code " + e);
            }
        }
    }

    @Test
    public void testCreateSceneWithResources() {
        AylaCollection scene = new AylaCollection();
        scene.name = "A scene with ayla device";
        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(3, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;
            devicesToCreate = new AylaCollectionDevice[]{device1};
        }
        scene.devices = devicesToCreate;
        AylaChildCollection[] aylaChildCollection = new AylaChildCollection[0];
        scene.childCollections = aylaChildCollection;

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.createScene(
                scene.name,
                scene.devices,
                scene.childCollections,
                null,
                future, future);
        try {
            AylaCollection createdScene = future.get();
            assertNotNull(createdScene.collectionUuid);
            if (scene.devices != null && scene.devices.length>0)
                assertEquals(createdScene.devices.length, scene.devices.length);
            assertEquals(createdScene.childCollections.length, scene.childCollections.length);
            _recyclableScenes.add(createdScene);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getMessage() != null && !e.getMessage().contains("Child collection is not found")) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testCreateScene() {
        AylaCollection scene = new AylaCollection();
        scene.name = "A scene with ayla device";
        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(3, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;

            AylaCollectionProperty property = new AylaCollectionProperty();
            //TODO
            /*property.propertyName = "Blue_LED";
            property.propertyValue = 0;*/
            /*property.propertyName = "cmd";
            property.propertyValue = "I m string";*/
            /*property.propertyName = "decimal_in";
            property.propertyValue = 12.34;*/
            property.propertyName = "input";
            property.propertyValue = 3;
            device1.states = new AylaCollectionProperty[]{property};

            devicesToCreate = new AylaCollectionDevice[]{device1};
        }
        scene.devices = devicesToCreate;
        AylaChildCollection[] aylaChildCollection = new AylaChildCollection[0];
        scene.childCollections = aylaChildCollection;
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.createScene(
                scene.name,
                scene.devices,
                scene.childCollections,
                null,
                future, future);
        try {
            AylaCollection createdScene = future.get();
            assertNotNull(createdScene.collectionUuid);
            //TODO
            /*devicesToCreate[0].states[0].propertyValue = null;
            devicesToCreate[0].states[0].propertyValue = createdScene.devices[0].states[0].propertyValue;
            assertEquals(createdScene.devices[0].states[0].propertyValue,
                    devicesToCreate[0].states[0].propertyValue);*/
            //fails - junit.framework.AssertionFailedError: expected:<0> but was:<0>
            //assertEquals(createdScene.devices[0].states[0].propertyValue,"I m string");
            //success
            /*devicesToCreate[0].states[0].propertyValue = null;
            devicesToCreate[0].states[0].propertyValue = createdScene.devices[0].states[0].propertyValue;*/

            //assertEquals(createdScene.devices[0].states[0].propertyValue, devicesToCreate[0].states[0].propertyValue);
            // - fails - junit.framework.AssertionFailedError: expected:<12.34> but was:<12.34>

            /*devicesToCreate[0].states[0].propertyValue = null;
            devicesToCreate[0].states[0].propertyValue = createdScene.devices[0].states[0].propertyValue;*/

            //assertEquals(createdScene.devices[0].states[0].propertyValue,(int)devicesToCreate[0].states[0].propertyValue);
            //java.lang.ClassCastException: java.lang.String cannot be cast to java.lang.Integer
            //assertEquals(createdScene.devices[0].states[0].propertyValue, devicesToCreate[0].states[0].propertyValue);
            //- fails - junit.framework.AssertionFailedError: expected:<3> but was:<3>
            assertEquals(createdScene.name, scene.name);
            assertEquals(createdScene.devices.length, scene.devices.length);
            assertEquals(createdScene.childCollections.length, scene.childCollections.length);
            _recyclableScenes.add(createdScene);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getMessage() != null && !e.getMessage().contains("Child collection is not found")) {
                fail(e.getMessage());
            }
        }
    }

    @Test
    public void testFetchAttributesForScene() {
        Map<String, String> custom_attributes = new HashMap<>();;
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.createScene(
                "A sample scene collection",
                null,
                null,
                custom_attributes,
                future, future);

        AylaCollection createdScene = null;
        try {
            createdScene = future.get();
            assertNotNull(createdScene);
            _recyclableScenes.add(createdScene);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to create scene collection due to error:" + e);
        }

        RequestFuture<Map<String, String>> future2 = RequestFuture.newFuture();
        _testSceneManager.fetchAttributesForScene(createdScene.collectionUuid, future2, future2);
        try {
            Map<String, String> fetchedAttr = future2.get();
            assertNotNull(fetchedAttr);
            assertEquals(fetchedAttr.toString(), ("{custom_attributes="+custom_attributes.toString()+"}"));
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch scene due to error:" + e);
        }
    }

    @Test
    public void testAddResourcesToScene() {
        AylaCollection parentCollection = createEmptyScene(
                "scene collection");

        AylaCollectionDevice[] devicesToAdd = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to add
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(3, size / 2));  //  1 <= num <= 3
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
            property.propertyName = "prop_name1";
            property.propertyValue = "1";
            device1.states = new AylaCollectionProperty[]{property};

            devicesToAdd = new AylaCollectionDevice[]{device1};
        }

        AylaCollection updatedParentCollection = null;
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.addResourcesToScene(
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

        AylaCollection childCollection = new AylaCollection();
        RequestFuture<AylaCollection> futureGroup = RequestFuture.newFuture();
        _testGroupManager.createGroup("Empty Group", futureGroup, futureGroup);
        try {
            childCollection = futureGroup.get();
            assertNotNull(childCollection);
            assertNotNull(childCollection.collectionUuid);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        AylaChildCollection aylaChildCollection = new AylaChildCollection();
        aylaChildCollection.collectionUuid = childCollection.collectionUuid;
        AylaChildCollection[] childCollectionsToAdd = new AylaChildCollection[]{aylaChildCollection};
        future = RequestFuture.newFuture();
        _testSceneManager.addResourcesToScene(
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
    public void testAddAttributesToScene() {
        AylaCollection createdCollection = createEmptyScene(
                "Empty Scene to add attribute");
        assertNotNull(createdCollection.collectionUuid);

        Map<String, String> custom_attributes = new HashMap<>();
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.addAttributesToScene(createdCollection, custom_attributes, future, future);
        try {
            AylaCollection updatedCollection = future.get();
            assertNotNull(updatedCollection.collectionUuid);
            assertEquals(updatedCollection.custom_attributes.get("color"), custom_attributes.get("color"));
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to add custom attributes due to error:" + e);
        }
    }

    @Test
    public void testFetchScenesHavingDSN() {
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
        _testSceneManager.createScene(
                "scene test fetch using dsn",
                devicesToCreate,
                null,
                null,
                future, future);
        try {
            AylaCollection createdCollection = future.get();
            assertNotNull(createdCollection);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to create scene due to error:" + e);
        }

        RequestFuture<AylaCollection[]> future2 = RequestFuture.newFuture();
        _testSceneManager.fetchScenesHavingDSN(device.dsn, future2, future2);
        try {
            AylaCollection[] fetchedCollections = future2.get();
            assertNotNull(fetchedCollections);
            assertTrue(fetchedCollections.length >= 1);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch scene due to error:" + e);
        }
    }

    @Test
    public void testFetchScene() {
        AylaCollection collection = createEmptyScene("fetch a scene");
        assertNotNull(collection);
        assertNotNull(collection.collectionUuid);

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.fetchScene(collection.collectionUuid, future, future);
        try {
            AylaCollection fetchedCollection = future.get();
            assertNotNull(fetchedCollection.collectionUuid);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch scene due to error:" + e);
        }
    }

    @Test
    public void testFetchScenes() {
        createEmptyScene("scene1");
        createEmptyScene("scene2");

        RequestFuture<AylaCollection[]> future = RequestFuture.newFuture();
        _testSceneManager.fetchScenes(future, future);
        try {
            AylaCollection[] fetchedCollections = future.get();
            assertNotNull(fetchedCollections);
            assertTrue(fetchedCollections.length >= 2);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to fetch scene due to error:" + e);
        }
    }

    //TODO SVC-16882
    @Test
    public void testUpdateScene() {

        AylaCollection scene = new AylaCollection();
        scene.name = "Scene upd dev child state";

        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(3, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;
                AylaCollectionProperty property = new AylaCollectionProperty();
                property.propertyName = "prop_name1";
                property.propertyValue = "1";
                device.properties = new AylaCollectionProperty[]{property};

                AylaCollectionProperty state = new AylaCollectionProperty();
                state.propertyName = "prop_name1";
                state.propertyValue = "1";
                device.states = new AylaCollectionProperty[]{state};

                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;

            AylaCollectionProperty property = new AylaCollectionProperty();
            property.propertyName = "prop_name1";
            property.propertyValue = "1";
            device1.properties = new AylaCollectionProperty[]{property};

            AylaCollectionProperty state = new AylaCollectionProperty();
            state.propertyName = "prop_name1";
            state.propertyValue = "1";
            device1.states = new AylaCollectionProperty[]{state};

            devicesToCreate = new AylaCollectionDevice[]{device1};
        }
        scene.devices = devicesToCreate;

        AylaChildCollection[] aylaChildCollection = null;

        scene.childCollections = aylaChildCollection;

        AylaCollection createdScene = null;
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.createScene(
                scene.name,
                scene.devices,
                scene.childCollections,
                null,
                future, future);

        try {
            createdScene = future.get();
            assertNotNull(createdScene.collectionUuid);
            assertEquals(createdScene.name, scene.name);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        scene.devices[0].states[0].propertyValue = "2";

        RequestFuture<AylaCollection> futureUpdate = RequestFuture.newFuture();
        _testSceneManager.updateStatesOfSceneResources(createdScene.collectionUuid,
                scene.devices, scene.childCollections,
                futureUpdate, futureUpdate);
        try {
            AylaCollection updatedScene = futureUpdate.get();
            assertNotNull(updatedScene.collectionUuid);
            assertEquals(
                    updatedScene.devices[0].states[0].propertyValue,
                    scene.devices[0].states[0].propertyValue);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to update scene due to error:" + e);
        }
    }

    @Test
    public void testUpdateChildCollectionStateScene() {
        AylaCollection scene = createEmptyScene(
                "Scene collection");

        AylaCollectionDevice[] devicesToCreate = null;
        scene.devices = devicesToCreate;

        AylaCollection childCollection = new AylaCollection();
        RequestFuture<AylaCollection> futureGroup = RequestFuture.newFuture();
        _testGroupManager.createGroup("Empty Group", futureGroup, futureGroup);
        try {
            childCollection = futureGroup.get();
            assertNotNull(childCollection);
            assertNotNull(childCollection.collectionUuid);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        AylaChildCollection aylaChildCollection = new AylaChildCollection();
        aylaChildCollection.collectionUuid = childCollection.collectionUuid;

        AylaCollectionProperty property = new AylaCollectionProperty();
        property.propertyName = "Blue_LED";
        property.propertyValue = "0";
        aylaChildCollection.states = new AylaCollectionProperty[]{property};

        AylaChildCollection[] childCollectionsToAdd = new AylaChildCollection[]{aylaChildCollection};
        RequestFuture<AylaCollection> futureAdd = RequestFuture.newFuture();
        AylaCollection updatedScene = null;
        _testSceneManager.addResourcesToScene(
                scene.collectionUuid,
                scene.devices,
                childCollectionsToAdd,
                futureAdd, futureAdd);

        try {
            updatedScene = futureAdd.get();
            assertNotNull(updatedScene);
            assertEquals(updatedScene.childCollections.length, childCollectionsToAdd.length);
        } catch (InterruptedException | ExecutionException e) {
            Log.d(TAG, "failed to add child collection due to error:" + e);
            fail(e.getMessage());
        }

        childCollectionsToAdd[0].states[0].propertyValue = "1";

        RequestFuture<AylaCollection> futureUpdate = RequestFuture.newFuture();
        _testSceneManager.updateStatesOfSceneResources(updatedScene.collectionUuid,
                scene.devices, childCollectionsToAdd,
                futureUpdate, futureUpdate);
        try {
            AylaCollection updatedSceneWithChild = futureUpdate.get();
            assertNotNull(updatedSceneWithChild.collectionUuid);
            assertEquals(
                    updatedSceneWithChild.childCollections[0].states[0].propertyValue,
                    childCollectionsToAdd[0].states[0].propertyValue);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to update scene due to error:" + e);
        }
    }

    @Test
    public void testUpdateSceneAttributes() {
        AylaCollection scene = new AylaCollection();
        scene.name = "Scene upd custatt state";

        Map <String, String> custom_attributes = new HashMap<>();
        custom_attributes.put("pincode", "600001");
        custom_attributes.put("tag", "houseHoldTag");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        AylaCollection createdScene = null;

        _testSceneManager.createScene(
                scene.name,
                null,
                null,
                custom_attributes,
                future, future);

        try {
            createdScene = future.get();
            assertNotNull(createdScene.collectionUuid);
            assertEquals(createdScene.name, scene.name);
        } catch (InterruptedException | ExecutionException e) {
            if (e.getMessage() != null && !e.getMessage().contains("Child collection is not found")) {
                fail(e.getMessage());
            }
        }

        custom_attributes.put("pincode", "600444");
        custom_attributes.put("tag", "officeTag");
        RequestFuture<AylaCollection> futureUpdate = RequestFuture.newFuture();
        _testSceneManager.updateAttributesForScene(createdScene,
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
    public void testUpdateSceneName() {
        String name = "Dining SCENE";
        String newName = "Meeting SCENE";

        AylaCollection room = createEmptyScene(name);

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.updateSceneName(room.collectionUuid, newName, future, future);
        try {
            AylaCollection updatedRoom = future.get();
            assertNotNull(updatedRoom.collectionUuid);
            assertEquals(updatedRoom.name, newName);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to update collection name due to error:" + e);
        }
    }

    @Test
    public void testDeleteScene() {
        AylaCollection createdCollection = createEmptyScene(
                "Empty scene delete test");
        assertNotNull(createdCollection.collectionUuid);
        RequestFuture future = RequestFuture.newFuture();
        _testSceneManager.deleteScene(createdCollection.collectionUuid,
                future, future);
    }

    @Test
    public void testDeleteAllScenes() {
        RequestFuture future = RequestFuture.newFuture();
        _testSceneManager.deleteAllScenes(future, future);
        assertEquals(0, _recyclableScenes.size());
    }

    @Test
    public void testDeleteResourcesFromScene() {
        AylaCollection collection = new AylaCollection();
        collection.name = "Living-Room";

        AylaCollectionDevice device1 = new AylaCollectionDevice();
        device1.dsn = TEST_DEVICE_DSN;
        collection.devices = new AylaCollectionDevice[]{device1};

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testSceneManager.createScene(
                collection.name,
                collection.devices,
                collection.childCollections,
                null,
                future, future);

        AylaCollection createdScene = null;
        try {
            createdScene = future.get();
            assertNotNull(createdScene.collectionUuid);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        future = RequestFuture.newFuture();
        _testSceneManager.deleteResourcesFromScene(
                createdScene.collectionUuid,
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
    public void testDeleteAttributesFromScene() {
        AylaCollection collection = new AylaCollection();
        collection.name = "Living-Room";

        Map<String, String> custom_attributes = new HashMap<>();;
        custom_attributes.put("color", "#F08082");
        custom_attributes.put("sort_order", "ascending");

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testSceneManager.createScene(
                collection.name,
                null,
                null,
                custom_attributes,
                future, future);

        AylaCollection createdCollection = null;
        try {
            createdCollection = future.get();
            assertNotNull(createdCollection.collectionUuid);
            _recyclableScenes.add(createdCollection);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        Map<String, String> deleteAttributes = new HashMap<>();;
        deleteAttributes.put("color", "#F08082");
        future = RequestFuture.newFuture();

        _testSceneManager.deleteAttributesFromScene(
                createdCollection.collectionUuid,
                deleteAttributes,
                future, future);
        try {
            AylaCollection updatedCollection = future.get();
            assertNotNull(updatedCollection);
            //assertEquals(0, updatedCollection.customAttributes.length);//TODO
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
        String[] properties = {"Blue_LED","Green_LED"};
        if (devices.size() > 0) {
            device.updateFrom(devices.get(0), properties);

            collection.devices = new AylaCollectionDevice[]{device};
            RequestFuture<AylaCollection> future = RequestFuture.newFuture();

            _testSceneManager.createScene(
                    collection.name,
                    collection.devices,
                    null,
                    null,
                    future, future);
            try {
                AylaCollection createdScene = future.get();
                assertNotNull(createdScene);
                assertEquals(createdScene.devices[0].states[0].propertyName, "Blue_LED");
                //assertEquals(createdScene.devices[0].states[0].propertyValue, 1);//TODO boolean issue
                _recyclableScenes.add(createdScene);
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, "failed to add collection devices due to error:" + e);
                fail(e.getMessage());
            }
        } else {
            fail("Test failed - No device registered to the user");
        }

    }

    @Test
    public void testTriggerScene() {
        String name = "Trigger Dining SCENE";

        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(3, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;

                AylaCollectionProperty property = new AylaCollectionProperty();
                property.propertyName = "Blue_LED";
                property.propertyValue = 1;
                device.states = new AylaCollectionProperty[]{property};

                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;

            //TODO
            AylaCollectionProperty property1 = new AylaCollectionProperty();
            property1.propertyName = "Blue_LED";
            property1.propertyValue = 1;
            AylaCollectionProperty property2 = new AylaCollectionProperty();
            property2.propertyName = "cmd";
            property2.propertyValue = "I am a String";
            AylaCollectionProperty property3 = new AylaCollectionProperty();
            property3.propertyName = "decimal_in";
            property3.propertyValue = 12.34;
            AylaCollectionProperty property4 = new AylaCollectionProperty();
            property4.propertyName = "input";
            property4.propertyValue = 9;
            device1.states = new AylaCollectionProperty[]{property1,property2, property3, property4};

            devicesToCreate = new AylaCollectionDevice[]{device1};
        }

        RequestFuture<AylaCollection> futureCreate = RequestFuture.newFuture();
        _testSceneManager.createScene(name, devicesToCreate, null,
                null, futureCreate, futureCreate);
        AylaCollection createdScene = null;
        try {
            createdScene = futureCreate.get();
            assertNotNull(createdScene);
            assertNotNull(createdScene.collectionUuid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        RequestFuture<AylaCollectionTriggerResponse[]> future = RequestFuture.newFuture();
        _testSceneManager.triggerScene(createdScene.collectionUuid, future, future);

        try {
            AylaCollectionTriggerResponse[] updatedScene = future.get();
            assertNotNull(updatedScene);
            assertEquals(devicesToCreate[0].states[0].propertyName, updatedScene[0].name);
            assertEquals(String.valueOf(201),updatedScene[0].status);

        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testEnableTriggerScene() {
        String name = "Enable Dining SCENE";

        AylaCollectionDevice[] devicesToCreate = null;
        AylaDeviceManager deviceManager = getSessionManager().getDeviceManager();
        if (deviceManager != null && deviceManager.getDevices().size() > 0) {
            // Randomly select N, 1 <= N <= 3, devices to update
            int size = deviceManager.getDevices().size();
            int num = Math.max(1, Math.min(3, size / 2));  //  1 <= num <= 3
            devicesToCreate = new AylaCollectionDevice[num];
            Random rnd = new Random(System.currentTimeMillis());
            for (int i = 0; i < num; i++) {
                AylaCollectionDevice device = new AylaCollectionDevice();
                device.dsn = deviceManager.getDevices().get(rnd.nextInt(size)).dsn;

                AylaCollectionProperty property = new AylaCollectionProperty();
                property.propertyName = "Blue_LED";
                property.propertyValue = "1";
                device.states = new AylaCollectionProperty[]{property};

                devicesToCreate[i] = device;
            }
        } else {
            AylaCollectionDevice device1 = new AylaCollectionDevice();
            device1.dsn = TEST_DEVICE_DSN;

            AylaCollectionProperty property = new AylaCollectionProperty();
            property.propertyName = "prop_name1";
            property.propertyValue = "1";
            device1.states = new AylaCollectionProperty[]{property};

            devicesToCreate = new AylaCollectionDevice[]{device1};
        }

        RequestFuture<AylaCollection> futureCreate = RequestFuture.newFuture();
        _testSceneManager.createScene(name, devicesToCreate, null,
                null, futureCreate, futureCreate);
        AylaCollection createdScene = null;
        try {
            createdScene = futureCreate.get();
            assertNotNull(createdScene);
            assertNotNull(createdScene.collectionUuid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Boolean isActive = false;
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.enableTriggerScene(createdScene.collectionUuid, isActive, future, future);

        try {
            AylaCollection updatedScene = future.get();
            assertNotNull(updatedScene);
            assertEquals(isActive, updatedScene.isActive);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAddScheduleToScene() {
        AylaSchedule schedule = new AylaSchedule();
        AylaCollection collection = new AylaCollection();

        Date startDate = getDate("2021-02-22");
        schedule.setStartDate(startDate);
        schedule.setStartTimeEachDay("20:00:00");
        collection.schedule = schedule;
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        AylaCollectionDevice device1 = new AylaCollectionDevice();
        device1.dsn = TEST_DEVICE_DSN;
        AylaCollectionProperty property1 = new AylaCollectionProperty();
        property1.propertyName = "Green_LED";
        property1.propertyValue = 1;
        device1.states = new AylaCollectionProperty[]{property1};
        AylaCollectionDevice[] devicesToCreate = null;
        devicesToCreate = new AylaCollectionDevice[]{device1};
        _testSceneManager.createScene(
                "Scene to add schedule",
                devicesToCreate,
                null,
                null,
                null, future, future);
        AylaCollection createdCollection = null;
        try {
            createdCollection = future.get();
            assertNotNull(createdCollection.collectionUuid);

        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        RequestFuture<AylaCollection> addFuture = RequestFuture.newFuture();
        AylaSchedule newSchedule = new AylaSchedule();
        Date newStartDate = getDate("2021-02-22");
        newSchedule.setStartDate(newStartDate);
        newSchedule.setStartTimeEachDay("21:00:00");

        _testSceneManager.addScheduleToScene(createdCollection, newSchedule, addFuture, addFuture);
        try {
            AylaCollection updatedCollection = addFuture.get();
            assertNotNull(updatedCollection.collectionUuid);
            assertNotNull(updatedCollection.schedule);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to add schedule due to error:" + e);
        }
    }

    @Test
    public void testUpdateScheduleForScene() {
        AylaCollection collection = new AylaCollection();
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        AylaSchedule schedule = new AylaSchedule();
        Date startDate = getDate("2021-02-22");
        schedule.setStartDate(startDate);
        schedule.setStartTimeEachDay("20:00:00");
        collection.schedule = schedule;
        AylaCollectionDevice device1 = new AylaCollectionDevice();
        device1.dsn = TEST_DEVICE_DSN;
        AylaCollectionProperty property1 = new AylaCollectionProperty();
        property1.propertyName = "Green_LED";
        property1.propertyValue = 0;
        device1.states = new AylaCollectionProperty[]{property1};
        AylaCollectionDevice[] devicesToCreate = null;
        devicesToCreate = new AylaCollectionDevice[]{device1};
        _testSceneManager.createScene(
                "Scene to update schedule",
                devicesToCreate,
                null,
                null,
                collection.schedule,
                future, future);
        AylaCollection createdCollection = null;
        try {
            createdCollection = future.get();
            assertNotNull(createdCollection.collectionUuid);
            assertNotNull(createdCollection.schedule);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
        AylaSchedule newSchedule = new AylaSchedule();
        Date newStartDate = getDate("2021-02-23");
        newSchedule.setStartDate(newStartDate);
        newSchedule.setStartTimeEachDay("21:00:00");
        RequestFuture<AylaCollection> updatedFuture = RequestFuture.newFuture();
        _testSceneManager.updateScheduleForScene(createdCollection, newSchedule, updatedFuture, updatedFuture);
        try {
            AylaCollection updatedCollection = updatedFuture.get();
            assertNotNull(updatedCollection.collectionUuid);
            assertNotNull(updatedCollection.schedule);
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to update schedule due to error:" + e);
        }

    }

    @Test
    public void testDeleteScheduleForScene() {
        AylaCollection collection = new AylaCollection();
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        AylaSchedule schedule = new AylaSchedule();
        Date startDate = getDate("2021-02-22");
        schedule.setStartDate(startDate);
        schedule.setStartTimeEachDay("20:00:00");
        collection.schedule = schedule;

        _testSceneManager.createScene(
                "Scene to delete schedule",
                null,
                null,
                null,
                collection.schedule,
                future, future);
        AylaCollection createdCollection = null;
        try {
            createdCollection = future.get();
            assertNotNull(createdCollection.collectionUuid);
            assertNotNull(createdCollection.schedule);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        RequestFuture<AylaAPIRequest.EmptyResponse> emptyFuture = RequestFuture.newFuture();
        _testSceneManager.deleteScheduleFromScene(createdCollection, emptyFuture, emptyFuture);
        AylaAPIRequest.EmptyResponse updatedCollection = null;
        try {
            updatedCollection = emptyFuture.get();
            //assertNotNull(updatedCollection);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

    }

    private Date getDate(String stringDate) {
        Date date = null;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        try {
            date = (Date)formatter.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    @Test
    public void testAddShare() {
        String name = "An empty scene";
        AylaCollection createdCollection = createEmptyScene(name);
        assertNotNull(createdCollection);
        assertNotNull(createdCollection.collectionUuid);
        assertEquals(createdCollection.name, name);

        DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MONTH, 1);
        Date _startDate = calendar.getTime();

        AylaShare share = new AylaShare(TEST_EMAIL_ID, "write",
                createdCollection.collectionUuid, "SCENE",
                null, null, null);
        RequestFuture<AylaShare> future = RequestFuture.newFuture();
        _testSceneManager.shareScene(share, future, future);
        try {
            AylaShare shareAdded = future.get();
            assertNotNull(shareAdded);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testFetchOwnedShares() {
        RequestFuture<AylaShare[]> future = RequestFuture.newFuture();

        _testSceneManager.fetchOwnedShares(future, future);
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
        _testSceneManager.fetchReceivedShares(future, future);
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
        AylaCollection createdCollection = createScene(
                "Scene to update schedule",
                devicesToCreate,
                null,
                null);
        assertNotNull(createdCollection);
        assertNotNull(createdCollection.collectionUuid);
        AylaShare share = new AylaShare(TEST_EMAIL_ID, "write",
                createdCollection.collectionUuid, "SCENE",
                null, null, null);
        AylaShare shareAdded = null;
        RequestFuture<AylaShare> future = RequestFuture.newFuture();
        _testSceneManager.shareScene(share,future,future);
        try {
            shareAdded = future.get();
            assertNotNull(shareAdded);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        AylaShare shareToUpdate = new AylaShare(TEST_EMAIL_ID, "read",
                null, null,
                null, null, null);
        shareToUpdate.setRoleName("user");
        shareToUpdate.setStartDateAt("2021-02-24");

        RequestFuture<AylaShare> futureUpdate = RequestFuture.newFuture();
        AylaShare shareUpdate = null;
        _testSceneManager.updateShare(shareAdded.getId(), shareToUpdate, futureUpdate, futureUpdate);
        try {
            shareUpdate = futureUpdate.get();
            assertEquals(shareUpdate.getOperation(), shareToUpdate.getOperation());
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDeleteShare() {
        String name = "An empty scene";
        AylaCollection collection = createEmptyScene(name);
        assertNotNull(collection);
        assertNotNull(collection.collectionUuid);
        assertEquals(collection.name, name);

        AylaShare share = new AylaShare(TEST_EMAIL_ID, "write",
                collection.collectionUuid, "SCENE",
                null, null, null);
        RequestFuture<AylaShare> future = RequestFuture.newFuture();
        _testSceneManager.shareScene(share, future, future);
        RequestFuture<AylaAPIRequest.EmptyResponse> futureDelete = RequestFuture.newFuture();
        try {
            AylaShare shareAdded = future.get();
            assertNotNull(shareAdded.getId());
            _testSceneManager.deleteShare(shareAdded.getId(), futureDelete, futureDelete);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testRemoveDevicesFromScene() {
        AylaCollection parentScene = new AylaCollection();
        parentScene.name = "A scene with ayla device";
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
            device.dsn = TEST_DEVICE_DSN1;
            devicesToCreate = new AylaCollectionDevice[]{device};
        }
        parentScene.devices = devicesToCreate;

        RequestFuture<AylaCollection> future = RequestFuture.newFuture();

        _testSceneManager.createScene(
                parentScene.name,
                devicesToCreate,
                null,
                null,
                future, future);

        AylaCollection createdParentScene = null;
        try {
            createdParentScene = future.get();
            assertNotNull(createdParentScene);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        RequestFuture<AylaCollection> futureChild = RequestFuture.newFuture();
        _testGroupManager.createGroup(
                "child collection group",
                futureChild, futureChild);
        AylaCollection childCollection = new AylaCollection();
        try {
            childCollection = futureChild.get();
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }

        AylaChildCollection aylaChildCollection = new AylaChildCollection();
        aylaChildCollection.collectionUuid = childCollection.collectionUuid;

        AylaCollectionDevice childDevice = new AylaCollectionDevice();
        childDevice.dsn = TEST_DEVICE_DSN1;
        AylaCollectionDevice[] childDevices = new AylaCollectionDevice[]{childDevice};
        aylaChildCollection.devices = childDevices;

        AylaChildCollection[] childCollectionsToAdd = new AylaChildCollection[]{aylaChildCollection};
        RequestFuture<AylaCollection>  futureAdd = RequestFuture.newFuture();
        _testSceneManager.addResourcesToScene(
                createdParentScene.collectionUuid,
                null,
                childCollectionsToAdd,
                futureAdd, futureAdd);

        AylaCollectionDevice deviceRemove = new AylaCollectionDevice();
        deviceRemove.dsn = TEST_DEVICE_DSN1;
        AylaCollectionDevice[] devicesRemove = new AylaCollectionDevice[]{deviceRemove};
        RequestFuture<AylaCollection> futureRemove = RequestFuture.newFuture();
        _testSceneManager.removeDevicesFromScene(createdParentScene.collectionUuid,
                devicesRemove, true, futureRemove, futureRemove);

        AylaCollection sceneRemove = null;
        try {
            sceneRemove = futureRemove.get();
            assertEquals(0, sceneRemove.devices.length);
        } catch (InterruptedException | ExecutionException e) {
            fail(e.getMessage());
        }
    }
    ///////////////////////////////////////////////////////////////////////////////
    private AylaSessionManager getSessionManager() {
        return AylaNetworks.sharedInstance().getSessionManager(
                TestConstants.TEST_SESSION_NAME);
    }

    private AylaCollection createEmptyScene(String name) {
        return createScene(name, null, null, null);
    }

    private AylaCollection createScene(
            String name,
            AylaCollectionDevice[] devices,
            AylaChildCollection[] childCollections,
            Map<String, String> attributes) {
        RequestFuture<AylaCollection> future = RequestFuture.newFuture();
        _testSceneManager.createScene(name, devices,
                childCollections, attributes, future, future);
        try {
            AylaCollection createdScene = future.get();
            assertNotNull(createdScene);
            _recyclableScenes.add(createdScene);
            return createdScene;
        } catch (InterruptedException | ExecutionException e) {
            fail("failed to create scene " + name + " due to: " + e);
            return null;
        }
    }
}
