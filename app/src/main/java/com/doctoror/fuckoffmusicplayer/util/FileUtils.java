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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * File utils
 */
public final class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException();
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
        context.deleteFile(fileName);
        if (data == null) {
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
