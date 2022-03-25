package com.aylanetworks.aylasdk.setup;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

/**
 * Android_Aura
 * <p>
 * Copyright 2017 Ayla Networks, all rights reserved
 */

public class AylaRegInfo {
    @Expose
    private String regtoken;
    @Expose
    private int registered;
    @Expose
    private String registrationType;
    @Expose
    private String hostSymname;

    public String getRegtoken() {
        return regtoken;
    }

    public boolean isRegistered() {
        return registered==1;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    @NonNull
    @Override
    public String toString() {
        return new StringBuilder()
                .append("registrationType:").append(registrationType).append(",")
                .append("regtoken:").append(registered).append(",")
                .append("registered:").append(isRegistered()).append(",")
                .append("hostSymname:").append(hostSymname).toString();
    }
}
