/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.data.media;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;

import java.io.IOException;

public final class AlbumMediaIdsProviderImpl implements AlbumMediaIdsProvider {

    private final ContentResolver contentResolver;

    public AlbumMediaIdsProviderImpl(@NonNull final ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    @Override
    public long[] getAlbumMediaIds(final long ambumId) throws IOException {
        final Cursor c = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.ALBUM_ID + '=' + ambumId,
                null,
                null);
        return getFirstLongColumnForAllRowsAndClose(c);
    }

    @NonNull
    private static long[] getFirstLongColumnForAllRowsAndClose(@Nullable final Cursor c)
            throws IOException {
        if (c == null) {
            throw new IOException("Query returned null");
        }
        final long[] result = new long[c.getCount()];
        int i = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
            result[i] = c.getLong(0);
        }
        c.close();
        return result;
    }
}
