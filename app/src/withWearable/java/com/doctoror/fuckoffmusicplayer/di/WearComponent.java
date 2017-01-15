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

import com.doctoror.fuckoffmusicplayer.wear.WearableListenerServiceImpl;
import com.doctoror.fuckoffmusicplayer.wear.WearableMediaPlaybackReporter;
import com.doctoror.fuckoffmusicplayer.wear.WearableSearchProviderService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Wear Component
 */
@Singleton
@Component(modules = {
        AppContextModule.class,
        MediaStoreProvidersModule.class,
        MediaModule.class
})
public interface WearComponent {

    void inject(WearableListenerServiceImpl target);

    void inject(WearableSearchProviderService target);

    void inject(WearableMediaPlaybackReporter target);

}
