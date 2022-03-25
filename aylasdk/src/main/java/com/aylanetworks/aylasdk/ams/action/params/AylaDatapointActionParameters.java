package com.aylanetworks.aylasdk.ams.action.params;

import androidx.annotation.NonNull;

import com.aylanetworks.aylasdk.AylaProperty;
import com.aylanetworks.aylasdk.util.TypeUtils;
import com.google.gson.annotations.Expose;

/**
 * Action parameters required for {@link com.aylanetworks.aylasdk.AylaAction.ActionType#DATAPOINT}.
 * <code>AylaDatapointActionParameters</code> is used to create a new datapoint on a property
 * when the associated rule gets triggered.
 */
public class AylaDatapointActionParameters extends AylaActionParameters {

    @Expose private String datapoint;

    /**
     * Constructs new datapoint action parameter with the datapoint expression
     * which specifies the target device, target property name and the new value
     * to be set, in a format like "DATAPOINT(dsn1,prop1) = 70".
     */
    public AylaDatapointActionParameters(@NonNull String datapoint) {
        setDatapoint(datapoint);
    }

    /**
     * Gets the datapoint expression that pertains to this action parameters.
     */
    public String getDatapoint() {
        return datapoint;
    }

    /**
     * Sets the datapoint expression which specifies the target device, target property
     * name and the new value to be set, in a format like "DATAPOINT(dsn1,prop1) = 70".
     */
    public void setDatapoint(@NonNull String datapoint) {
        this.datapoint = datapoint;
    }

    /**
     * Creates action parameters for new property value.
     * @param property the property the value will be set for.
     * @param value the property value to be set to.
     */
    public static AylaDatapointActionParameters createPropertyValueParameters(
            @NonNull AylaProperty property, @NonNull Object value) {
        boolean quotes = AylaProperty.BASE_TYPE_STRING.equals(property.getBaseType());
        String convertedValue = TypeUtils.convertTypedValue(property, value, quotes, true);
        String dsn = property.getOwner().getDsn();
        String propertyName = property.getName();
        String datapoint = String.format("DATAPOINT(%s, %s) = %s", dsn, propertyName, convertedValue);
        return new AylaDatapointActionParameters(datapoint);
    }
}
