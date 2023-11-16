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

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.commons.util.Log;
import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import java.io.IOException;

/**
 * Protobuf nano utils
 */
public final class ProtoUtils {

    private static final String TAG = "ProtoUtils";

    private ProtoUtils() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static byte[] toByteArray(@NonNull final MessageNano messageNano) throws IOException {
        final byte[] output = new byte[messageNano.getSerializedSize()];
        messageNano.writeTo(CodedOutputByteBufferNano.newInstance(output));
        return output;
    }

    public static void writeToFile(@NonNull final Context context,
            @NonNull final String fileName,
            @NonNull final MessageNano messageNano) {
        final byte[] output;
        try {
            output = toByteArray(messageNano);
        } catch (IOException e) {
            Log.w(TAG, e);
            return;
        }
        FileUtils.writeOrDeletePrivateFile(context, fileName, output);
    }

    @Nullable
    public static <T extends MessageNano> T readFromFile(@NonNull final Context context,
            @NonNull final String fileName,
            @NonNull final T obj) {
        T message = null;
        try {
            final byte[] bytes = FileUtils.readPrivateFile(context, fileName);
            message = MessageNano.mergeFrom(obj, bytes);
        } catch (IOException e) {
            Log.w(TAG, e);
        }

        return message;
    }

    @NonNull
    public static <T extends MessageNano> T readFromFileNonNull(@NonNull final Context context,
            @NonNull final String fileName,
            @NonNull final T obj) {
        final T message = readFromFile(context, fileName, obj);
        return message != null ? message : obj;
    }

}
