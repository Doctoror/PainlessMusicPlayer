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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

import com.doctoror.fuckoffmusicplayer.data.media.session.MediaSessionCallback;
import com.doctoror.fuckoffmusicplayer.data.media.session.MediaSessionFactoryImpl;
import com.doctoror.fuckoffmusicplayer.data.media.session.MediaSessionHolderImpl;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.MediaIdPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.SearchPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderMediaBrowser;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.presentation.nowplaying.NowPlayingActivity;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class MediaSessionModule {

    @Provides
    MediaSessionCompat.Callback provideMediaSessionCallback(
            @NonNull final QueueProviderMediaBrowser queueProviderMediaBrowser,
            @NonNull final MediaIdPlaybackInitializer mediaIdPlaybackInitializer,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackServiceControl playbackServiceControl,
            @NonNull final SearchPlaybackInitializer searchPlaybackInitializer) {
        return new MediaSessionCallback(
                mediaIdPlaybackInitializer,
                playbackInitializer,
                playbackServiceControl,
                queueProviderMediaBrowser,
                searchPlaybackInitializer);
    }

    @Provides
    MediaSessionFactory provideMediaSessionFactory(
            @NonNull final Context context,
            @NonNull final MediaSessionCompat.Callback mediaSessionCallback) {
        return new MediaSessionFactoryImpl(context, NowPlayingActivity.class, mediaSessionCallback);
    }

    @Provides
    @Singleton
    MediaSessionHolder provideMediaSesionHolder(
            @NonNull final MediaSessionFactory mediaSessionFactory,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackReporterFactory playbackReporterFactory) {
        return new MediaSessionHolderImpl(
                mediaSessionFactory, playbackData, playbackReporterFactory);
    }
}
