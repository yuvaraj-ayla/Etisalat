package com.aylanetworks.aylasdk;
/*
 * AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.util.DateUtils;
import com.aylanetworks.aylasdk.util.URLHelper;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimeZone;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class URLHelperTest {

    @Test
    public void testParameterizeArray() {
        String[] arrayItems = new String[]{"Item One", "Item Two", "Item Three"};
        String calculated = URLHelper.parameterizeArray("MyArray", arrayItems);
        String expected = "?MyArray[]=Item+One&MyArray[]=Item+Two&MyArray[]=Item+Three";
        assertEquals(expected, calculated);
    }

    @Test
    public void testAppendParameters() {
        String baseUrl = "https://www.google.com/";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("some_other_argument", "This has some spaces in it and an & ampersand");
        params.put("username", "bking");
        params.put("password", "mypassword");

        String calculated = URLHelper.appendParameters(baseUrl, params);
        String expected = "https://www.google" +
                ".com/?some_other_argument=This+has+some+spaces+in+it+and+an+%26+ampersand&" +
                "username=bking&password=mypassword";
        assertEquals(expected, calculated);
    }

    @Test
    public void testApiGmtDateFormat() {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        cal.set(2015, Calendar.DECEMBER, 28, 14, 15, 16);
        Date testDate = cal.getTime();

        String calculated = DateUtils.getISO8601DateFormat().format(testDate);
        String expected = "2015-12-28T14:15:16Z";
        assertEquals(expected, calculated);
    }
}
