package com.aylanetworks.aylasdk.gss.model;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import androidx.annotation.NonNull;

import com.aylanetworks.aylasdk.AylaDevice;
import com.google.gson.annotations.Expose;

public class AylaCollectionDevice {

    @Expose public String dsn;
    @Expose public String connected_at;
    @Expose public String connectionStatus;
    @Expose public Boolean lanEnabled;
    @Expose public String oemModel;
    @Expose public String productName;
    @Expose public AylaCollectionProperty[] properties;
    @Expose public AylaCollectionProperty[] states;

    /**
     * Intializes the AylaCollectionDevice with the given AylaDevice
     * @param device a device of type 'AylaDevice'
     * @param propertiesName list of properties need to be added to collection. For Groups type pass null.
     * @return AylaCollectionProperty
     */
    public AylaCollectionDevice updateFrom(@NonNull AylaDevice device,
                                           @NonNull String[] propertiesName) {
        dsn = device.getDsn();
        if (propertiesName != null && propertiesName.length > 0) {
            AylaCollectionProperty[] propertiesArray = new AylaCollectionProperty[propertiesName.length];
            for (int i = 0; i < propertiesName.length; i++) {
                AylaCollectionProperty collectionProperty = new AylaCollectionProperty();
                collectionProperty.propertyName = propertiesName[i];
                collectionProperty.propertyValue = String.valueOf(device.getProperty(propertiesName[i]).getValue());
                propertiesArray[i] = collectionProperty;
            }
            states = propertiesArray;
        }
        return this;
    }
}
