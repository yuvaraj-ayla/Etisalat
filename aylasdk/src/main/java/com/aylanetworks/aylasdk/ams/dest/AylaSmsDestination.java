package com.aylanetworks.aylasdk.ams.dest;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class AylaSmsDestination extends AylaDestination {

    @Expose String countryCode;

    public static final String SMS_SERVICE_PROVIDER_TWILIO  = "twilio";
    public static final String SMS_SERVICE_PROVIDER_YUNPIAN = "yunpian";

    public static final String COUNTRY_CODE_CN = "86";

    /**
     * Constructs an empty SMS destination that is can be used
     * to update an existing destination.
     */
    public AylaSmsDestination() {
    }

    public AylaSmsDestination(
            @NonNull String phoneNumber,
            @NonNull String countryCode,
            @NonNull String provider,
            @NonNull String subject,
            @NonNull String body) {
        setType(AylaDestinationTypes.SMS);
        setProvider(provider);
        setDeliverTo(phoneNumber);
        setCountryCode(countryCode);
        setTitle(subject);
        setBody(body);
    }

    public AylaSmsDestination(
            @NonNull String phoneNumber,
            @NonNull String countryCode,
            @NonNull String subject,
            @NonNull String body) {
        this(phoneNumber, countryCode, null, subject, body);
        String smsProvider = countryCode.contains(COUNTRY_CODE_CN)
                ? SMS_SERVICE_PROVIDER_YUNPIAN
                : SMS_SERVICE_PROVIDER_TWILIO;
        setProvider(smsProvider);
    }

    public String getCountryCode() {
        return countryCode;
    }

    public AylaSmsDestination setCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public String getPhoneNumber() {
        return getDeliverTo();
    }

    public AylaSmsDestination setPhoneNumber(String phoneNumber) {
        setDeliverTo(phoneNumber);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AylaSmsDestination that = (AylaSmsDestination) o;

        return Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        return result;
    }
}
