package com.doctoror.fuckoffmusicplayer.util;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;

import static org.junit.Assert.*;

/**
 * Created by Yaroslav Mytkalyk on 26.12.16.
 */

@RunWith(AndroidJUnit4.class)
public final class SqlUtilsAndroidTest {

    @Test(expected = NullPointerException.class)
    public void testEscapeAndWrapForLikeArgumentNull() throws Exception {
        SqlUtils.escapeAndWrapForLikeArgument(null);
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
        assertEquals("'%''Don''t''%\'", SqlUtils.escapeAndWrapForLikeArgument("\'Don\'t\'"));
    }
}
