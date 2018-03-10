/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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

import com.doctoror.fuckoffmusicplayer.data.effects.AudioEffectsImpl;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackParamsImpl;
import com.doctoror.fuckoffmusicplayer.data.player.MediaPlayerFactoryImpl;
import com.doctoror.fuckoffmusicplayer.data.reporter.PlaybackReporterFactoryImpl;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackNotificationFactoryImpl;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControlImpl;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class EngineModule {

    @Provides
    @Singleton
    AudioEffects provideAudioEffects(@NonNull final Context context) {
        return new AudioEffectsImpl(context);
    }

    @Provides
    @Singleton
    MediaPlayerFactory provideMediaPlayerFactory() {
        return new MediaPlayerFactoryImpl();
    }

    @Provides
    @Singleton
    PlaybackParams providePlaybackParams(@NonNull final Context context) {
        return new PlaybackParamsImpl(context);
    }

    @Provides
    @Singleton
    PlaybackServiceControl providePlaybackServiceControl(@NonNull final Context context) {
        return new PlaybackServiceControlImpl(context);
    }

    @Provides
    @Singleton
    PlaybackReporterFactory providePlaybackReporterFactory(
            @NonNull final Context context,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final Settings settings,
            @NonNull final PlaybackData playbackData) {
        return new PlaybackReporterFactoryImpl(context, albumThumbHolder, settings, playbackData);
    }

    @Provides
    @Singleton
    PlaybackNotificationFactory providePlaybackNotificationFactory() {
        return new PlaybackNotificationFactoryImpl();
    }
}
