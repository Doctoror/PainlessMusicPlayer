package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackDataImpl;
import com.doctoror.fuckoffmusicplayer.data.playback.initializer.MediaIdPlaybackInitializerImpl;
import com.doctoror.fuckoffmusicplayer.data.playback.initializer.PlaybackInitializerImpl;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.MediaIdPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
final class PlaybackModule {

    @Provides
    @Singleton
    PlaybackData providePlaybackData(@NonNull final Context context,
            @NonNull final RecentActivityManager recentActivityManager) {
        return new PlaybackDataImpl(context, recentActivityManager);
    }

    @Provides
    MediaIdPlaybackInitializer mediaIdPlaybackInitializer(
            @NonNull final Resources resources,
            @NonNull final MediaProvider mediaProvider,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackServiceControl playbackServiceControl) {
        return new MediaIdPlaybackInitializerImpl(
                resources.getText(R.string.No_media_found),
                mediaProvider,
                playbackInitializer,
                playbackData,
                playbackServiceControl);
    }

    @Provides
    PlaybackInitializer providePlaybackInitializer(
            @NonNull final PlaybackServiceControl control,
            @NonNull final PlaybackData playbackData) {
        return new PlaybackInitializerImpl(control, playbackData);
    }
}
