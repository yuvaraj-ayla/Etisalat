package com.aylanetworks.aylasdk.ams.dest;

import android.text.TextUtils;

import androidx.annotation.StringDef;

import com.google.gson.annotations.Expose;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public abstract class AylaDestination {

    /**
     * This is created by cloud when a new Action is created, later used to
     * update action & delete action using PUT and DELETE API's.
     * */
    @Expose String uuid;

    /**
     * Notification provider Ex. for email smtp, for push apns or fcm & for sms twilio
     */
    @Expose String provider;

    /**
     *  Notification types Ex. email, sms & push
     */
    @Expose String type;

    /**
     *  Logged user device token deliver a notification in case of PUSH,
     *  email address in case of EMAIL and phone number in case of SMS.
     */
    @Expose String deliverTo;

    /**
     * Message template ID AMS uses to send messages.
     */
    @Expose String messageTemplateId;

    @Expose String title;
    @Expose String body;
    @Expose String createdBy;
    @Expose String createdAt;
    @Expose String updatedAt;
    @Expose String metaData;
    @Expose String oemId;

    public static final class Wrapper {
        @Expose public AylaDestination destination;
    }

    public static class AylaDestinationTypes {
        @StringDef({SMS, EMAIL, PUSH})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {}

        public static final String SMS   = "sms";
        public static final String PUSH  = "push";
        public static final String EMAIL = "email";
    }

    public static final class DestinationsWrapper {
        @Expose public AylaDestination[] destinations;
    }

    public String getUUID() {
        return uuid;
    }

    public String getProvider() {
        return provider;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getDeliverTo() {
        return deliverTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getMetaData() {
        return metaData;
    }

    public String getOemId() {
        return oemId;
    }

    public String getMessageTemplateId() {
        return messageTemplateId;
    }

    public AylaDestination setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    protected void setType(@AylaDestinationTypes.AllowedType String type) {
        this.type = type;
    }

    protected AylaDestination setDeliverTo(String deliverTo) {
        this.deliverTo = deliverTo;
        return this;
    }

    public AylaDestination setMessageTemplateId(String messageTemplateId) {
        this.messageTemplateId = messageTemplateId;
        return this;
    }

    public AylaDestination setTitle(String title) {
        this.title = title;
        return this;
    }

    public AylaDestination setBody(String body) {
        this.body = body;
        return this;
    }

    public AylaDestination setMetaData(String metaData) {
        this.metaData = metaData;
        return this;
    }

    public void updateUuidFrom(AylaDestination other) {
        this.uuid = other.uuid;
    }

    public void updateFrom(AylaDestination other) {
        this.uuid = other.uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AylaDestination that = (AylaDestination) o;
        return TextUtils.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, type, deliverTo);
    }
}
