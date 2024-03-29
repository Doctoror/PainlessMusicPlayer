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
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

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
}
