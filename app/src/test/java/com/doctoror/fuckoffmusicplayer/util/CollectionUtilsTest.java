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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * {@link CollectionUtils} test
 */
public final class CollectionUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testToLongArrayNull() throws Exception {
        //noinspection ConstantConditions
        CollectionUtils.toLongArray(null);
    }

    @Test
    public void testToLongArrayEmpty() throws Exception {
        //noinspection unchecked
        final long[] result = CollectionUtils.toLongArray(Collections.EMPTY_LIST);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testToLongArray() throws Exception {
        final long[] expected = new long[] {1L, 2L, 3L, 4L, 5L, 666L};
        final Collection<Long> input = Arrays.asList(1L, 2L, 3L, 4L, 5L, 666L);
        final long[] result = CollectionUtils.toLongArray(input);
        assertArrayEquals(expected, result);
    }

    @Test(expected = NullPointerException.class)
    public void testToReverseLongArrayNull() throws Exception {
        //noinspection ConstantConditions
        CollectionUtils.toReverseLongArray(null);
    }

    @Test
    public void testToReverseLongArrayEmpty() throws Exception {
        //noinspection unchecked
        final long[] result = CollectionUtils.toReverseLongArray(Collections.EMPTY_LIST);
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testToReverseLongArray() throws Exception {
        final long[] expected = new long[] {666L, 5L, 4L, 3L, 2L, 1L};
        final Collection<Long> input = Arrays.asList(1L, 2L, 3L, 4L, 5L, 666L);
        final long[] result = CollectionUtils.toReverseLongArray(input);
        assertArrayEquals(expected, result);
    }
}
