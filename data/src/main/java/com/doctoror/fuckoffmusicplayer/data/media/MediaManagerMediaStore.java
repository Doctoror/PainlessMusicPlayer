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
package com.doctoror.fuckoffmusicplayer.data.media;

import com.doctoror.fuckoffmusicplayer.data.util.SelectionUtils;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.io.IOException;

/**
 * {@link MediaStore} {@link MediaManager}
 */
public final class MediaManagerMediaStore implements MediaManager {

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaManagerMediaStore(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public void deletePlaylist(final long id) throws IOException {
        final int count = mContentResolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Playlists._ID + '=' + id, null);
        if (count != 1) {
            throw new IOException(
                    "Unexpected count when deleting playlist from MediaStore, count = " + count);
        }
    }

    @Override
    public void deleteMedia(final long id) throws IOException {
        final int count = mContentResolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + '=' + id, null);
        if (count != 1) {
            throw new IOException(
                    "Unexpected count when deleting playlist from MediaStore, count = " + count);
        }
    }

    @Override
    public void deleteAlbum(final long id) throws IOException {
        final long[] mediaIds = getAlbumMediaIds(mContentResolver, id);
        deleteMedias(mContentResolver, mediaIds);
    }

    @NonNull
    public static long[] getAlbumMediaIds(@NonNull final ContentResolver resolver,
            final long id) throws IOException {
        final Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.ALBUM_ID + '=' + id,
                null,
                null);
        return getFirstLongColumnForAllRowsAndClose(c);
    }

    @WorkerThread
    static void deleteMedias(
            @NonNull final ContentResolver resolver,
            @NonNull final long[] ids) throws IOException {
        final int count = deleteMediasSilently(resolver, ids);
        if (count != ids.length) {
            throw new IOException(
                    "Unexpected count when deleting medias from MediaStore, count = " + count);
        }
    }

    @WorkerThread
    private static int deleteMediasSilently(
            @NonNull final ContentResolver resolver,
            @NonNull final long[] ids) {
        return resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                SelectionUtils.inSelectionLong(MediaStore.Audio.Media._ID, ids), null);
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
