package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.albums.MediaStoreAlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.artists.ArtistsProvider;
import com.doctoror.fuckoffmusicplayer.db.artists.MediaStoreArtistsProvider;
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

}
