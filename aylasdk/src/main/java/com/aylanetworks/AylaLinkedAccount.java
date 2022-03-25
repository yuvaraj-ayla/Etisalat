package com.aylanetworks;

import com.aylanetworks.aylasdk.AylaNetworks;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

/**
 * Describes an account linked to the current user. One of email, phone, or username should
 * be provided.
 */
public class AylaLinkedAccount {
    public AylaLinkedAccount() {

    }

    public AylaLinkedAccount(String email, String username, String phone, String oemName,
                             String oemString) {
       this.email = email;
       this.username = username;
       this.phone = phone;
       this.oemName = oemName;
       this.oemString = oemString;
    }

    @Expose public String email;
    @Expose public String username;
    @Expose public String phone;
    @Expose public String oemName;
    @Expose public String oemString;
    @Expose public String password;

    public JsonObject toJsonObject() {
        Gson gson = AylaNetworks.sharedInstance().getGson();
        JsonElement account = gson.toJsonTree(this);
        JsonObject json = account.getAsJsonObject();

        if (oemString != null) {
            json.addProperty("origin_oem_str", oemString);
        }

        if (email != null) {
            json.addProperty("user_email", email);
        }

        return json;
    }
}
