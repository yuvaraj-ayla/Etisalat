package com.aylanetworks.aylasdk.gss.model;

/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import androidx.annotation.NonNull;
import com.aylanetworks.aylasdk.AylaDevice;
import com.google.gson.annotations.Expose;

public class AylaCollectionProperty<T> {

    @Expose public String propertyName;
    @Expose public T propertyValue;

}
