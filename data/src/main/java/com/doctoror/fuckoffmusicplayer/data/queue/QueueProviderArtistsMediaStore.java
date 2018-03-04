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

import com.doctoror.fuckoffmusicplayer.domain.queue.QueueConfig;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.data.util.SqlUtils;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import io.reactivex.Observable;

/**
 * MediaStore {@link QueueProviderArtists}
 */
public final class QueueProviderArtistsMediaStore implements QueueProviderArtists {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public QueueProviderArtistsMediaStore(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromArtist(final long artistId) {
        return mMediaProvider.load(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC + " AND "
                        + MediaStore.Audio.Media.ARTIST_ID + '=' + artistId,
                null,
                MediaStore.Audio.Media.ALBUM_ID + ',' + MediaStore.Audio.Media.TRACK,
                null);
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromArtistSearch(@Nullable final String query) {
        final StringBuilder sel = new StringBuilder(256);
        sel.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            sel.append(" AND ").append(MediaStore.Audio.Media.ARTIST).append(" LIKE ")
                    .append(SqlUtils.escapeAndWrapForLikeArgument(query));
        }

        return mMediaProvider.load(sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK,
                QueueConfig.MAX_QUEUE_SIZE);
    }
}
