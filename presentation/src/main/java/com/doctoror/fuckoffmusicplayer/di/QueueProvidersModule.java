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
package com.doctoror.fuckoffmusicplayer.di;

import android.content.ContentResolver;

import androidx.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderAlbumsMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderArtistsMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderFilesMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderGenresMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderMediaBrowserImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderPlaylistsMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderRandomMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderRecentlyScannedMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderSearchImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderTracksMediaStore;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderFiles;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderGenres;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderMediaBrowser;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderPlaylists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRandom;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderSearch;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;

import dagger.Module;
import dagger.Provides;

@Module
public final class QueueProvidersModule {

    @Provides
    QueueProviderAlbums provideQueueProviderAlbums(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderAlbumsMediaStore(mediaProvider);
    }

    @Provides
    QueueProviderArtists provideQueueProviderArtists(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderArtistsMediaStore(mediaProvider);
    }

    @Provides
    QueueProviderGenres provideQueueProviderGenres(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderGenresMediaStore(mediaProvider);
    }

    @Provides
    QueueProviderFiles provideQueueProviderFiles(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderFilesMediaStore(mediaProvider);
    }

    @Provides
    QueueProviderMediaBrowser provideQueueProviderMediaBrowser(
            @NonNull final QueueProviderAlbums queueProviderAlbums,
            @NonNull final QueueProviderGenres queueProviderGenres,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned,
            @NonNull final QueueProviderRandom queueProviderRandom) {
        return new QueueProviderMediaBrowserImpl(
                queueProviderAlbums,
                queueProviderGenres,
                queueProviderRecentlyScanned,
                queueProviderRandom);
    }

    @Provides
    QueueProviderPlaylists provideQueueProviderPlaylists(
            @NonNull final ContentResolver resolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderPlaylistsMediaStore(resolver, mediaProvider);
    }

    @Provides
    QueueProviderRandom provideQueueProviderRandom(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderRandomMediaStore(mediaProvider);
    }

    @Provides
    QueueProviderRecentlyScanned provideQueueProviderRecentlyScanned(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderRecentlyScannedMediaStore(mediaProvider);
    }

    @Provides
    QueueProviderSearch provideQueueProviderSearch(
            @NonNull final QueueProviderArtists artistPlaylistFactory,
            @NonNull final QueueProviderAlbums albumPlaylistFactory,
            @NonNull final QueueProviderTracks tracksQueueProvide) {
        return new QueueProviderSearchImpl(
                artistPlaylistFactory, albumPlaylistFactory, tracksQueueProvide);
    }

    @Provides
    QueueProviderTracks provideQueueProviderTracks(
            @NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderTracksMediaStore(contentResolver, mediaProvider);
    }
}
