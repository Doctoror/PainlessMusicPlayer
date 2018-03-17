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

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackDataImpl;
import com.doctoror.fuckoffmusicplayer.data.playback.initializer.MediaIdPlaybackInitializerImpl;
import com.doctoror.fuckoffmusicplayer.data.playback.initializer.PlaybackInitializerImpl;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.MediaIdPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class PlaybackModule {

    @Provides
    @Singleton
    PlaybackData providePlaybackData(@NonNull final Context context,
            @NonNull final RecentActivityManager recentActivityManager) {
        return new PlaybackDataImpl(context, recentActivityManager);
    }

    @Provides
    MediaIdPlaybackInitializer mediaIdPlaybackInitializer(
            @NonNull final Resources resources,
            @NonNull final MediaProvider mediaProvider,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackServiceControl playbackServiceControl) {
        return new MediaIdPlaybackInitializerImpl(
                resources.getText(R.string.No_media_found),
                mediaProvider,
                playbackInitializer,
                playbackData,
                playbackServiceControl);
    }

    @Provides
    PlaybackInitializer providePlaybackInitializer(
            @NonNull final PlaybackServiceControl control,
            @NonNull final PlaybackData playbackData) {
        return new PlaybackInitializerImpl(control, playbackData);
    }
}
