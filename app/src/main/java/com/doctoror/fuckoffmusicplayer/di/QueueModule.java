package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.data.playback.initializer.PlaybackInitializerImpl;
import com.doctoror.fuckoffmusicplayer.data.queue.provider.QueueFromSearchProviderImpl;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.QueueFromSearchProvider;

import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class QueueModule {

    @Provides
    @Singleton
    PlaybackInitializer providePlaybackInitializer(
            @NonNull final PlaybackServiceControl control,
            @NonNull final PlaybackData playbackData) {
        return new PlaybackInitializerImpl(control, playbackData);
    }

    @Provides
    @Singleton
    QueueFromSearchProvider provideQueueFromSearchProvider(
            @NonNull final QueueProviderArtists artistPlaylistFactory,
            @NonNull final QueueProviderAlbums albumPlaylistFactory,
            @NonNull final QueueProviderTracks tracksQueueProvide) {
        return new QueueFromSearchProviderImpl(
                artistPlaylistFactory, albumPlaylistFactory, tracksQueueProvide);
    }
}
