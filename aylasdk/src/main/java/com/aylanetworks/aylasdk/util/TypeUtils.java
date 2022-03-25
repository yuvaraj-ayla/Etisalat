package com.aylanetworks.aylasdk.util;

/**
 * Android_Aura
 * <p/>
 * Copyright 2016 Ayla Networks, all rights reserved
 */

import androidx.annotation.NonNull;

import com.aylanetworks.aylasdk.AylaProperty;

import static com.aylanetworks.aylasdk.AylaProperty.BASE_TYPE_BOOLEAN;
import static com.aylanetworks.aylasdk.AylaProperty.BASE_TYPE_DECIMAL;
import static com.aylanetworks.aylasdk.AylaProperty.BASE_TYPE_INTEGER;
import static com.aylanetworks.aylasdk.AylaProperty.BASE_TYPE_STRING;

/**
 * Class used for data type conversions.
 */
public class TypeUtils {

    /**
     * Converts type of datapoint value according to the property's base_type
     * @param baseType Property base type
     * @param value Value of the datapoint
     * @return Java Object type representing a datapoint value parsed to the right type. null
     * value will be returned if the value passed to this method cannot be parsed to the basetype.
     */
    public static Object getTypeConvertedValue(String baseType, String value){

        switch(baseType){
            case "boolean":
            case "integer":
                try{
                    return Integer.decode(value);
                } catch(NumberFormatException exception){
                    return null;
                }

            case "decimal":
                try{
                    return Float.valueOf(value);
                } catch(NumberFormatException exception){
                    return null;
                }
            default:
                return value;
        }

    }

    /**
     * Converts a property value to its appropriate string form so that it
     * can be used in, for example, a rule expression.
     *
     * @param property the owner property the value to be set for or check against.
     * @param value the value to be converted.
     * @param quotes should quote the value for string base type values.
     * @param single true to use single quotes, and false to use double quotes.
     *               Meaningful only when the <code>quotes</code> parameter is true.
     *
     * @return Returns the converted string.
     */
    public static String convertTypedValue(@NonNull AylaProperty property,
                                           @NonNull Object value,
                                           boolean quotes,
                                           boolean single) {
        String baseType = property.getBaseType();
        String convertedText = String.valueOf(value);

        switch (baseType) {
            case BASE_TYPE_INTEGER:
                // even though the basetype is integer the value being passed might be double.
                if (value instanceof Double) {
                    convertedText = String.valueOf(((Double) value).intValue());
                }
                break;

            case BASE_TYPE_BOOLEAN:
                if (value instanceof Double) {
                    convertedText = (((Double) value).intValue() == 0) ? "false" : "true";
                } else if (value instanceof Integer) {
                    convertedText = (((Integer) value) == 0) ? "false" : "true";
                }
                break;

            default:
        }

        if (quotes) {
            return single ? ObjectUtils.singleQuote(convertedText) : ObjectUtils.quote(convertedText);
        } else {
            return convertedText;
        }
    }
}
