package com.aylanetworks.aylasdk.ams.action.params;

import com.google.gson.annotations.Expose;

/**
 * Action parameters that are required for
 * {@link com.aylanetworks.aylasdk.AylaAction.ActionType#EMAIL}.
 */
public class AylaEmailActionParameters extends AylaActionParameters {

    @Expose public String[] email_to;
    @Expose public String email_subject;
    @Expose public String email_body;
}
