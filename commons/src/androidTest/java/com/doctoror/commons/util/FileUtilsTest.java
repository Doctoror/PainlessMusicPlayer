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
package com.doctoror.commons.util;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * {@link FileUtils} test
 */
public final class FileUtilsTest {

    @Test
    public void testReadPrivateFile() throws Exception {
        final byte[] data = new byte[] {
                0x1, 0x2, 0x3, 0x4, 0xC
        };

        final String fileName = "testFileForRead";
        final Context context = InstrumentationRegistry.getContext();

        final OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ByteStreams.copy(new ByteArrayInputStream(data), os);
        os.close();

        final byte[] read = FileUtils.readPrivateFile(context, fileName);
        assertArrayEquals(data, read);
    }

    @Test
    public void testWritePrivateFile() throws Exception {
        final byte[] data = new byte[] {
                0x1, 0x2, 0x3, 0x4, 0x8
        };

        final String fileName = "testFileForWrite";
        final Context context = InstrumentationRegistry.getContext();
        FileUtils.writeOrDeletePrivateFile(context, fileName, data);

        final InputStream is = context.openFileInput(fileName);
        final byte[] read = ByteStreams.toByteArray(is);

        assertArrayEquals(data, read);
    }

    @Test(expected = FileNotFoundException.class)
    public void testDeletePrivateFile() throws Exception {
        final byte[] data = new byte[] {
                0x1, 0x2, 0x3, 0x4, 0x8
        };

        final String fileName = "testFileForWrite";
        final Context context = InstrumentationRegistry.getContext();

        // Write file
        final OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ByteStreams.copy(new ByteArrayInputStream(data), os);
        os.close();

        // Delete file
        FileUtils.writeOrDeletePrivateFile(context, fileName, null);

        // Open for test. Must throw FileNotFoundException
        context.openFileInput(fileName);
    }

}
