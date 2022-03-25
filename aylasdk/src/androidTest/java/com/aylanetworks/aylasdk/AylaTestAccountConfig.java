package com.aylanetworks.aylasdk;

import com.aylanetworks.aylasdk.auth.AylaAuthorization;

/**
 * Android_AylaSDK
 * <p>
 * Copyright 2016 Ayla Networks, all rights reserved
 */
public class AylaTestAccountConfig {
    private String userEmail;
    private String password;
    private String deviceDSN;
    private String testSessionName;


    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceDSN() {
        return deviceDSN;
    }

    public void setDeviceDSN(String deviceDSN) {
        this.deviceDSN = deviceDSN;
    }

    public String getTestSessionName() {
        return testSessionName;
    }

    public void setTestSessionName(String testSessionName) {
        this.testSessionName = testSessionName;
    }

    public AylaTestAccountConfig(String userEmail, String password, String deviceDSN, String testSessionName) {
        this.userEmail = userEmail;
        this.password = password;
        this.deviceDSN = deviceDSN;
        this.testSessionName = testSessionName;
    }
}
