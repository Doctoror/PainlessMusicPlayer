/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.data.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * {@link SqlUtils} android test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
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
