package com.aylanetworks.aylasdk.rules;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.aylanetworks.aylasdk.AylaDevice;
import com.aylanetworks.aylasdk.AylaUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * <code>AylaLocationRuleExpression</code> is used to monitor location change
 * event, will be evaluated either device location or user's phone location
 * gets changed.
 */
public class AylaLocationRuleExpression implements AylaRuleExpression {

    final private AylaDevice _device;
    final private AylaUser _user;

    /**
     * Constructs new location rule based on device location.
     * @param device the device to be monitored.
     */
    public AylaLocationRuleExpression(@NonNull AylaDevice device) {
        _device = device;
        _user = null;
    }

    /**
     * Constructs new location rule based on phone location.
     * @param user the user who signed in the phone to be monitored.
     */
    public AylaLocationRuleExpression(@NonNull AylaUser user) {
        _device = null;
        _user = user;
    }

    @Override
    public String key() {
        return "LOCATION";
    }

    @Override
    public String create() {
        String dsn = _device.getDsn();
        if (_device != null) {
            return String.format(Locale.US, "LOCATION(%s)", dsn);
        } else {
            return String.format(Locale.US, "LOCATION(%s)", _user.getUuid());
        }
    }
}
