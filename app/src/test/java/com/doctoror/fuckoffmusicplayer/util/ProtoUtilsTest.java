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

import com.google.protobuf.nano.CodedInputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import com.doctoror.commons.util.ByteStreams;
import com.doctoror.commons.wear.nano.WearPlaybackData;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.*;

/**
 * {@link ProtoUtils} test
 */
public final class ProtoUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testToByteArrayNull() throws Exception {
        //noinspection ConstantConditions
        ProtoUtils.toByteArray(null);
    }

    @Test
    public void testToByteArrayNotNull() throws Exception {
        final byte[] data = ProtoUtils.toByteArray(new WearPlaybackData.PlaybackPosition());
        assertNotNull(data);
    }

    @Test
    public void testToByteArray() throws Exception {
        final WearPlaybackData.PlaybackPosition message = new WearPlaybackData.PlaybackPosition();
        message.mediaId = 666L;
        message.position = 5;

        final byte[] data = ProtoUtils.toByteArray(message);
        assertNotNull(data);

        final WearPlaybackData.PlaybackPosition fromBytes = MessageNano.mergeFrom(
                new WearPlaybackData.PlaybackPosition(), data);

        assertEquals(message.mediaId, fromBytes.mediaId);
        assertEquals(message.position, fromBytes.position);
    }

    @Test
    public void testReadFromFileWhenNull() throws Exception {
        final String fileName = "protoUtilsReadFromFileWhenNull";

        final Context context = InstrumentationRegistry.getContext();
        context.deleteFile(fileName);

        final WearPlaybackData.PlaybackPosition read = ProtoUtils.readFromFile(
                context, fileName, new WearPlaybackData.PlaybackPosition());
        assertNull(read);
    }

    @Test
    public void testReadFromFileNonNullWhenNull() throws Exception {
        final String fileName = "protoUtilsReadFromFileNonNullWhenNull";

        final Context context = InstrumentationRegistry.getContext();
        context.deleteFile(fileName);

        final WearPlaybackData.PlaybackPosition read = ProtoUtils.readFromFileNonNull(
                context, fileName, new WearPlaybackData.PlaybackPosition());
        assertNotNull(read);
    }

    @Test
    public void testReadFromFile() throws Exception {
        final WearPlaybackData.PlaybackPosition message = new WearPlaybackData.PlaybackPosition();
        message.mediaId = 666L;
        message.position = 128;

        final String fileName = "protoUtilsReadFromFile";
        final Context context = InstrumentationRegistry.getContext();
        context.deleteFile(fileName);

        final OutputStream os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
        ByteStreams.copy(new ByteArrayInputStream(ProtoUtils.toByteArray(message)), os);
        os.close();

        final WearPlaybackData.PlaybackPosition read = ProtoUtils.readFromFile(
                context, fileName, new WearPlaybackData.PlaybackPosition());

        assertNotNull(read);
        assertEquals(message.mediaId, read.mediaId);
        assertEquals(message.position, read.position);
    }

    @Test
    public void testWriteToFile() throws Exception {
        final WearPlaybackData.PlaybackPosition message = new WearPlaybackData.PlaybackPosition();
        message.mediaId = 666L;
        message.position = 128;

        final String fileName = "protoUtilsWriteToFile";

        final Context context = InstrumentationRegistry.getContext();

        // Delete old copies
        context.deleteFile(fileName);

        // Write actual file
        ProtoUtils.writeToFile(context, fileName, message);

        // Read
        final InputStream is = context.openFileInput(fileName);
        final byte[] readBytes = ByteStreams.toByteArray(is);
        assertNotNull(readBytes);

        // Create from read
        final WearPlaybackData.PlaybackPosition read = new WearPlaybackData.PlaybackPosition()
                .mergeFrom(CodedInputByteBufferNano.newInstance(readBytes));

        // compare
        assertEquals(message.mediaId, read.mediaId);
        assertEquals(message.position, read.position);
    }

}
