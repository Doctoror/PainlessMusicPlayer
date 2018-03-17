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

import com.doctoror.fuckoffmusicplayer.data.queue.provider.MediaBrowserQueueProviderImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.provider.QueueFromSearchProviderImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.usecase.RemoveAlbumFromQueueUseCaseImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.usecase.RemoveMediasFromCurrentQueueUseCaseImpl;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderGenres;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRandom;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.MediaBrowserQueueProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.QueueFromSearchProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveAlbumFromQueueUseCase;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveMediasFromCurrentQueueUseCase;

import android.support.annotation.NonNull;

import dagger.Module;
import dagger.Provides;

@Module
final class QueueModule {

    @Provides
    MediaBrowserQueueProvider mediaBrowserQueueProvider(
            @NonNull final QueueProviderAlbums queueProviderAlbums,
            @NonNull final QueueProviderGenres queueProviderGenres,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned,
            @NonNull final QueueProviderRandom queueProviderRandom) {
        return new MediaBrowserQueueProviderImpl(
                queueProviderAlbums,
                queueProviderGenres,
                queueProviderRecentlyScanned,
                queueProviderRandom);
    }

    @Provides
    RemoveAlbumFromQueueUseCase provideRemoveAlbumFromQueueUseCase(
            @NonNull final AlbumMediaIdsProvider albumMediaIdsProvider,
            @NonNull final RemoveMediasFromCurrentQueueUseCase useCase) {
        return new RemoveAlbumFromQueueUseCaseImpl(albumMediaIdsProvider, useCase);
    }

    @Provides
    RemoveMediasFromCurrentQueueUseCase provideRemoveMediasFromCurrentQueueUseCase(
            @NonNull final PlaybackData playbackData) {
        return new RemoveMediasFromCurrentQueueUseCaseImpl(playbackData);
    }

    @Provides
    QueueFromSearchProvider provideQueueFromSearchProvider(
            @NonNull final QueueProviderArtists artistPlaylistFactory,
            @NonNull final QueueProviderAlbums albumPlaylistFactory,
            @NonNull final QueueProviderTracks tracksQueueProvide) {
        return new QueueFromSearchProviderImpl(
                artistPlaylistFactory, albumPlaylistFactory, tracksQueueProvide);
    }
}
