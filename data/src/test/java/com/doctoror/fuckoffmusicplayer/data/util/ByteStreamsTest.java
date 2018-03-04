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

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * {@link ByteStreams} test
 */
public final class ByteStreamsTest {

    @Test(expected = NullPointerException.class)
    public void testCheckNotNullWithNullValue() throws Exception {
        ByteStreams.checkNotNull(null);
    }

    @Test
    public void testCheckNotNull() throws Exception {
        ByteStreams.checkNotNull(new Object());
    }

    @Test(expected = NullPointerException.class)
    public void testCopyNulls() throws Exception {
        ByteStreams.copy(null, null);
    }

    @Test
    public void testCopy() throws Exception {
        final byte[] input = new byte[] {
                0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE
        };

        final InputStream is = new ByteArrayInputStream(input);
        final ByteArrayOutputStream os = new ByteArrayOutputStream(input.length);

        ByteStreams.copy(is, os);

        assertArrayEquals(input, os.toByteArray());
    }

    @Test
    public void testToByteArray() throws Exception {
        final byte[] input = new byte[] {
                0x1, 0x2, 0x3, 0x4, 0x5, 0x6, 0x7, 0x8, 0x9, 0xA, 0xB, 0xC, 0xD, 0xE
        };

        final InputStream is = new ByteArrayInputStream(input);
        final byte[] result = ByteStreams.toByteArray(is);

        assertArrayEquals(input, result);
    }

}
