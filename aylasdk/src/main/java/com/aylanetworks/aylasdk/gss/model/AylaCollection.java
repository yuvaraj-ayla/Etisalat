package com.aylanetworks.aylasdk.gss.model;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import com.aylanetworks.aylasdk.AylaGrant;
import com.aylanetworks.aylasdk.AylaSchedule;
import com.aylanetworks.aylasdk.AylaShare;
import com.google.gson.annotations.Expose;

import java.util.Map;

public class AylaCollection {

//collection: {}
//    collection_uuid: String
//    name: String
//    type: String
//    devices: [{}]
//        dsn: String
//        states: []
//            property_name: String
//            property_value: Int
//    child_collections: [{}]
//        collection_uuid: String
//        devices: [{}]
//            dsn: String
//            states: []
//                property_name: String
//                property_value: Int
//    custom_attributes: [{}]
//        key: String
//        Value: String
//    shared_users: []
//    trigger_expression: String

    @Expose public String collectionUuid;
    @Expose public String name;
    @Expose public String type;
    @Expose public AylaCollectionDevice[] devices;
    @Expose public AylaChildCollection[] childCollections;
    @Expose public Map<String, String> custom_attributes;
    @Expose public String[] sharedUsers;
    @Expose public String triggerExpression;
    @Expose public Boolean isActive;
    @Expose public AylaCollectionProperty[] states;
    @Expose public AylaSchedule schedule;
    @Expose public AylaShare share;
    @Expose public AylaGrant grant;

    public static class CollectionsWrapper {
        @Expose public AylaCollection[] collections;
    }

    public static class CollectionWrapper {
        @Expose public AylaCollection collection;
    }

    public static class CollectionShareWrapper {
        @Expose public AylaShare share;
    }
}
