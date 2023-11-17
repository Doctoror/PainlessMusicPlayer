/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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

import androidx.annotation.NonNull;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackServiceImpl;
import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackControllerProvider;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitAudioFocus;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitAudioNoisyManagement;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitMediaPositionUpdater;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitMediaSession;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitPlayCurrentOrNewQueue;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitPlayMediaFromQueue;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitQueueMonitor;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitReporter;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitStopTimeout;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitWakeLock;
import com.doctoror.fuckoffmusicplayer.di.scopes.ServiceScope;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackAndroidService;
import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackServiceViewImpl;

import dagger.Module;
import dagger.Provides;

@Module
public final class PlaybackServiceModule {

    @Provides
    @ServiceScope
    MediaPlayer provideMediaPlayer(@NonNull final MediaPlayerFactory mediaPlayerFactory) {
        return mediaPlayerFactory.newMediaPlayer();
    }

    @Provides
    @ServiceScope
    PlaybackControllerProvider providePlaybackControllerProvider(
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackParams playbackParams,
            @NonNull final PlaybackServiceUnitPlayMediaFromQueue psUnitPlayFromQueue,
            @NonNull final Runnable stopAction) {
        return new PlaybackControllerProvider(
                playbackData, playbackParams, psUnitPlayFromQueue, stopAction);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitAudioFocus providePlaybackServiceUnitAudioFocus(
            @NonNull final PlaybackAndroidService service) {
        return new PlaybackServiceUnitAudioFocus(service);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitAudioNoisyManagement providePlaybackServiceUnitAudioNoisyManagement(
            @NonNull final PlaybackAndroidService service,
            @NonNull final Runnable stopAction) {
        return new PlaybackServiceUnitAudioNoisyManagement(service, stopAction);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitMediaPositionUpdater providePlaybackServiceUnitMediaPositionUpdater(
            @NonNull final MediaPlayer mediaPlayer,
            @NonNull final PlaybackData playbackData,
            @NonNull final SchedulersProvider schedulers) {
        return new PlaybackServiceUnitMediaPositionUpdater(mediaPlayer, playbackData, schedulers);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitMediaSession providePlaybackServiceUnitMediaSession(
            @NonNull final MediaSessionHolder mediaSessionHolder) {
        return new PlaybackServiceUnitMediaSession(mediaSessionHolder);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitPlayCurrentOrNewQueue providePlaybackServiceUnitPlayCurrentOrNewQueue(
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackServiceUnitPlayMediaFromQueue psUnitPlayMediaFromQueue,
            @NonNull final QueueProviderRecentlyScanned queueProviderRecentlyScanned,
            @NonNull final SchedulersProvider schedulersProvider) {
        return new PlaybackServiceUnitPlayCurrentOrNewQueue(
                playbackData,
                playbackInitializer,
                psUnitPlayMediaFromQueue,
                queueProviderRecentlyScanned,
                schedulersProvider);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitPlayMediaFromQueue providePlaybackServiceUnitPlayMediaFromQueue(
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final MediaPlayer mediaPlayer,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackServiceUnitAudioFocus unitAudioFocus,
            @NonNull final PlaybackServiceUnitReporter unitReporter) {
        return new PlaybackServiceUnitPlayMediaFromQueue(
                currentMediaProvider,
                mediaPlayer,
                playbackData,
                unitAudioFocus,
                unitReporter);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitQueueMonitor providePlaybackServiceUnitQueueMonitor(
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final PlaybackControllerProvider playbackControllerProvider,
            @NonNull final PlaybackData playbackData,
            @NonNull final SchedulersProvider schedulersProvider,
            @NonNull final Runnable stopAction) {
        return new PlaybackServiceUnitQueueMonitor(
                albumThumbHolder,
                playbackControllerProvider,
                playbackData,
                schedulersProvider,
                stopAction);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitReporter providePlaybackServiceUnitReporter(
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackReporterFactory playbackReporterFactory) {
        return new PlaybackServiceUnitReporter(
                currentMediaProvider, mediaSessionHolder, playbackReporterFactory);
    }


    @Provides
    @ServiceScope
    PlaybackServiceUnitStopTimeout providePlaybackServiceUnitStopTimeout(
            @NonNull final Runnable stopAction,
            @NonNull final SchedulersProvider schedulersProvider) {
        return new PlaybackServiceUnitStopTimeout(stopAction, schedulersProvider);
    }

    @Provides
    @ServiceScope
    PlaybackServiceUnitWakeLock providePlaybackServiceUnitWakeLock(
            @NonNull final PlaybackAndroidService service) {
        return new PlaybackServiceUnitWakeLock(service);
    }

    @Provides
    @ServiceScope
    PlaybackService providePlaybackService(
            @NonNull final PlaybackAndroidService service,
            @NonNull final AlbumArtFetcher albumArtFetcher,
            @NonNull final AudioEffects audioEffects,
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final MediaPlayer mediaPlayer,
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackControllerProvider playbackControllerProvider,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackServiceUnitAudioFocus unitAudioFocus,
            @NonNull final PlaybackServiceUnitAudioNoisyManagement unitAudioNoisyManagement,
            @NonNull final PlaybackServiceUnitMediaPositionUpdater unitMediaPositionUpdater,
            @NonNull final PlaybackServiceUnitMediaSession unitMediaSession,
            @NonNull final PlaybackServiceUnitPlayCurrentOrNewQueue unitPlayCurrentOrNewQueue,
            @NonNull final PlaybackServiceUnitPlayMediaFromQueue unitPlayMediaFromQueue,
            @NonNull final PlaybackServiceUnitQueueMonitor unitQueueMonitor,
            @NonNull final PlaybackServiceUnitReporter unitReporter,
            @NonNull final PlaybackServiceUnitStopTimeout unitStopTimeout,
            @NonNull final PlaybackServiceUnitWakeLock unitWakeLock,
            @NonNull final PlaybackServiceView playbackServiceView,
            @NonNull final Runnable stopAction) {
        return new PlaybackServiceImpl(
                service,
                albumArtFetcher,
                audioEffects,
                currentMediaProvider,
                mediaPlayer,
                mediaSessionHolder,
                playbackControllerProvider,
                playbackData,
                unitAudioFocus,
                unitAudioNoisyManagement,
                unitMediaPositionUpdater,
                unitMediaSession,
                unitPlayCurrentOrNewQueue,
                unitPlayMediaFromQueue,
                unitQueueMonitor,
                unitReporter,
                unitStopTimeout,
                unitWakeLock,
                playbackServiceView,
                stopAction);
    }

    @Provides
    @ServiceScope
    PlaybackServiceView providePlaybackServiceView(
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackNotificationFactory playbackNotificationFactory,
            @NonNull final PlaybackAndroidService service) {
        return new PlaybackServiceViewImpl(
                mediaSessionHolder, playbackNotificationFactory, service);
    }

    @Provides
    @ServiceScope
    Runnable provideStopAction(@NonNull final PlaybackAndroidService service) {
        return service::stopSelf;
    }
}
