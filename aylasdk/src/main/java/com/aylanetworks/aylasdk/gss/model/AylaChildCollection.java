package com.aylanetworks.aylasdk.gss.model;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import com.google.gson.annotations.Expose;

public class AylaChildCollection {

    @Expose public String collectionUuid;
    @Expose public String name;
    @Expose public String type;
    @Expose public AylaCollectionDevice[] devices;
    @Expose public AylaChildCollection[] childCollections;
    @Expose public String[] sharedUsers;
    @Expose public String triggerExpression;
    @Expose public AylaCollectionProperty[] states;
    @Expose public Boolean isActive;
}
