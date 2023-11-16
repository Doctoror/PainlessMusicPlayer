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

import androidx.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveAlbumFromQueueUseCase;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveMediasFromCurrentQueueUseCase;

import dagger.Module;
import dagger.Provides;

@Module
final class QueueModule {

    @Provides
    RemoveAlbumFromQueueUseCase provideRemoveAlbumFromQueueUseCase(
            @NonNull final AlbumMediaIdsProvider albumMediaIdsProvider,
            @NonNull final RemoveMediasFromCurrentQueueUseCase useCase) {
        return new RemoveAlbumFromQueueUseCase(albumMediaIdsProvider, useCase);
    }

    @Provides
    RemoveMediasFromCurrentQueueUseCase provideRemoveMediasFromCurrentQueueUseCase(
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackInitializer playbackInitializer) {
        return new RemoveMediasFromCurrentQueueUseCase(playbackData, playbackInitializer);
    }
}
