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

import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderSearch;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import io.reactivex.Observable;

public final class QueueProviderSearchImpl implements QueueProviderSearch {

    private final QueueProviderArtists artistPlaylistFactory;
    private final QueueProviderAlbums albumPlaylistFactory;
    private final QueueProviderTracks tracksQueueProvider;

    public QueueProviderSearchImpl(
            @NonNull final QueueProviderArtists artistPlaylistFactory,
            @NonNull final QueueProviderAlbums albumPlaylistFactory,
            @NonNull final QueueProviderTracks tracksQueueProvider) {
        this.artistPlaylistFactory = artistPlaylistFactory;
        this.albumPlaylistFactory = albumPlaylistFactory;
        this.tracksQueueProvider = tracksQueueProvider;
    }

    @NonNull
    @Override
    public Observable<List<Media>> queueSourceFromSearch(
            @NonNull final String query,
            @Nullable final Bundle extras) {
        return Observable.fromCallable(() -> extractParams(extras))
                .flatMap((params) -> {
                    Observable<List<Media>> source = null;
                    if (params.isArtistFocus) {
                        source = artistPlaylistFactory.fromArtistSearch(
                                TextUtils.isEmpty(params.artist) ? query : params.artist);
                    } else if (params.isAlbumFocus) {
                        source = albumPlaylistFactory.fromAlbumSearch(
                                TextUtils.isEmpty(params.album) ? query : params.album);
                    }

                    if (source == null) {
                        source = tracksQueueProvider.fromTracksSearch(query);
                    } else {
                        source = source.flatMap(queue -> queue.isEmpty()
                                ? tracksQueueProvider.fromTracksSearch(query)
                                : Observable.just(queue));
                    }

                    return source;
                });
    }

    @NonNull
    private static Params extractParams(@Nullable final Bundle extras) {
        boolean isArtistFocus = false;
        boolean isAlbumFocus = false;

        String artist = null;
        String album = null;

        String mediaFocus = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
        if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
            isArtistFocus = true;
            artist = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
        } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
            isAlbumFocus = true;
            album = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
        }
        return new Params(isArtistFocus, isAlbumFocus, artist, album);
    }

    private static final class Params {

        final boolean isArtistFocus;
        final boolean isAlbumFocus;

        @Nullable
        final String artist;

        @Nullable
        final String album;

        Params(final boolean isArtistFocus,
                final boolean isAlbumFocus,
                @Nullable final String artist,
                @Nullable final String album) {
            this.isArtistFocus = isArtistFocus;
            this.isAlbumFocus = isAlbumFocus;
            this.artist = artist;
            this.album = album;
        }
    }
}
