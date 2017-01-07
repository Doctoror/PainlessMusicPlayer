package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.db.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbums;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderArtists;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderFiles;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderGenres;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbumsMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderArtistsMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderFilesMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderGenresMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRandomMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRecentlyScannedMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderTracksMediaStore;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRandom;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderTracks;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManagerImpl;

import android.content.ContentResolver;
import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Playlists module
 */
@Module
final class PlaylistsModule {

    @Provides
    @Singleton
    RecentPlaylistsManager provideRecentPlaylistsManager(@NonNull final Context context) {
        return RecentPlaylistsManagerImpl.getInstance(context);
    }

    @Provides
    @Singleton
    MediaProvider provideMediaProvider(@NonNull final ContentResolver contentResolver) {
        return new MediaStoreMediaProvider(contentResolver);
    }

    @Provides
    @Singleton
    MediaStoreMediaProvider provideMediaStoreMediaProvider(
            @NonNull final ContentResolver contentResolver) {
        return new MediaStoreMediaProvider(contentResolver);
    }

    @Provides
    @Singleton
    PlaylistProviderArtists providePlaylistProviderArtists(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderArtistsMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    PlaylistProviderAlbums providePlaylistProviderAlbums(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderAlbumsMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    PlaylistProviderGenres providePlaylistProviderGenres(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderGenresMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    PlaylistProviderTracks providePlaylistProviderTracks(
            @NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderTracksMediaStore(contentResolver, mediaProvider);
    }

    @Provides
    @Singleton
    PlaylistProviderFiles providePlaylistProviderFiles(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderFilesMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    PlaylistProviderRandom providePlaylistProviderRandom(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderRandomMediaStore(mediaProvider);
    }

    @Provides
    @Singleton
    PlaylistProviderRecentlyScanned providePlaylistProviderRecentlyScanned(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new PlaylistProviderRecentlyScannedMediaStore(mediaProvider);
    }
}
