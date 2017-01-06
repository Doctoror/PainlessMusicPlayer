package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.db.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.playlist.AlbumPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.ArtistPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.FilePlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.GenrePlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreAlbumPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreArtistPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreFilePlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreGenrePlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreRandomPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreRecentlyScannedPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.MediaStoreTrackPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.RandomPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.RecentlyScannedPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.playlist.TrackPlaylistFactory;
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
    ArtistPlaylistFactory provideArtistPlaylistFactory(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreArtistPlaylistFactory(mediaProvider);
    }

    @Provides
    @Singleton
    AlbumPlaylistFactory provideAlbumPlaylistFactory(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreAlbumPlaylistFactory(mediaProvider);
    }

    @Provides
    @Singleton
    GenrePlaylistFactory provideGenrePlaylistFactory(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreGenrePlaylistFactory(mediaProvider);
    }

    @Provides
    @Singleton
    TrackPlaylistFactory provideTracksPlaylistFactory(
            @NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreTrackPlaylistFactory(contentResolver, mediaProvider);
    }

    @Provides
    @Singleton
    FilePlaylistFactory provideFilePlaylistFactory(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreFilePlaylistFactory(mediaProvider);
    }

    @Provides
    @Singleton
    RandomPlaylistFactory provideRandomPlaylistFactory(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreRandomPlaylistFactory(mediaProvider);
    }

    @Provides
    @Singleton
    RecentlyScannedPlaylistFactory provideRecentlyScannedPlaylistFactory(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        return new MediaStoreRecentlyScannedPlaylistFactory(mediaProvider);
    }
}
