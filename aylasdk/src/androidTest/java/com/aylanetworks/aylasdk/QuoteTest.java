package com.aylanetworks.aylasdk;
/*
 * Android_AylaSDK
 *
 * Copyright 2015 Ayla Networks, all rights reserved
 */

import androidx.test.runner.AndroidJUnit4;

import com.aylanetworks.aylasdk.util.ObjectUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class QuoteTest {
    @Test
    public void testQuote() {
        String s = "This is a string. It has a \"quoted string\" in the middle.";
        String quoted = ObjectUtils.quote(s);
        assertEquals("\"This is a string. It has a \"quoted string\" in the middle.\"",
                quoted);

        String unquoted = ObjectUtils.unquote(quoted);
        assertEquals(s, unquoted);
    }

    @Test
    public void testQuoteNull() {
        String s = ObjectUtils.quote(null);
        assertNotNull(s);
        assertEquals(s, "\"null\"");
    }

    @Test
    public void testUnquoteNull() {
        String s = ObjectUtils.unquote(null);
        assertNull(s);
    }
}
