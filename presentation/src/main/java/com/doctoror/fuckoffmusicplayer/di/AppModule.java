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

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.commons.reactivex.SchedulersProviderImpl;
import com.doctoror.fuckoffmusicplayer.data.settings.SettingsImpl;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger context module
 */
@Module
final class AppModule {

    @NonNull
    private final Context context;

    AppModule(@NonNull final Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return context;
    }

    @Provides
    @Singleton
    Resources provideResources() {
        return context.getResources();
    }

    @Provides
    @Singleton
    ContentResolver provideContentResolver() {
        return context.getContentResolver();
    }

    @Provides
    @Singleton
    SchedulersProvider provideSchedulersProvider() {
        return new SchedulersProviderImpl();
    }

    @Provides
    @Singleton
    Settings provideSettings(@NonNull final Context context) {
        return new SettingsImpl(context);
    }
}
