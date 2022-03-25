package com.aylanetworks.aylasdk.gss.model;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
import com.aylanetworks.aylasdk.AylaDatapoint;
import com.google.gson.annotations.Expose;

public class AylaCollectionTriggerResponse {

    /*
        [{"dsn": "AC000W000000001","name": "Blue_LED","status": 201, "datapoint": {"updated_at": "2021-01-19T05:35:00Z",
        "created_at": "2021-01-19T05:35:00Z","echo": false,"metadata": {},"value": 1}}]
    */

    @Expose public String dsn;
    @Expose public String name;
    @Expose public String status;
    @Expose public AylaDatapoint datapoint;

}
