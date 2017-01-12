/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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

import android.content.ContentResolver;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.io.IOException;

/**
 * {@link MediaStore} utils
 */
public final class MediaStoreUtils {

    private MediaStoreUtils() {
        throw new UnsupportedOperationException();
    }

    @WorkerThread
    public static void deletePlaylist(@NonNull final ContentResolver resolver,
            final long targetId) throws IOException {
        final int count = resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Playlists._ID + "=" + targetId, null);
        if (count != 1) {
            throw new IOException(
                    "Unexpected count when deleting playlist from MediaStore, count = " + count);
        }
    }
}
