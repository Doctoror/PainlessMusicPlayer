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

import com.doctoror.fuckoffmusicplayer.data.albums.MediaStoreAlbumsProvider;
import com.doctoror.fuckoffmusicplayer.data.artists.MediaStoreArtistsProvider;
import com.doctoror.fuckoffmusicplayer.data.genres.MediaStoreGenresProvider;
import com.doctoror.fuckoffmusicplayer.data.media.MediaManagerFile;
import com.doctoror.fuckoffmusicplayer.data.media.MediaManagerMediaStore;
import com.doctoror.fuckoffmusicplayer.data.media.MediaManagerSet;
import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.queue.QueueProviderPlaylistsMediaStore;
import com.doctoror.fuckoffmusicplayer.data.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.domain.artists.ArtistsProvider;
import com.doctoror.fuckoffmusicplayer.domain.genres.GenresProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaManager;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderPlaylists;
import com.doctoror.fuckoffmusicplayer.domain.tracks.TracksProvider;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class MediaStoreProvidersModule {

    @Provides
    @Singleton
    ArtistsProvider provideArtistsProvider(@NonNull final ContentResolver resolver) {
        return new MediaStoreArtistsProvider(resolver);
    }

    @Provides
    @Singleton
    AlbumsProvider provideAlbumsProvider(@NonNull final ContentResolver resolver,
            @NonNull final RecentActivityManager recentActivityManager) {
        return new MediaStoreAlbumsProvider(resolver, recentActivityManager);
    }

    @Provides
    @Singleton
    GenresProvider provideGenresProvider(@NonNull final ContentResolver resolver) {
        return new MediaStoreGenresProvider(resolver);
    }

    @Provides
    @Singleton
    TracksProvider provideTracksProvider(@NonNull final ContentResolver resolver) {
        return new MediaStoreTracksProvider(resolver);
    }

    @Provides
    @Singleton
    QueueProviderPlaylists providePlaylistsProvider(@NonNull final ContentResolver resolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new QueueProviderPlaylistsMediaStore(resolver, mediaProvider);
    }

    @Provides
    @Singleton
    MediaManager provideMediaManager(
            @NonNull final ContentResolver resolver,
            @NonNull final AlbumMediaIdsProvider albumMediaIdsProvider) {
        return new MediaManagerSet(
                new MediaManagerFile(resolver),
                new MediaManagerMediaStore(resolver, albumMediaIdsProvider));
    }
}
