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

import android.content.Context;
import android.support.annotation.NonNull;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.fuckoffmusicplayer.data.effects.AudioEffectsImpl;
import com.doctoror.fuckoffmusicplayer.data.player.MediaPlayerFactoryImpl;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class EngineModule {

    @Provides
    @Singleton
    AudioEffects provideAudioEffects(
            @NonNull final Context context,
            @NonNull final SchedulersProvider schedulersProvider) {
        return new AudioEffectsImpl(context, schedulersProvider);
    }

    @Provides
    MediaPlayerFactory provideMediaPlayerFactory() {
        return new MediaPlayerFactoryImpl();
    }
}
