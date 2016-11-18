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

import com.google.protobuf.nano.CodedOutputByteBufferNano;
import com.google.protobuf.nano.MessageNano;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class ProtoPersister {

    private static final String TAG = "ProtoPersister";

    private ProtoPersister() {
        throw new UnsupportedOperationException();
    }

    public static void writeToFile(@NonNull final Context context,
            @NonNull final String fileName,
            @NonNull final MessageNano messageNano) {
        final byte[] output = new byte[messageNano.getCachedSize()];
        try {
            messageNano.writeTo(CodedOutputByteBufferNano.newInstance(output));
        } catch (IOException e) {
            Log.w(TAG, e);
            return;
        }
        writeOrDeletePrivateFile(context, fileName, output);
    }

    @Nullable
    public static <T extends MessageNano> T readFromFile(@NonNull final Context context,
            @NonNull final String fileName,
            @NonNull final T obj) {
        T message = null;
        try {
            final byte[] bytes = readPrivateFile(context, fileName);
            message = MessageNano.mergeFrom(obj, bytes);
        } catch (IOException e) {
            Log.w(TAG, e);
        }

        return message;
    }

    @NonNull
    public static byte[] readPrivateFile(@NonNull final Context context,
            @NonNull final String fileName) throws IOException {
        InputStream is = null;
        try {
            is = context.openFileInput(fileName);
            return ByteStreams.toByteArray(is);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    @Nullable
    public static byte[] readPrivateFileSilently(@NonNull final Context context,
            @NonNull final String fileName) {
        try {
            return readPrivateFile(context, fileName);
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    /**
     * Silently writes or deletes private file. If the bytes are null the file will be deleted.
     *
     * @param context  Context to use
     * @param fileName File name
     * @param data     data to write. If the data is null trhe file will be deleted
     * @return true on success, false on error
     */
    public static boolean writeOrDeletePrivateFile(@NonNull final Context context,
            @NonNull final String fileName,
            @Nullable final byte[] data) {
        if (data == null) {
            context.deleteFile(fileName);
            return true;
        } else {
            OutputStream os = null;
            try {
                os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                os.write(data);
                os.close();
                return true;
            } catch (IOException e) {
                Log.w(TAG, e);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Log.w(TAG, e);
                    }
                }
            }
        }
        return false;
    }
}
