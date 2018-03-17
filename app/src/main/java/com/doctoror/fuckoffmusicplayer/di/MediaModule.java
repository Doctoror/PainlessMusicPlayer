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
package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.data.media.AlbumMediaIdsProviderImpl;
import com.doctoror.fuckoffmusicplayer.data.media.AlbumThumbHolderImpl;
import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.media.session.MediaSessionCallback;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackDataImpl;
import com.doctoror.fuckoffmusicplayer.data.playlist.RecentActivityManagerImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderAlbumsMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderArtistsMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderFilesMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderGenresMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderRandomMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderRecentlyScannedMediaStore;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderTracksMediaStore;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.MediaIdPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.SearchPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderFiles;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderGenres;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRandom;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.MediaBrowserQueueProvider;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Media module
 */
@Module
final class MediaModule {

    @Provides
    AlbumMediaIdsProvider provideAlbumMediaIdsProvider(
            @NonNull final ContentResolver contentResolver) {
        return new AlbumMediaIdsProviderImpl(contentResolver);
    }

    @Provides
    @Singleton
    RecentActivityManager provideRecentActivityManager(@NonNull final Context context) {
        return RecentActivityManagerImpl.getInstance(context);
    }

    @Provides
    @Singleton
    MediaProvider provideMediaProvider(@NonNull final ContentResolver contentResolver) {
        return new MediaStoreMediaProvider(contentResolver);
    }

    @Provides
    @Singleton
    MediaStoreMediaProvider provideMediaStoreMediaProvider(
            @NonNull final ContentResolver contentResolver) {
        return new MediaStoreMediaProvider(contentResolver);
    }

    @Provides
    MediaSessionCompat.Callback provideMediaSessionCallback(
            @NonNull final MediaBrowserQueueProvider mediaBrowserQueueProvider,
            @NonNull final MediaIdPlaybackInitializer mediaIdPlaybackInitializer,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackServiceControl playbackServiceControl,
            @NonNull final SearchPlaybackInitializer searchPlaybackInitializer) {
        return new MediaSessionCallback(
                mediaBrowserQueueProvider,
                mediaIdPlaybackInitializer,
                playbackInitializer,
                playbackServiceControl,
                searchPlaybackInitializer);
    }

    @Provides
    @Singleton
    QueueProviderArtists provideQueueProviderArtists(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderArtistsMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    QueueProviderAlbums provideQueueProviderAlbums(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderAlbumsMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    QueueProviderGenres provideQueueProviderGenres(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderGenresMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    QueueProviderTracks provideQueueProviderTracks(
            @NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderTracksMediaStore(contentResolver, mediaProvider);
    }

    @Provides
    @Singleton
    QueueProviderFiles provideQueueProviderFiles(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderFilesMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    QueueProviderRandom provideQueueProviderRandom(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderRandomMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    QueueProviderRecentlyScanned provideQueueProviderRecentlyScanned(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderRecentlyScannedMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    PlaybackData providePlaybackData(@NonNull final Context context,
            @NonNull final RecentActivityManager recentActivityManager) {
        return new PlaybackDataImpl(context, recentActivityManager);
    }

    @Provides
    @Singleton
    AlbumThumbHolder provideAlbumThumbHolder(@NonNull final Context context) {
        return new AlbumThumbHolderImpl(context);
    }
}
