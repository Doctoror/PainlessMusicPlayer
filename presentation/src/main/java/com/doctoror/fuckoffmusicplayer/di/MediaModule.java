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

import androidx.annotation.NonNull;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.fuckoffmusicplayer.data.media.AlbumMediaIdsProviderImpl;
import com.doctoror.fuckoffmusicplayer.data.media.AlbumThumbHolderImpl;
import com.doctoror.fuckoffmusicplayer.data.media.CurrentMediaProviderImpl;
import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.playlist.RecentActivityManagerImpl;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;

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
    AlbumThumbHolder provideAlbumThumbHolder(
            @NonNull final Context context,
            @NonNull final SchedulersProvider schedulersProvider) {
        return new AlbumThumbHolderImpl(context, schedulersProvider);
    }

    @Provides
    CurrentMediaProvider provideCurrentMediaProvider(
            @NonNull final PlaybackData playbackData) {
        return new CurrentMediaProviderImpl(playbackData);
    }

    @Provides
    @Singleton
    RecentActivityManager provideRecentActivityManager(
            @NonNull final Context context,
            @NonNull final SchedulersProvider schedulersProvider) {
        return new RecentActivityManagerImpl(context, schedulersProvider);
    }

    @Provides
    MediaProvider provideMediaProvider(@NonNull final ContentResolver contentResolver) {
        return provideMediaStoreMediaProvider(contentResolver);
    }

    @Provides
    MediaStoreMediaProvider provideMediaStoreMediaProvider(
            @NonNull final ContentResolver contentResolver) {
        return new MediaStoreMediaProvider(contentResolver);
    }
}
