package com.doctoror.fuckoffmusicplayer.util;

import org.junit.Test;

import static org.junit.Assert.*;

public final class StringUtilsTest {

    @Test
    public void testNotNullStringWithNullValue() throws Exception {
        assertEquals("", StringUtils.notNullString(null));
    }

    @Test
    public void testNotNullStringWithNonNullValue() throws Exception {
        final String expected = "Shitcore";
        assertEquals(expected, StringUtils.notNullString(expected));
    }

}
