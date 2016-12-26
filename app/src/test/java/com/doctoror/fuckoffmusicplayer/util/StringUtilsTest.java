package com.doctoror.fuckoffmusicplayer.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {@link StringUtils} test
 */
public final class StringUtilsTest {

    @Test
    public void testNotNullStringNullValue() throws Exception {
        assertEquals("", StringUtils.notNullString(null));
    }

    @Test
    public void testNotNullStringNullValueNotEquals() throws Exception {
        assertNotEquals(null, StringUtils.notNullString(null));
    }

    @Test
    public void testNotNullStringNonNullValue() throws Exception {
        final String expected = "Shitcore";
        assertEquals(expected, StringUtils.notNullString(expected));
    }

    @Test
    public void testNotNullStringNonEquals() throws Exception {
        assertNotEquals("Crapcore", StringUtils.notNullString("Shitcore"));
    }

    @Test(expected = NullPointerException.class)
    public void testCapWordsNull() throws Exception {
        //noinspection ConstantConditions
        StringUtils.capWords(null);
    }

    @Test
    public void testCapWordsEmpty() throws Exception {
        assertEquals("", StringUtils.capWords(""));
    }

    @Test
    public void testCapWordsSingleWord() throws Exception {
        assertEquals("Crapman", StringUtils.capWords("crapman"));
    }

    @Test
    public void testCapWordsTwoWords() throws Exception {
        assertEquals("Captain Shitman", StringUtils.capWords("Captain shitman"));
    }

    @Test
    public void testCapWordsTwoWordsNotEquals() throws Exception {
        assertNotEquals("captain shitman", StringUtils.capWords("captain shitman"));
    }

    @Test
    public void testCapWordsTwoWordsMultipleWhitespaces() throws Exception {
        assertEquals("Captain    Shitman", StringUtils.capWords("captain    shitman"));
    }
}
