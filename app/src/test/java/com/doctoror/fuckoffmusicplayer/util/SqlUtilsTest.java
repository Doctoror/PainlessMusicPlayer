package com.doctoror.fuckoffmusicplayer.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * {@link SqlUtils} test
 */
public final class SqlUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testWrapForLikeArgumentNull() throws Exception {
        //noinspection ConstantConditions
        SqlUtils.wrapForLikeArgument(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapForLikeArgumentEmpty() throws Exception {
        SqlUtils.wrapForLikeArgument("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapForLikeArgumentShort() throws Exception {
        SqlUtils.wrapForLikeArgument("'");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWrapForLikeArgumentNotWrapped() throws Exception {
        SqlUtils.wrapForLikeArgument("a'");
    }

    @Test
    public void testWrapForLikeArgumentEmptyQuotes() throws Exception {
        assertEquals("'%%'", SqlUtils.wrapForLikeArgument("''"));
    }

    @Test
    public void testWrapForLikeArgumentEmptyQuotesNotEquals() throws Exception {
        assertNotEquals("''", SqlUtils.wrapForLikeArgument("''"));
    }

    @Test
    public void testWrapForLikeArgumentNonEmptyArgument() throws Exception {
        assertEquals("'%asshole%'", SqlUtils.wrapForLikeArgument("'asshole'"));
    }

    @Test
    public void testEscapeAndWrapForLikeArgumentNull() throws Exception {
        assertEquals("'%NULL%'", SqlUtils.escapeAndWrapForLikeArgument(null));
    }

    @Test
    public void testEscapeAndWrapForLikeArgumentEmpty() throws Exception {
        assertEquals("'%%'", SqlUtils.escapeAndWrapForLikeArgument(""));
    }

    @Test
    public void testEscapeAndWrapForLikeArgumentWithApostrophe() throws Exception {
        assertEquals("'%Don''t%'", SqlUtils.escapeAndWrapForLikeArgument("Don't"));
    }

    @Test
    public void testEscapeAndWrapForLikeArgumentWithQuotes() throws Exception {
        assertEquals("'%'Don''t'%'", SqlUtils.escapeAndWrapForLikeArgument("'Don't'"));
    }
}
