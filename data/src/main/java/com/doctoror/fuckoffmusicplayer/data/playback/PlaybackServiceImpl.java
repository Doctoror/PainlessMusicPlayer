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

import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleOwner;
import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackControllerProvider;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.AudioFocusListener;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitAudioFocus;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitMediaSession;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitPlayCurrentOrNewQueue;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitPlayMediaFromQueue;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitReporter;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitQueueMonitor;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitAudioNoisyManagement;
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitWakeLock;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerListener;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_ERROR;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_IDLE;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_LOADING;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PAUSED;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PLAYING;

public final class PlaybackServiceImpl extends ServiceLifecycleOwner implements PlaybackService {

    private final Context mContext;

    private final AudioEffects mAudioEffects;

    private final CurrentMediaProvider mCurrentMediaProvider;

    private final MediaSessionHolder mMediaSessionHolder;

    private final PlaybackControllerProvider playbackControllerProvider;

    private final PlaybackData mPlaybackData;

    private final PlaybackServiceView mPlaybackServicePresenter;

    private final Runnable mStopAction;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final PlaybackServiceUnitAudioFocus unitAudioFocus;
    private final PlaybackServiceUnitAudioNoisyManagement unitAudioNoisyManagement;
    private final PlaybackServiceUnitMediaSession unitMediaSession;
    private final PlaybackServiceUnitPlayCurrentOrNewQueue unitPlayCurrentOrNewQueue;
    private final PlaybackServiceUnitPlayMediaFromQueue unitPlayMediaFromQueue;
    private final PlaybackServiceUnitQueueMonitor unitQueueMonitor;
    private final PlaybackServiceUnitReporter unitReporter;
    private final PlaybackServiceUnitWakeLock unitWakeLock;

    private boolean playOnFocusGain;

    private PlaybackState mState = STATE_IDLE;

    private final MediaPlayer mediaPlayer;

    private Disposable mDisposableTimer;
    private Disposable mDisposablePauseTimeout;

    private boolean mDestroying;

    private CharSequence mErrorMessage;

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
            @NonNull final PlaybackServiceUnitMediaSession unitMediaSession,
            @NonNull final PlaybackServiceUnitPlayCurrentOrNewQueue unitPlayCurrentOrNewQueue,
            @NonNull final PlaybackServiceUnitPlayMediaFromQueue unitPlayMediaFromQueue,
            @NonNull final PlaybackServiceUnitQueueMonitor unitQueueMonitor,
            @NonNull final PlaybackServiceUnitReporter unitReporter,
            @NonNull final PlaybackServiceUnitWakeLock unitWakeLock,
            @NonNull final PlaybackServiceView playbackServicePresenter,
            @NonNull final Runnable stopAction) {
        mContext = context;
        mAudioEffects = audioEffects;
        mCurrentMediaProvider = currentMediaProvider;
        this.mediaPlayer = mediaPlayer;
        mMediaSessionHolder = mediaSessionHolder;
        this.playbackControllerProvider = playbackControllerProvider;
        mPlaybackData = playbackData;
        mPlaybackServicePresenter = playbackServicePresenter;
        mStopAction = stopAction;

        this.unitAudioFocus = unitAudioFocus;
        this.unitMediaSession = unitMediaSession;
        this.unitAudioNoisyManagement = unitAudioNoisyManagement;
        this.unitPlayCurrentOrNewQueue = unitPlayCurrentOrNewQueue;
        this.unitPlayMediaFromQueue = unitPlayMediaFromQueue;
        this.unitQueueMonitor = unitQueueMonitor;
        this.unitReporter = unitReporter;
        this.unitWakeLock = unitWakeLock;

        unitAudioFocus.setListener(new AudioFocusListenerImpl());
        init();
    }

    private void init() {
        registerLifecycleObserver(unitWakeLock);
        registerLifecycleObserver(unitAudioFocus);
        registerLifecycleObserver(unitAudioNoisyManagement);
        registerLifecycleObserver(unitQueueMonitor);

        // Ensure the ordering of these two does not change
        registerLifecycleObserver(unitMediaSession);
        registerLifecycleObserver(unitReporter);

        // Must be called after all lifecycle observers registered
        onCreate();

        mDestroying = false;
        mErrorMessage = null;

        mediaPlayer.setListener(new MediaPlayerListenerImpl());
        mediaPlayer.init(mContext);
    }

    @Override
    public void playPause() {
        switch (mState) {
            case STATE_PLAYING:
                pause();
                break;

            case STATE_PAUSED:
                play();
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

    @Override
    public void play() {
        if (mDisposablePauseTimeout != null) {
            mDisposablePauseTimeout.dispose();
            mDisposablePauseTimeout = null;
        }
        playOnFocusGain = true;
        playCurrent(true);
    }

    @Override
    public void playAnything() {
        playCurrentOrNewQueue();
    }

    @Override
    public void pause() {
        playOnFocusGain = false;
        pauseInner();
        mDisposablePauseTimeout = Observable.timer(8, TimeUnit.SECONDS)
                .subscribe(o -> stop());
        showNotification();
    }

    @Override
    public void stop() {
        playOnFocusGain = false;
        mStopAction.run();
    }

    @Override
    public void stopWithError(@Nullable final CharSequence errorMessage) {
        playOnFocusGain = false;
        mErrorMessage = errorMessage;
        mStopAction.run();
    }

    @Override
    public void playPrev() {
        playPrevInner();
    }

    @Override
    public void playNext() {
        playNextInner(true);
    }

    @Override
    public void seek(final long position) {
        mediaPlayer.seekTo(position);
    }

    private void pauseInner() {
        mediaPlayer.pause();
        setState(STATE_PAUSED);
    }

    private void playCurrent(final boolean mayContinueWhereStopped) {
        Completable.fromAction(() -> unitPlayMediaFromQueue.play(
                mPlaybackData.getQueue(),
                mPlaybackData.getQueuePosition(),
                mayContinueWhereStopped))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    private void playPrevInner() {
        Completable.fromAction(() -> playbackControllerProvider.obtain().playPrev())
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    private void playNextInner(final boolean isUserAction) {
        Completable.fromAction(() -> playbackControllerProvider.obtain().playNext(isUserAction))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void restart() {
        if (mState == STATE_PLAYING) {
            mediaPlayer.stop();
        }
        playCurrent(false);
    }

    @Nullable
    private MediaSessionCompat getMediaSession() {
        return mMediaSessionHolder != null ? mMediaSessionHolder.getMediaSession() : null;
    }

    private void showNotification() {
        final Media media = mCurrentMediaProvider.getCurrentMedia();
        if (media != null) {
            final MediaSessionCompat mediaSession = getMediaSession();
            if (mediaSession != null) {
                mExecutor.submit(() -> mPlaybackServicePresenter.startForeground(media, mState));
            }
        }
    }

    @Override
    public void destroy() {
        onDestroy();
        playOnFocusGain = false;

        mediaPlayer.stop();

        mPlaybackData.setMediaPosition(mediaPlayer.getCurrentPosition());
        mPlaybackData.persistAsync();

        mDestroying = true;
        if (mErrorMessage != null) {
            setState(STATE_ERROR);
        } else {
            setState(STATE_IDLE);
        }
        if (mDisposableTimer != null) {
            mDisposableTimer.dispose();
            mDisposableTimer = null;
        }
        mAudioEffects.relese();
        mediaPlayer.release();
    }

    private void setState(@NonNull final PlaybackState state) {
        if (mState != state) {
            mState = state;
            notifyState();
            mPlaybackData.setPlaybackState(state);
        }
    }

    @Override
    public void notifyState() {
        mExecutor.submit(() -> unitReporter.reportPlaybackState(mState, mErrorMessage));
    }

    private void updateMediaPosition() {
        if (mState == STATE_PLAYING) {
            mPlaybackData.setMediaPosition(mediaPlayer.getCurrentPosition());
        }
    }

    private final class AudioFocusListenerImpl implements AudioFocusListener {

        @Override
        public void onFocusGranted() {
            if (playOnFocusGain) {
                playCurrent(true);
            }
        }

        @Override
        public void onFocusDenied() {
            playOnFocusGain = mState == STATE_PLAYING;
            pauseInner();
        }
    }

    private final class MediaPlayerListenerImpl implements MediaPlayerListener {

        @Override
        public void onAudioSessionId(final int audioSessionId) {
            mErrorMessage = null;
            if (audioSessionId == MediaPlayer.SESSION_ID_NOT_SET) {
                mAudioEffects.relese();
            } else {
                mAudioEffects.create(audioSessionId);
            }
        }

        @Override
        public void onLoading() {
            mErrorMessage = null;
            setState(STATE_LOADING);
        }

        @Override
        public void onPlaybackStarted() {
            mErrorMessage = null;
            setState(STATE_PLAYING);
            showNotification();
            mDisposableTimer = Observable.interval(1L, TimeUnit.SECONDS)
                    .subscribe(o -> updateMediaPosition());
        }

        @Override
        public void onPlaybackFinished() {
            mErrorMessage = null;
            if (!mDestroying) {
                playNextInner(false);
            }
        }

        @Override
        public void onPlaybackPaused() {
            mErrorMessage = null;
            setState(STATE_PAUSED);
            if (mDisposableTimer != null) {
                mDisposableTimer.dispose();
                mDisposableTimer = null;
            }
        }

        @Override
        public void onPlayerError(@NonNull final Exception error) {
            mErrorMessage = mPlaybackServicePresenter.showPlaybackFailedError(error);
            setState(STATE_ERROR);
            mStopAction.run();
        }
    }
}
