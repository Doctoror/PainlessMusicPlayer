package com.doctoror.fuckoffmusicplayer.di;

import android.app.Service;
import android.support.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackServiceImpl;
import com.doctoror.fuckoffmusicplayer.di.scopes.ServiceScope;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceViewImpl;

import dagger.Module;
import dagger.Provides;

@Module
public final class PlaybackServiceModule {

    @Provides
    @ServiceScope
    PlaybackService providePlaybackService(
            @NonNull final Service service,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final AudioEffects audioEffects,
            @NonNull final MediaPlayerFactory mediaPlayerFactory,
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackParams playbackParams,
            @NonNull final PlaybackReporterFactory playbackReporterFactory,
            @NonNull final PlaybackServiceView playbackServicePresenter,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned) {
        return new PlaybackServiceImpl(
                service,
                albumThumbHolder,
                audioEffects,
                mediaPlayerFactory,
                mediaSessionHolder,
                playbackData,
                playbackInitializer,
                playbackParams,
                playbackReporterFactory,
                playbackServicePresenter,
                queueProviderRecentlyScanned,
                service::stopSelf);
    }

    @Provides
    @ServiceScope
    PlaybackServiceView providePlaybackServiceView(
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackNotificationFactory playbackNotificationFactory,
            @NonNull final Service service) {
        return new PlaybackServiceViewImpl(
                mediaSessionHolder, playbackNotificationFactory, service);
    }
}
