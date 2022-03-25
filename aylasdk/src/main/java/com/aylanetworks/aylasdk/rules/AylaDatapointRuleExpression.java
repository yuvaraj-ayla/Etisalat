package com.aylanetworks.aylasdk.rules;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aylanetworks.aylasdk.AylaProperty;
import com.aylanetworks.aylasdk.rules.AylaNumericComparisonExpression.Comparator;
import com.aylanetworks.aylasdk.util.TypeUtils;

import java.util.Locale;

/**
 * <code>AylaDatapointRuleExpression</code> is used to describe datapoint rule
 * expressions that meets certain criteria, examples:
 * <ol>
 *     <li>DATAPOINT(dsn1, property1) == true</li>
 *     <li>DATAPOINT(dsn2, property2) == 70</li>
 *     <li>DATAPOINT(dsn3, prop3) < 60</li>
 *     <li>str_equals(DATAPOINT(dsn4, prop4), 'Hello ARE')</li>
 *     <li>str_contains(DATAPOINT(dsn5, prop5), 'Hello ARE')</li>
 *     <li>changed(DATAPOINT(dsn6, prop6))</li>
 * </ol>
 */
public class AylaDatapointRuleExpression implements AylaRuleExpression {

    final private AylaProperty _property;
    final private String _comparator;
    final private Object _value;

    /**
     * Constructor for new rule expression when the value of the datapoint
     * meets the requirement.
     *
     * @param property the property to check against.
     * @param comparator the comparision operator used to check the current datapoint value
     *                   against the target value.
     * @param value the expected datapoint value of the property.
     */
    public AylaDatapointRuleExpression(
            @NonNull AylaProperty property,
            @NonNull @Comparator.AllowedType String comparator,
            @Nullable Object value) {
        _property = property;
        _comparator = comparator;
        _value = value;
    }

    public AylaProperty getProperty() {
        return _property;
    }

    public String getComparator() {
        return _comparator;
    }

    @Override
    public String key() {
        return "DATAPOINT";
    }

    @Override
    public String create() {
        AylaProperty property = getProperty();
        String dsn = property.getOwner().getDsn();
        String propertyName = property.getName();

        if (Comparator.CH.equals(getComparator())) {
            return String.format(Locale.US, "changed(DATAPOINT(%s, %s))", dsn, propertyName);
        } else if (Comparator.AN.equals(getComparator())) {
            // TODO: keyword 'any' is not supported yet, used 'changed' instead.
            // return String.format(Locale.US, "any(DATAPOINT(%s, %s)", dsn, propertyName);
            return String.format(Locale.US, "changed(DATAPOINT(%s, %s))", dsn, propertyName);
        } else {
            if (AylaProperty.BASE_TYPE_STRING.equals(property.getBaseType())) {
                String convertedValue = TypeUtils.convertTypedValue(property,
                        _value, true, true);
                if (Comparator.CT.equals(getComparator())) {
                    return String.format(Locale.US, "str_contains(DATAPOINT(%s, %s), %s)",
                            dsn, propertyName, convertedValue);
                } else if (Comparator.EQ.equals(getComparator())) {
                    return String.format(Locale.US, "str_equals(DATAPOINT(%s, %s), %s)",
                            dsn, propertyName, convertedValue);
                }
            }

            return String.format(Locale.US, "DATAPOINT(%s, %s) %s %s",
                    dsn, propertyName, getComparator(), _value);
        }
    }
}
