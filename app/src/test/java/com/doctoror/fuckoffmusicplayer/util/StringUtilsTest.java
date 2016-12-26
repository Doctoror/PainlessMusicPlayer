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
