package com.aylanetworks.aylasdk.util;

/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import com.aylanetworks.aylasdk.AylaLog;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This is an utility class for AylaTypeAdapterFactory class and is used by that class when the raw
 * type is Date.
 * The Date Type Adapter is needed to fix deserialization bug of date in the gson
 * parser after switching mobile phone time preferences from 24 hour to 12 hour,
 * Refer to known issue https://github.com/google/gson/issues/935
 */
public final class UtcDateTypeAdapterUtil {
    public final static TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
    private static final String LOG_TAG = "UtcDateTypeAdapterUtil";

    private static final String GMT_ID = "GMT";

    /**
     * Format date into yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     *
     * @param date   the date to format
     * @param millis true to include millis precision otherwise false
     * @param tz     timezone to use for the formatting (GMT will produce 'Z')
     * @return the date formatted as yyyy-MM-ddThh:mm:ss[.sss][Z|[+-]hh:mm]
     */
    public static String format(Date date, boolean millis, TimeZone tz) {
        Calendar calendar = new GregorianCalendar(tz, Locale.US);
        calendar.setTime(date);

        // estimate capacity of buffer as close as we can (yeah, that's pedantic ;)
        int capacity = "yyyy-MM-ddThh:mm:ss".length();
        capacity += millis ? ".sss".length() : 0;
        capacity += tz.getRawOffset() == 0 ? "Z".length() : "+hh:mm".length();
        StringBuilder formatted = new StringBuilder(capacity);

        padInt(formatted, calendar.get(Calendar.YEAR), "yyyy".length());
        formatted.append('-');
        padInt(formatted, calendar.get(Calendar.MONTH) + 1, "MM".length());
        formatted.append('-');
        padInt(formatted, calendar.get(Calendar.DAY_OF_MONTH), "dd".length());
        formatted.append('T');
        padInt(formatted, calendar.get(Calendar.HOUR_OF_DAY), "hh".length());
        formatted.append(':');
        padInt(formatted, calendar.get(Calendar.MINUTE), "mm".length());
        formatted.append(':');
        padInt(formatted, calendar.get(Calendar.SECOND), "ss".length());
        if (millis) {
            formatted.append('.');
            padInt(formatted, calendar.get(Calendar.MILLISECOND), "sss".length());
        }

        int offset = tz.getOffset(calendar.getTimeInMillis());
        if (offset != 0) {
            int hours = Math.abs((offset / (60 * 1000)) / 60);
            int minutes = Math.abs((offset / (60 * 1000)) % 60);
            formatted.append(offset < 0 ? '-' : '+');
            padInt(formatted, hours, "hh".length());
            formatted.append(':');
            padInt(formatted, minutes, "mm".length());
        } else {
            formatted.append('Z');
        }

        return formatted.toString();
    }

    /**
     * Zero pad a number to a specified length
     *
     * @param buffer buffer to use for padding
     * @param value  the integer value to pad if necessary.
     * @param length the length of the string we should zero pad
     */
    private static void padInt(StringBuilder buffer, int value, int length) {
        String strValue = Integer.toString(value);
        for (int i = length - strValue.length(); i > 0; i--) {
            buffer.append('0');
        }
        buffer.append(strValue);
    }

    /**
     * Parse a date from ISO-8601 formatted string. It expects a format
     * [yyyy-MM-dd|yyyyMMdd][T(hh:mm[:ss[.sss]]|hhmm[ss[.sss]])]?[Z|[+-]hh:mm]]
     *
     * @param date ISO string to parse in the appropriate format.
     * @param pos  The position to start parsing from, updated to where parsing stopped.
     * @return the parsed date
     * @throws ParseException if the date is not in the appropriate format
     */
    public static Date parse(String date, ParsePosition pos) throws ParseException {

        Exception fail;
        try {
            int offset = pos.getIndex();

            // extract year
            int year = parseInt(date, offset, offset += 4);
            if (checkOffset(date, offset, '-')) {
                offset += 1;
            }

            // extract month
            int month = parseInt(date, offset, offset += 2);
            if (checkOffset(date, offset, '-')) {
                offset += 1;
            }

            // extract day
            int day = parseInt(date, offset, offset += 2);
            // default time value
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            // always use 0 otherwise returned date will include millis of current time
            int milliseconds = 0;
            if (checkOffset(date, offset, 'T')) {

                // extract hours, minutes, seconds and milliseconds
                hour = parseInt(date, offset += 1, offset += 2);
                if (checkOffset(date, offset, ':')) {
                    offset += 1;
                }

                minutes = parseInt(date, offset, offset += 2);
                if (checkOffset(date, offset, ':')) {
                    offset += 1;
                }
                // second and milliseconds can be optional
                if (date.length() > offset) {
                    char c = date.charAt(offset);
                    if (c != 'Z' && c != '+' && c != '-') {
                        seconds = parseInt(date, offset, offset += 2);
                        // milliseconds can be optional in the format
                        if (checkOffset(date, offset, '.')) {
                            milliseconds = parseInt(date, offset += 1, offset += 3);
                        }
                    }
                }
            }

            // extract timezone
            String timezoneId;
            if (date.length() <= offset) {
                throw new IllegalArgumentException("No time zone indicator");
            }
            char timezoneIndicator = date.charAt(offset);
            if (timezoneIndicator == '+' || timezoneIndicator == '-') {
                String timezoneOffset = date.substring(offset);
                timezoneId = GMT_ID + timezoneOffset;
                offset += timezoneOffset.length();
            } else if (timezoneIndicator == 'Z') {
                timezoneId = GMT_ID;
                offset += 1;
            } else {
                throw new IndexOutOfBoundsException("Invalid time zone indicator " +
                        timezoneIndicator);
            }

            TimeZone timezone = TimeZone.getTimeZone(timezoneId);
            if (!timezone.getID().equals(timezoneId)) {
                throw new IndexOutOfBoundsException();
            }

            Calendar calendar = new GregorianCalendar(timezone);
            calendar.setLenient(false);
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, seconds);
            calendar.set(Calendar.MILLISECOND, milliseconds);

            pos.setIndex(offset);
            return calendar.getTime();
            // If we get a ParseException it'll already have the right message/offset.
            // Other exception types can convert here.
        } catch (IndexOutOfBoundsException e) {
            AylaLog.e(LOG_TAG, e.getLocalizedMessage());
        } catch (NumberFormatException e) {
            AylaLog.e(LOG_TAG, "NumberFormatException " + e.getLocalizedMessage());
        } catch (IllegalArgumentException e) {
            AylaLog.e(LOG_TAG, "IllegalArgumentException" + e.getLocalizedMessage());
        }

        //------------------------------------------------------------------------------
        // Patch up problem that was occurring by using the default Gson DateTypeAdapter. This would
        // format the Dates depending on the DEVICE date preferences, but had problems handling 24 hour
        // format. See gson issue https://github.com/google/gson/issues/935.
        // For now we will convert from both the 12 and 24 hour versions, and from then on use UTC format.
        //------------------------------------------------------------------------------

        // Try 12 hour, device locale
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.parse(date);
        } catch (ParseException e) {
            AylaLog.e(LOG_TAG, "UtcDateTypeAdapterUtil cannot be parsed as 12hr STRING");
        }

        // Try 24 hour, device locale
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.parse(date);
        } catch (ParseException e) {
            AylaLog.e(LOG_TAG, "UtcDateTypeAdapterUtil cannot be parsed as 24hr STRING");
        }

        // Try 12 hour, US locale
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.parse(date);
        } catch (ParseException e) {
            AylaLog.e(LOG_TAG, "UtcDateTypeAdapterUtil cannot be parsed as 12hr STRING");
        }

        // Try 24 hour, US locale
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            return formatter.parse(date);
        } catch (ParseException e) {
            AylaLog.e(LOG_TAG, "UtcDateTypeAdapterUtil cannot be parsed as 24hr STRING");
            fail = e;
        }

        //------------------------------------------------------------------------------

        String input = (date == null) ? null : ('"' + date + "'");
        throw new ParseException("Failed to parse date [" + input + "]: " + fail.getMessage(),
                pos.getIndex());
    }

    /**
     * Check if the expected character exist at the given offset in the value.
     *
     * @param value    the string to check at the specified offset
     * @param offset   the offset to look for the expected character
     * @param expected the expected character
     * @return true if the expected character exist at the given offset
     */
    private static boolean checkOffset(String value, int offset, char expected) {
        return (offset < value.length()) && (value.charAt(offset) == expected);
    }

    /**
     * Parse an integer located between 2 given offsets in a string
     *
     * @param value      the string to parse
     * @param beginIndex the start index for the integer in the string
     * @param endIndex   the end index for the integer in the string
     * @return the int
     * @throws NumberFormatException if the value is not a number
     */
    private static int parseInt(String value, int beginIndex, int endIndex) throws
            NumberFormatException {
        if (beginIndex < 0 || endIndex > value.length() || beginIndex > endIndex) {
            throw new NumberFormatException(value);
        }
        // use same logic as in Integer.parseInt() but less generic we're not supporting
        // negative values
        int i = beginIndex;
        int result = 0;
        int digit;
        if (i < endIndex) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value);
            }
            result = -digit;
        }
        while (i < endIndex) {
            digit = Character.digit(value.charAt(i++), 10);
            if (digit < 0) {
                throw new NumberFormatException("Invalid number: " + value);
            }
            result *= 10;
            result -= digit;
        }
        return -result;
    }
}