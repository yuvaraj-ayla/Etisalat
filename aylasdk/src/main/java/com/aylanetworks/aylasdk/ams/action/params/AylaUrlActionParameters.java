package com.aylanetworks.aylasdk.ams.action.params;

import com.google.gson.annotations.Expose;

/**
 * Action parameters corresponds to
 * {@link com.aylanetworks.aylasdk.AylaAction.ActionType#URL}.
 */
public class AylaUrlActionParameters extends AylaActionParameters {

    @Expose public String scheme;
    @Expose public String username;
    @Expose public String password;
    @Expose public String endpoint;
    @Expose public String body;

}
