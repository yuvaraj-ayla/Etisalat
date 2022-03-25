package com.aylanetworks.aylasdk.rules;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.aylanetworks.aylasdk.AylaDevice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * <code>AylaConnectionRuleExpression</code> is used to describe device connection
 * status change event, examples:
 * <ul>
 *     <li>CONNECTION(dsn, online)</li>
 *     <li>CONNECTION(dsn, offline)</li>
 *     <li>CONNECTION(dsn, all)</li>
 * </ul>
 */
public class AylaConnectionRuleExpression implements AylaRuleExpression {

    public static class ConnectionStatus {
        @StringDef({ONLINE, OFFLINE, ALL})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {}

        public static final String ONLINE  = "online";
        public static final String OFFLINE = "offline";
        public static final String ALL     = "all";
    }

    final private AylaDevice _device;
    final private String _connectionStatus;

    public AylaConnectionRuleExpression(
            @NonNull AylaDevice device,
            @NonNull @ConnectionStatus.AllowedType String status) {
        _device = device;
        _connectionStatus = status;
    }

    @Override
    public String key() {
        return "CONNECTION";
    }

    @Override
    public String create() {
        String dsn = _device.getDsn();
        String status = _connectionStatus;
        return String.format(Locale.US, "CONNECTION(%s, %s)", dsn, status);
    }
}
