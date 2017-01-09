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

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.File;
import java.io.IOException;

/**
 * Created by Yaroslav Mytkalyk on 27.10.16.
 */

public final class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    @WorkerThread
    public static void delete(@NonNull final ContentResolver resolver,
            @NonNull final Media media) throws IOException {
        final Uri data = media.getData();
        final File file = new File(data.getPath());
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file);
        }
        if (!file.canWrite()) {
            throw new IOException("File is not writable: " + file);
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
        final int count = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + "=" + media.getId(), null);
        if (count != 1) {
            Log.w(TAG, "Unexpected count when deleting from MediaStore, count = " + count);
        }
    }
}
