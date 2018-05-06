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
package com.doctoror.fuckoffmusicplayer.data.playback;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleOwner;
import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackControllerProvider;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.AudioFocusListener;
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
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerKt;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerListener;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;

import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_ERROR;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_IDLE;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_LOADING;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PAUSED;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PLAYING;

public final class PlaybackServiceImpl extends ServiceLifecycleOwner implements PlaybackService {

    private static final String TAG = "PlaybackServiceImpl";

    private final AudioEffects audioEffects;
    private final Context context;
    private final CurrentMediaProvider currentMediaProvider;
    private final MediaPlayer mediaPlayer;
    private final MediaSessionHolder mediaSessionHolder;
    private final PlaybackControllerProvider playbackControllerProvider;
    private final PlaybackData playbackData;
    private final PlaybackServiceView playbackServiceView;

    private final PlaybackServiceUnitAudioFocus unitAudioFocus;
    private final PlaybackServiceUnitAudioNoisyManagement unitAudioNoisyManagement;
    private final PlaybackServiceUnitMediaPositionUpdater unitMediaPositionUpdater;
    private final PlaybackServiceUnitMediaSession unitMediaSession;
    private final PlaybackServiceUnitPlayCurrentOrNewQueue unitPlayCurrentOrNewQueue;
    private final PlaybackServiceUnitPlayMediaFromQueue unitPlayMediaFromQueue;
    private final PlaybackServiceUnitQueueMonitor unitQueueMonitor;
    private final PlaybackServiceUnitReporter unitReporter;
    private final PlaybackServiceUnitStopTimeout unitStopTimeout;
    private final PlaybackServiceUnitWakeLock unitWakeLock;

    private final Runnable stopAction;

    private CharSequence errorMessage;
    private PlaybackState state = STATE_IDLE;

    private boolean isDestroying;

    public PlaybackServiceImpl(
            @NonNull final Context context,
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
        this.context = context;
        this.audioEffects = audioEffects;
        this.currentMediaProvider = currentMediaProvider;
        this.mediaPlayer = mediaPlayer;
        this.mediaSessionHolder = mediaSessionHolder;
        this.playbackControllerProvider = playbackControllerProvider;
        this.playbackData = playbackData;
        this.playbackServiceView = playbackServiceView;
        this.stopAction = stopAction;

        this.unitAudioFocus = unitAudioFocus;
        this.unitMediaSession = unitMediaSession;
        this.unitAudioNoisyManagement = unitAudioNoisyManagement;
        this.unitMediaPositionUpdater = unitMediaPositionUpdater;
        this.unitPlayCurrentOrNewQueue = unitPlayCurrentOrNewQueue;
        this.unitPlayMediaFromQueue = unitPlayMediaFromQueue;
        this.unitQueueMonitor = unitQueueMonitor;
        this.unitReporter = unitReporter;
        this.unitStopTimeout = unitStopTimeout;
        this.unitWakeLock = unitWakeLock;

        unitAudioFocus.setListener(new AudioFocusListenerImpl());
        init();
    }

    private void init() {
        registerLifecycleObserver(unitWakeLock);

        registerLifecycleObserver(unitAudioFocus);
        registerLifecycleObserver(unitAudioNoisyManagement);
        registerLifecycleObserver(unitMediaPositionUpdater);
        registerLifecycleObserver(unitQueueMonitor);

        // Ensure the ordering of these two does not change
        registerLifecycleObserver(unitMediaSession);
        registerLifecycleObserver(unitReporter);

        // Must be called after all lifecycle observers registered
        notifyLifecycleStateOnCreate();

        isDestroying = false;
        errorMessage = null;

        mediaPlayer.setListener(new MediaPlayerListenerImpl());
        mediaPlayer.init(context);
    }

    @Override
    public void playPause() {
        switch (state) {
            case STATE_PLAYING:
                pause();
                break;

            case STATE_PAUSED:
                playCurrent();
                break;

            case STATE_IDLE:
            case STATE_ERROR:
                playCurrentOrNewQueue();
                break;

            case STATE_LOADING:
            default:
                // Do nothing
                break;
        }
    }

    private void playCurrentOrNewQueue() {
        Completable.fromAction(unitPlayCurrentOrNewQueue::playCurrentOrNewQueue)
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    private void playCurrent() {
        final List<Media> queue = playbackData.getQueue();
        if (queue == null) {
            Log.w(TAG, "Play requested for null queue");
            return;
        }
        play(queue, playbackData.getQueuePosition());
    }

    @Override
    public void play(@NonNull final List<Media> queue, final int position) {
        if (queue.isEmpty()) {
            Log.w(TAG, "Play requested for empty queue");
            return;
        }
        Completable.fromAction(() -> unitPlayMediaFromQueue.play(queue, position))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void playAnything() {
        playCurrentOrNewQueue();
    }

    @Override
    public void pause() {
        pauseTemporary();
        unitAudioFocus.abandonAudioFocus();
        unitStopTimeout.initializeStopTimer();
    }

    @Override
    public void stop() {
        stopAction.run();
    }

    @Override
    public void stopWithError(@Nullable final CharSequence errorMessage) {
        this.errorMessage = errorMessage;
        stopAction.run();
    }

    @Override
    public void playPrev() {
        Completable.fromAction(() -> playbackControllerProvider.obtain().playPrev())
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void playNext() {
        playNextInner(true);
    }

    @Override
    public void seek(final long position) {
        mediaPlayer.seekTo(position);
    }

    /**
     * Pauses, but does not abandon audio focus and does not schedule the stop timer.
     */
    private void pauseTemporary() {
        if (state == STATE_PLAYING) {
            mediaPlayer.pause();
            setState(STATE_PAUSED);
            showNotification();
        }
    }

    private void playNextInner(final boolean isUserAction) {
        // Set this state but do not notify listeners so that onPlaybackFinished will not invoke playNext
        // again
        state = STATE_LOADING;
        Completable.fromAction(() -> playbackControllerProvider.obtain().playNext(isUserAction))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void restart() {
        if (state == STATE_PLAYING) {
            mediaPlayer.stop();
        }
        playbackData.setMediaPosition(0);
        playCurrent();
    }

    @Nullable
    private MediaSessionCompat getMediaSession() {
        return mediaSessionHolder != null ? mediaSessionHolder.getMediaSession() : null;
    }

    private void showNotification() {
        final Media media = currentMediaProvider.getCurrentMedia();
        if (media != null) {
            final MediaSessionCompat mediaSession = getMediaSession();
            if (mediaSession != null) {
                Schedulers.io().scheduleDirect(
                        () -> playbackServiceView.startForeground(media, state));
            }
        }
    }

    @Override
    public void destroy() {
        isDestroying = true;
        notifyLifecycleStateOnDestroy();

        mediaPlayer.stop();

        playbackData.setMediaPosition(mediaPlayer.getCurrentPosition());
        playbackData.persistAsync();

        if (errorMessage != null) {
            setState(STATE_ERROR);
        } else {
            setState(STATE_IDLE);
        }

        audioEffects.release();
        mediaPlayer.release();
    }

    private void setState(@NonNull final PlaybackState state) {
        if (this.state != state) {
            this.state = state;
            notifyState();
            playbackData.setPlaybackState(state);
        }
    }

    @Override
    public void notifyState() {
        Schedulers.io().scheduleDirect(() -> unitReporter.reportPlaybackState(state, errorMessage));
    }

    private final class AudioFocusListenerImpl implements AudioFocusListener {

        @Override
        public void onFocusGranted() {
            playCurrent();
        }

        @Override
        public void onFocusDenied() {
            pauseTemporary();
        }
    }

    private final class MediaPlayerListenerImpl implements MediaPlayerListener {

        @Override
        public void onAudioSessionId(final int audioSessionId) {
            errorMessage = null;
            if (audioSessionId == MediaPlayerKt.SESSION_ID_NOT_SET) {
                audioEffects.release();
            } else {
                audioEffects.create(audioSessionId);
            }
        }

        @Override
        public void onLoading() {
            errorMessage = null;
            setState(STATE_LOADING);
        }

        @Override
        public void onPlaybackStarted() {
            unitStopTimeout.abortStopTimer();
            errorMessage = null;
            setState(STATE_PLAYING);
            showNotification();
            unitMediaPositionUpdater.initializeMediaPositionUpdater();
        }

        @Override
        public void onPlaybackFinished() {
            errorMessage = null;
            if (!isDestroying && state == STATE_PLAYING) {
                playNextInner(false);
            }
        }

        @Override
        public void onPlaybackPaused() {
            errorMessage = null;
            setState(STATE_PAUSED);
            unitMediaPositionUpdater.disposeMediaPositionUpdater();
        }

        @Override
        public void onPlayerError(@Nullable final Exception error) {
            errorMessage = playbackServiceView.showPlaybackFailedError(error);
            setState(STATE_ERROR);
            stopAction.run();
        }
    }
}
