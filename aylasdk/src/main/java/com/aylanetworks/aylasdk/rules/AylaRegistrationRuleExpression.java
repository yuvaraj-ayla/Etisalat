package com.aylanetworks.aylasdk.rules;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.aylanetworks.aylasdk.AylaDevice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * <code>AylaRegistrationRuleExpression</code> can be used to monitor device
 * registration event, and will be evaluated if the target device gets registered,
 * unregistered, or both.
 */
public class AylaRegistrationRuleExpression implements AylaRuleExpression {

    public static class RegistrationEvent {
        @StringDef({REGISTERED, UNREGISTERED, ALL})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {}

        public static final String REGISTERED   = "registered";
        public static final String UNREGISTERED = "unregistered";
        public static final String ALL          = "all";
    }

    final private AylaDevice _device;
    final private String _event;

    public AylaRegistrationRuleExpression(
            @NonNull AylaDevice device,
            @NonNull @RegistrationEvent.AllowedType String event) {
        _device = device;
        _event = event;
    }

    @Override
    public String key() {
        return "REGISTRATION";
    }

    @Override
    public String create() {
        String dsn = _device.getDsn();
        switch (_event) {
            case RegistrationEvent.REGISTERED:
                return String.format(Locale.US, "REGISTRATION(%s, true)", dsn);

            case RegistrationEvent.UNREGISTERED:
                return String.format(Locale.US, "REGISTRATION(%s, false)", dsn);

            default:
                return String.format(Locale.US, "REGISTRATION(%s, all)", dsn);
        }
    }
}
