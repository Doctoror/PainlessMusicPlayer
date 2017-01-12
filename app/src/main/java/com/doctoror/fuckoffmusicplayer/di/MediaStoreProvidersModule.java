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

import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.albums.MediaStoreAlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.artists.ArtistsProvider;
import com.doctoror.fuckoffmusicplayer.db.artists.MediaStoreArtistsProvider;
import com.doctoror.fuckoffmusicplayer.db.genres.GenresProvider;
import com.doctoror.fuckoffmusicplayer.db.genres.MediaStoreGenresProvider;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistsProvider;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistsProviderMediaStore;
import com.doctoror.fuckoffmusicplayer.db.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.media.manager.MediaManager;
import com.doctoror.fuckoffmusicplayer.media.manager.MediaManagerFactory;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */
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
            @NonNull final RecentPlaylistsManager recentPlaylistsManager) {
        return new MediaStoreAlbumsProvider(resolver, recentPlaylistsManager);
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
    PlaylistsProvider providePlaylistsProvider(@NonNull final ContentResolver resolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistsProviderMediaStore(resolver, mediaProvider);
    }

    @Provides
    @Singleton
    MediaManager provideMediaManager(@NonNull final ContentResolver resolver) {
        return MediaManagerFactory.getDefault(resolver);
    }

}
