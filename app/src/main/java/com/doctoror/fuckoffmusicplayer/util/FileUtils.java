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
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbums;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v4.util.ArraySet;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 27.10.16.
 */

public final class FileUtils {

    private static final String TAG = "FileUtils";

    private FileUtils() {
        throw new UnsupportedOperationException();
    }

    @WorkerThread
    public static void deleteAlbum(@NonNull final ContentResolver resolver,
            @NonNull final PlaylistProviderAlbums playlistProvider,
            final long albumId) throws IOException {
        deleteAlbumFiles(resolver, playlistProvider, albumId);
    }

    @WorkerThread
    private static void deleteAlbumFiles(@NonNull final ContentResolver resolver,
            @NonNull final PlaylistProviderAlbums playlistProvider,
            final long albumId) throws IOException {
        final List<Media> mediaList = playlistProvider.fromAlbum(albumId);
        if (mediaList != null) {
            deleteMedias(resolver, mediaList);
        }
    }

    @WorkerThread
    private static void deleteMedias(@NonNull final ContentResolver resolver,
            @NonNull final Collection<Media> items) throws IOException {
        final Collection<Long> deletedMediaIds = new ArraySet<>(items.size());

        IOException exception = null;
        try {
            deleteMediaFiles(resolver, items, deletedMediaIds);
        } catch (IOException e) {
            Log.w(TAG, e);
            exception = e;
        }

        if (!deletedMediaIds.isEmpty()) {
            deleteMediasFromMediaStoreSilently(resolver, deletedMediaIds);
        }

        if (exception != null) {
            throw exception;
        }
    }

    @WorkerThread
    private static void deleteMediaFiles(@NonNull final ContentResolver resolver,
            @NonNull final Collection<Media> items,
            @NonNull final Collection<Long> deletedIds) throws IOException {
        IOException caught = null;
        for (final Media item : items) {
            try {
                deleteMedia(resolver, item);
                deletedIds.add(item.getId());
            } catch (IOException e) {
                Log.w(TAG, e);
                if (caught == null) {
                    caught = e;
                }
            }
        }

        if (caught != null) {
            throw caught;
        }
    }

    @WorkerThread
    private static void deleteMediasFromMediaStore(
            @NonNull final ContentResolver resolver,
            @NonNull final Collection<Long> ids) throws IOException {
        final int count = deleteMediasFromMediaStoreSilently(resolver, ids);
        if (count != ids.size()) {
            throw new IOException(
                    "Unexpected count when deleting medias from MediaStore, count = " + count);
        }
    }

    @WorkerThread
    private static int deleteMediasFromMediaStoreSilently(
            @NonNull final ContentResolver resolver,
            @NonNull final Collection<Long> ids) {
        return resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                SelectionUtils.inSelection(MediaStore.Audio.Media._ID, ids), null);
    }

    @WorkerThread
    public static void deleteMedia(@NonNull final ContentResolver resolver,
            @NonNull final Media media) throws IOException {
        deleteMediaFile(media);
        deleteMediaFromMediaStore(resolver, media);
    }

    @WorkerThread
    static void deleteMediaFile(@NonNull final Media media) throws IOException {
        final File file = fileForMedia(media);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file);
        }
        if (!file.canWrite()) {
            throw new IOException("File is not writable: " + file);
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file);
        }
    }

    @WorkerThread
    static void deleteMediaFromMediaStore(@NonNull final ContentResolver resolver,
            @NonNull final Media media) throws IOException {
        final int count = resolver.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media._ID + "=" + media.getId(), null);
        if (count != 1) {
            throw new IOException(
                    "Unexpected count when deleting media from MediaStore, count = " + count);
        }
    }

    @NonNull
    static File fileForMedia(@NonNull final Media media) {
        final Uri data = media.getData();
        return new File(data.getPath());
    }
}
