package com.aylanetworks.aylasdk.ams.dest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class AylaEmailDestination extends AylaDestination {

    @Expose String userMessage;
    @Expose String userName;

    public static final String EMAIL_PROVIDER_SMTP = "smtp";

    /**
     * Constructs an empty email destination that is can be used
     * to update an existing destination.
     */
    public AylaEmailDestination() {
    }

    public AylaEmailDestination(
            @NonNull String emailAddress,
            @NonNull String title,
            @NonNull String body) {
        this(emailAddress, title, body, null, null);
    }

    public AylaEmailDestination(
            @NonNull String emailAddress,
            @NonNull String title,
            @NonNull String body,
            @Nullable String userName,
            @Nullable String userMessage) {
        this(emailAddress, title, body, null, EMAIL_PROVIDER_SMTP, userName, userMessage);
    }

    public AylaEmailDestination(
            @NonNull String emailAddress,
            @NonNull String title,
            @NonNull String body,
            @Nullable String templateId,
            @Nullable String provider,
            @Nullable String userName,
            @Nullable String userMessage) {
        setType(AylaDestinationTypes.EMAIL);
        setTitle(title);
        setBody(body);
        setDeliverTo(emailAddress);
        setMessageTemplateId(templateId);
        setProvider(provider);
        setUserName(userName);
        setUserMessage(userMessage);
    }

    public String getEmailAddress() {
        return getDeliverTo();
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getUserName() {
        return userMessage;
    }

    public AylaEmailDestination setUserMessage(String userMessage) {
        this.userMessage = userMessage;
        return this;
    }

    public AylaEmailDestination setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AylaEmailDestination that = (AylaEmailDestination) o;

        if (!Objects.equals(userMessage, that.userMessage))
            return false;
        return Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (userMessage != null ? userMessage.hashCode() : 0);
        result = 31 * result + (userName != null ? userName.hashCode() : 0);
        return result;
    }
}
