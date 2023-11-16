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

import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.data.util.SqlUtils;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueConfig;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public final class QueueProviderAlbumsMediaStore implements QueueProviderAlbums {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public QueueProviderAlbumsMediaStore(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromAlbumSearch(@Nullable final String query) {
        final StringBuilder sel = new StringBuilder(256);
        sel.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            sel.append(" AND ").append(MediaStore.Audio.Media.ALBUM).append(" LIKE ")
                    .append(SqlUtils.escapeAndWrapForLikeArgument(query));
        }

        return mMediaProvider.load(sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK,
                QueueConfig.MAX_QUEUE_SIZE);
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromAlbum(final long albumId) {
        return fromAlbums(new long[]{albumId}, null);
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromAlbums(
            @NonNull final long[] albumIds,
            @Nullable final Long forArtist) {
        return Observable.fromCallable(() -> queueFromAlbums(albumIds, forArtist));
    }

    @NonNull
    private List<Media> queueFromAlbums(
            @NonNull final long[] albumIds,
            @Nullable final Long forArtist) {
        final List<Media> queue = new ArrayList<>(15 * albumIds.length);
        for (final long albumId : albumIds) {
            final StringBuilder selection = new StringBuilder(256);
            selection.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC).append(" AND ");
            selection.append(MediaStore.Audio.Media.ALBUM_ID).append('=').append(albumId);
            if (forArtist != null) {
                selection.append(" AND ")
                        .append(MediaStore.Audio.Media.ARTIST_ID).append('=').append(forArtist);
            }

            queue.addAll(mMediaProvider.load(selection.toString(), null,
                    MediaStore.Audio.Media.TRACK, null).take(1).blockingFirst());
        }
        return queue;
    }
}
