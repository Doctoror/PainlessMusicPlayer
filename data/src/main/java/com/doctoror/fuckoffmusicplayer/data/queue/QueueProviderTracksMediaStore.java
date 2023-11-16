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
package com.doctoror.fuckoffmusicplayer.data.queue;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreVolumeNames;
import com.doctoror.fuckoffmusicplayer.data.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.data.util.SelectionUtils;
import com.doctoror.fuckoffmusicplayer.data.util.SqlUtils;
import com.doctoror.fuckoffmusicplayer.data.util.StringUtils;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueConfig;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * MediaStore {@link QueueProviderTracks}
 */
public final class QueueProviderTracksMediaStore implements QueueProviderTracks {

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public QueueProviderTracksMediaStore(
            @NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        mContentResolver = contentResolver;
        mMediaProvider = mediaProvider;
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromTracks(@NonNull final long[] trackIds) {
        return mMediaProvider.load(
                SelectionUtils.inSelectionLong(MediaStore.Audio.Media._ID, trackIds),
                null,
                MediaStore.Audio.Media.TITLE,
                null);
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromTracksSearch(@Nullable final String query) {
        return Observable.fromCallable(() -> queueFromTracksSearch(query));
    }

    @NonNull
    @WorkerThread
    private List<Media> queueFromTracksSearch(@Nullable final String query) {
        final List<Long> ids = new ArrayList<>(15);

        final StringBuilder sel = new StringBuilder(256);
        sel.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            final String likeQuery = " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(query);
            sel.append(" AND (").append(MediaStore.Audio.Media.TITLE).append(likeQuery);
            sel.append(" OR ").append(MediaStore.Audio.Media.ARTIST).append(likeQuery);
            sel.append(" OR ").append(MediaStore.Audio.Media.ALBUM).append(likeQuery);
            sel.append(')');
        }

        final List<Media> fromProvider = mMediaProvider.load(sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK,
                QueueConfig.MAX_QUEUE_SIZE).take(1).blockingFirst();

        for (final Media media : fromProvider) {
            ids.add(media.getId());
        }

        if (!TextUtils.isEmpty(query) && fromProvider.size() < QueueConfig.MAX_QUEUE_SIZE) {
            // Search in genres for tracks with media ids that do not match found ids
            Cursor c = mContentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    new String[]{BaseColumns._ID},
                    MediaStore.Audio.Genres.NAME + "=?",
                    new String[]{StringUtils.capWords(query)},
                    null);

            Long genreId = null;
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        genreId = c.getLong(0);
                    }
                } finally {
                    c.close();
                }
            }

            if (genreId != null) {
                fromProvider.addAll(mMediaProvider.load(
                        MediaStore.Audio.Genres.Members
                                .getContentUri(MediaStoreVolumeNames.EXTERNAL, genreId),
                        SelectionUtils.notInSelection(MediaStore.Audio.Media._ID, ids),
                        null,
                        "RANDOM()",
                        QueueConfig.MAX_QUEUE_SIZE - ids.size())
                        .take(1)
                        .blockingFirst());
            }
        }

        return fromProvider;
    }
}
