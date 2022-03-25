package com.aylanetworks.aylasdk.ams.dest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import com.aylanetworks.aylasdk.AylaNetworks;
import com.google.gson.annotations.Expose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class AylaPushDestination extends AylaDestination {

    @Expose String appId;
    @Expose String sound;

    public static class PushProvider {
        @StringDef({FCM, APNS, BAIDU})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {}

        public static final String FCM   = "fcm";
        public static final String APNS  = "apns";
        public static final String BAIDU = "baidu";
    }

    /**
     * Constructs an empty push destination that is can be used
     * to update an existing destination.
     */
    public AylaPushDestination() {
    }

    /**
     * Constructs a new push destination with provided info.
     * @param provider To determine way to send notification.
     * @param deviceToken device token on which notification will be send.
     * @param title notification message title.
     * @param message notification message.
     * @param sound sound which will be played along with notification on device.
     */
    public AylaPushDestination(
            @NonNull @PushProvider.AllowedType String provider,
            @NonNull String deviceToken,
            @NonNull String title,
            @NonNull String message,
            @Nullable String sound) {
        this.type = AylaDestinationTypes.PUSH;
        this.provider = provider;
        this.deliverTo = deviceToken;
        this.title = title;
        this.body = message;
        this.sound = sound;
        this.appId = AylaNetworks.sharedInstance().getSystemSettings().appId;
    }

    /**
     * Constructs a new push destination with provided info.
     * @param title notification message title.
     * @param message notification message.
     * @param provider To determine way to send notification.
     * @param deviceToken device token on which notification will be send.
     * @param metaData additional information about notification.
     * @param sound sound which will be played along with notification on device.
     */
    public AylaPushDestination(
            @NonNull String title,
            @NonNull String message,
            @NonNull @PushProvider.AllowedType String provider,
            @NonNull String deviceToken,
            @Nullable String metaData,
            @Nullable String sound) {
        this.type = AylaDestinationTypes.PUSH;
        this.title = title;
        this.body = message;
        this.provider = provider;
        this.deliverTo = deviceToken;
        this.metaData = metaData;
        this.sound = sound;
        this.appId = AylaNetworks.sharedInstance().getSystemSettings().appId;
    }

    public String getAppId() {
        return appId;
    }

    public AylaPushDestination setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getSound() {
        return sound;
    }

    public AylaPushDestination setSound(String sound) {
        this.sound = sound;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AylaPushDestination that = (AylaPushDestination) o;

        if (!Objects.equals(appId, that.appId)) return false;
        return Objects.equals(sound, that.sound);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (sound != null ? sound.hashCode() : 0);
        return result;
    }
}
