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

import android.support.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackServiceImpl;
import com.doctoror.fuckoffmusicplayer.di.scopes.ServiceScope;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackAndroidService;
import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackServiceViewImpl;

import dagger.Module;
import dagger.Provides;

@Module
public final class PlaybackServiceModule {

    @Provides
    @ServiceScope
    PlaybackService providePlaybackService(
            @NonNull final PlaybackAndroidService service,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final AudioEffects audioEffects,
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final MediaPlayerFactory mediaPlayerFactory,
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackParams playbackParams,
            @NonNull final PlaybackReporterFactory playbackReporterFactory,
            @NonNull final PlaybackServiceView playbackServicePresenter,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned) {
        return new PlaybackServiceImpl(
                service,
                albumThumbHolder,
                audioEffects,
                currentMediaProvider,
                mediaPlayerFactory,
                mediaSessionHolder,
                playbackData,
                playbackInitializer,
                playbackParams,
                playbackReporterFactory,
                playbackServicePresenter,
                queueProviderRecentlyScanned,
                service::stopSelf);
    }

    @Provides
    @ServiceScope
    PlaybackServiceView providePlaybackServiceView(
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackNotificationFactory playbackNotificationFactory,
            @NonNull final PlaybackAndroidService service) {
        return new PlaybackServiceViewImpl(
                mediaSessionHolder, playbackNotificationFactory, service);
    }
}
