/*
 * AylaSDK
 *
 * Copyright 2019 Ayla Networks, all rights reserved
 */
package com.aylanetworks.aylasdk;

import androidx.annotation.IntDef;
import androidx.annotation.StringDef;

import com.aylanetworks.aylasdk.AylaDeviceConnectionInfo.ConnectionType.AllowedType;
import com.google.gson.annotations.Expose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Representation of the connection info of an Ayla device, a cellular device in particular.
 * Used to hold the connection information of a device with a specific device DSN.
 */
public class AylaDeviceConnectionInfo {

    @Expose private ConnectionInfo connectionInfo;

    private class ConnectionInfo {
        @Expose private String connectivityType;
        @Expose private String connectivityTechnology;
        @Expose private String networkOperator;
        @Expose private String networkName;
        @Expose private String equipmentId;
        @Expose private String subscriptionId;
        @Expose private String baseStation;
        @Expose private String lastCellConnectionAt;
        @Expose private int rssi;
    }

    public static class ConnectionType {
        @StringDef({LPWAN, WIFI})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {}

        public static final String LPWAN = "LPWAN";
        public static final String WIFI = "Wifi";
    }

    public static class RssiLevel {
        @IntDef({UNKNOWN, POOR, WEAK, FAIR, GOOD, VERY_GOOD, EXCELLENT})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedLevel {}

        public static final int UNKNOWN   = -1;
        public static final int POOR      = 0;
        public static final int WEAK      = 1;
        public static final int FAIR      = 2;
        public static final int GOOD      = 3;
        public static final int VERY_GOOD = 4;
        public static final int EXCELLENT = 5;

        @AllowedLevel
        public static int fromValue(int rssi) {
            if (rssi >= 0) {
                return UNKNOWN;
            } else if (rssi <= -200) {
                return POOR;
            } else if (rssi <= -70 && rssi > -199) {
                return WEAK;
            } else if (rssi <= -60 && rssi >= -69) {
                return FAIR;
            } else if (rssi <= -50 && rssi >= -59) {
                return GOOD;
            } else if (rssi <= -40 && rssi >= -49) {
                return VERY_GOOD;
            } else {
                return EXCELLENT;
            }
        }
    }

    /**
     * Get the type of connectivity the device used to connect to the platform,
     * e.g. “Wifi”, “LPWAN”, etc, or null if otherwise not available.
     *
     * @see AllowedType
     */
     @AllowedType
     public String getConnectivityType() {
        return connectionInfo != null ? connectionInfo.connectivityType : null;
    }

    /**
     * Get the device connectivity technology, e.g. “802.11ac”, “802.11n”, “Cat-M1”, “3G”, etc.
     * Could be a list of technology specific tokens which fully describe a combined technologies, or
     * null if otherwise not available.
     */
    public String getConnectivityTechnology() {
        return connectionInfo != null ? connectionInfo.connectivityTechnology : null;
    }

    /**
     * Get the network operator, identified as a combination of MCC (mobile country code)
     * and MNC (mobile network code), e.g. “466,92”, or null if otherwise not available.
     */
    public String getNetworkOperator() {
        return connectionInfo != null ? connectionInfo.networkOperator : null;
    }

    /**
     * Get network name, can be the SSID name or APN name, e.g. “ayla-guest”,
     * or null if otherwise not available.
     */
    public String getNetworkName() {
        return connectionInfo != null ? connectionInfo.networkName : null;
    }

    /**
     * Get the equipment Id, can be the device IMEI (international mobile equipment identity),
     * or MAC address, e.g. “351756051523999”, or null if otherwise not available.
     */
    public String getEquipmentId() {
        return connectionInfo != null ? connectionInfo.equipmentId : null;
    }

    /**
     * Get the device subscription Id, can be the IMSI (international mobile subscriber identity),
     * or WiFi user id. e.g: “310150123456789”, or null if otherwise not available.
     */
    public String getSubscriptionId() {
        return connectionInfo != null ? connectionInfo.subscriptionId : null;
    }

    /**
     * Get the base station, can be BSSID(or Cell Tower ID), e.g. “00:19:3b:99:e2:80”,
     * or null if otherwise not available.
     */
    public String getBaseStation() {
        return connectionInfo != null ? connectionInfo.baseStation : null;
    }

    /**
     * Get the RSSI value (signal strength, unit: dB). e.g.: -50.
     */
    public int getRssi() {
        return connectionInfo != null ? connectionInfo.rssi : 0;
    }

    /**
     * Get the RSSI level based on the RSSI value returned from {@link #getRssi()}.
     */
    @RssiLevel.AllowedLevel
    public int getRssiLevel() {
        return RssiLevel.fromValue(getRssi());
    }

    /**
     * Get the time of last cell connection, in number of seconds since epoch,
     * or null if otherwise not available. e.g. "2012-07-14T16:23:28Z".
     */
    public String getLastCellConnectionAt() {
        return connectionInfo != null ? connectionInfo.lastCellConnectionAt : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("connect type:").append(getConnectivityType()).append("\n")
          .append("technologies:").append(getConnectivityTechnology()).append("\n")
          .append("network operator:").append(getNetworkOperator()).append("\n")
          .append("network name:").append(getNetworkName()).append("\n")
          .append("equipmentId:").append(getEquipmentId()).append("\n")
          .append("subscriptionId:").append(getSubscriptionId()).append("\n")
          .append("baseStation:").append(getBaseStation()).append("\n")
          .append("rssi:").append(getRssi()).append("\n")
          .append("lastCellConnectionAt:").append(getLastCellConnectionAt());
        return sb.toString();
    }
}
