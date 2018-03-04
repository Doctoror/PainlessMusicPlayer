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

import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaManager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * {@link MediaManager} that handle files
 */
public final class MediaManagerFile implements MediaManager {

    private static final String TAG = "MediaManagerFile";

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaManagerFile(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public void deletePlaylist(final long id) throws IOException {
        final String path = getPlaylistPath(mContentResolver, id);
        if (!TextUtils.isEmpty(path)) {
            deleteFile(new File(path));
        }
    }

    @Override
    public void deleteMedia(final long id) throws IOException {
        final String path = getMediaPath(mContentResolver, id);
        if (!TextUtils.isEmpty(path)) {
            deleteFile(new File(path));
        }
    }

    @Override
    public void deleteAlbum(final long id) throws IOException {
        final String[] paths = getAlbumMediaPaths(mContentResolver, id);
        IOException caught = null;
        for (final String path : paths) {
            if (!TextUtils.isEmpty(path)) {
                try {
                    deleteFile(new File(path));
                } catch (IOException e) {
                    Log.w(TAG, e);
                    if (caught == null) {
                        caught = e;
                    }
                }
            }
        }
        if (caught != null) {
            throw caught;
        }
    }

    @WorkerThread
    private static void deleteFile(@NonNull final File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file);
        }
        if (!file.canWrite()) {
            throw new SecurityIoException("File is not writable: " + file);
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
    }

    @NonNull
    private static String[] getAlbumMediaPaths(@NonNull final ContentResolver resolver,
            final long id) throws IOException {
        final Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.ALBUM_ID + '=' + id,
                null,
                null);
        return getFirstStringColumnForAllRowsAndClose(c);
    }

    @Nullable
    private static String getMediaPath(@NonNull final ContentResolver resolver,
            final long id) throws IOException {
        final Cursor c = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Playlists._ID + '=' + id,
                null,
                null);
        return getFirstStringColumnAndClose(c);
    }

    @Nullable
    private static String getPlaylistPath(@NonNull final ContentResolver resolver,
            final long id) throws IOException {
        final Cursor c = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Playlists.DATA},
                MediaStore.Audio.Playlists._ID + '=' + id,
                null,
                null);
        return getFirstStringColumnAndClose(c);
    }

    @Nullable
    private static String getFirstStringColumnAndClose(@Nullable final Cursor c)
            throws IOException {
        if (c == null) {
            throw new IOException("Query returned null");
        }
        try {
            if (!c.moveToFirst()) {
                throw new IOException("Query returned empty cursor");
            }
            return c.getString(0);
        } finally {
            c.close();
        }
    }

    @NonNull
    private static String[] getFirstStringColumnForAllRowsAndClose(@Nullable final Cursor c)
            throws IOException {
        if (c == null) {
            throw new IOException("Query returned null");
        }
        final String[] result = new String[c.getCount()];
        int i = 0;
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext(), i++) {
            result[i] = c.getString(0);
        }
        c.close();
        return result;
    }
}
