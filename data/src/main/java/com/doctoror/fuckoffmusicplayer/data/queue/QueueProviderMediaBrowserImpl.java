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
package com.doctoror.fuckoffmusicplayer.data.queue;

import com.doctoror.fuckoffmusicplayer.data.media.browser.MediaBrowserConstants;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderGenres;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRandom;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderMediaBrowser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

public final class QueueProviderMediaBrowserImpl implements QueueProviderMediaBrowser {

    private final QueueProviderAlbums queueProviderAlbums;
    private final QueueProviderGenres queueProviderGenres;
    private final QueueProviderRecentlyScanned queueProviderRecentlyScanned;
    private final QueueProviderRandom queueProviderRandom;

    public QueueProviderMediaBrowserImpl(
            @NonNull final QueueProviderAlbums queueProviderAlbums,
            @NonNull final QueueProviderGenres queueProviderGenres,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned,
            @NonNull final QueueProviderRandom queueProviderRandom) {
        this.queueProviderAlbums = queueProviderAlbums;
        this.queueProviderGenres = queueProviderGenres;
        this.queueProviderRecentlyScanned = queueProviderRecentlyScanned;
        this.queueProviderRandom = queueProviderRandom;
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromMediaBrowserId(@Nullable final String mediaId) {
        if (TextUtils.isEmpty(mediaId)) {
            return Observable.just(Collections.emptyList());
        }

        if (MediaBrowserConstants.MEDIA_ID_RANDOM.equals(mediaId)) {
            return queueProviderRandom.randomQueue();
        }

        if (MediaBrowserConstants.MEDIA_ID_RECENT.equals(mediaId)) {
            return queueProviderRecentlyScanned.recentlyScannedQueue();
        }

        if (mediaId.startsWith(MediaBrowserConstants.MEDIA_ID_PREFIX_ALBUM)) {
            return queueSourceFromAlbumId(mediaId);
        }

        if (mediaId.startsWith(MediaBrowserConstants.MEDIA_ID_PREFIX_GENRE)) {
            return queueSourceFromGenreId(mediaId);
        }

        return Observable.just(Collections.emptyList());
    }

    @NonNull
    private Observable<List<Media>> queueSourceFromAlbumId(@NonNull final String mediaId) {
        final String albumId = mediaId
                .substring(MediaBrowserConstants.MEDIA_ID_PREFIX_ALBUM.length());
        try {
            return queueProviderAlbums.fromAlbum(Long.parseLong(albumId));
        } catch (NumberFormatException e) {
            return Observable.error(
                    new NumberFormatException("Album id is not a number " + albumId));
        }
    }

    @NonNull
    private Observable<List<Media>> queueSourceFromGenreId(@NonNull final String mediaId) {
        final String genreId = mediaId
                .substring(MediaBrowserConstants.MEDIA_ID_PREFIX_GENRE.length());
        try {
            return queueProviderGenres.fromGenre(Long.parseLong(genreId));
        } catch (NumberFormatException e) {
            return Observable.error(
                    new NumberFormatException("Genre id is not a number " + genreId));
        }
    }
}
