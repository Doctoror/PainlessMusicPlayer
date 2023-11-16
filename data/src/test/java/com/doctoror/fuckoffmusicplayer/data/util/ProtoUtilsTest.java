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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;

import com.doctoror.fuckoffmusicplayer.data.settings.nano.SettingsProto;
import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * {@link ProtoUtils} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class ProtoUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testToByteArrayNull() throws Exception {
        //noinspection ConstantConditions
        ProtoUtils.toByteArray(null);
    }

    @Test
    public void testToByteArrayNotNull() throws Exception {
        final byte[] data = ProtoUtils.toByteArray(new SettingsProto.Settings());
        assertNotNull(data);
    }

    @Test
    public void testToByteArray() throws Exception {
        final SettingsProto.Settings message = new SettingsProto.Settings();
        message.scrobbleEnabled = true;
        message.theme = 666;

        final byte[] data = ProtoUtils.toByteArray(message);
        assertNotNull(data);

        final SettingsProto.Settings fromBytes = MessageNano.mergeFrom(
                new SettingsProto.Settings(), data);

        assertEquals(message.scrobbleEnabled, fromBytes.scrobbleEnabled);
        assertEquals(message.theme, fromBytes.theme);
    }

    @Test
    public void testReadFromFileWhenNull() {
        final String fileName = "protoUtilsReadFromFileWhenNull";

        final Context context = RuntimeEnvironment.application;
        context.deleteFile(fileName);

        final SettingsProto.Settings read = ProtoUtils.readFromFile(
                context, fileName, new SettingsProto.Settings());
        assertNull(read);
    }

    @Test
    public void testReadFromFileNonNullWhenNull() {
        final String fileName = "protoUtilsReadFromFileNonNullWhenNull";

        final Context context = RuntimeEnvironment.application;
        context.deleteFile(fileName);

        final SettingsProto.Settings read = ProtoUtils.readFromFileNonNull(
                context, fileName, new SettingsProto.Settings());
        assertNotNull(read);
    }

    @Test
    public void testReadFromFile() throws Exception {
        final SettingsProto.Settings message = new SettingsProto.Settings();
        message.scrobbleEnabled = true;
        message.theme = 666;

        final String fileName = "protoUtilsReadFromFile";
        final Context context = RuntimeEnvironment.application;
        context.deleteFile(fileName);

        final OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ByteStreams.copy(new ByteArrayInputStream(ProtoUtils.toByteArray(message)), os);
        os.close();

        final SettingsProto.Settings read = ProtoUtils.readFromFile(
                context, fileName, new SettingsProto.Settings());

        assertNotNull(read);
        assertEquals(message.scrobbleEnabled, read.scrobbleEnabled);
        assertEquals(message.theme, read.theme);
    }

    @Test
    public void testWriteToFile() throws Exception {
        final SettingsProto.Settings message = new SettingsProto.Settings();
        message.scrobbleEnabled = true;
        message.theme = 666;

        final String fileName = "protoUtilsWriteToFile";

        final Context context = RuntimeEnvironment.application;

        // Delete old copies
        context.deleteFile(fileName);

        // Write actual file
        ProtoUtils.writeToFile(context, fileName, message);

        // Read
        final InputStream is = context.openFileInput(fileName);
        final byte[] readBytes = ByteStreams.toByteArray(is);
        assertNotNull(readBytes);

        // Create from read
        final SettingsProto.Settings read = new SettingsProto.Settings()
                .mergeFrom(CodedInputByteBufferNano.newInstance(readBytes));

        // compare
        assertEquals(message.scrobbleEnabled, read.scrobbleEnabled);
        assertEquals(message.theme, read.theme);
    }

}
