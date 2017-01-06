package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManagerImpl;

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

}
