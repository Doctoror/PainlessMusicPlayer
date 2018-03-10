/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.playback;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackDataUtils;
import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.playback.RepeatMode;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerListener;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.util.RandomHolder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_ERROR;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_IDLE;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_LOADING;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PAUSED;
import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PLAYING;

/**
 * Media playback Service
 */
public final class PlaybackService extends Service {

    private static final String SUFFIX_PERMISSION_RECEIVE_PLAYBACK_STATE
            = ".permission.RECEIVE_PLAYBACK_STATE";

    private static final String TAG = "PlaybackService";
    private static final int NOTIFICATION_ID = 666;

    public static final String ACTION_STATE_CHANGED
            = "com.doctoror.fuckoffmusicplayer.playback.ACTION_STATE_CHANGED";
    public static final String EXTRA_STATE = "EXTRA_STATE";

    static final String ACTION_RESEND_STATE = "ACTION_RESEND_STATE";
    static final String ACTION_PLAY_MEDIA_FROM_QUEUE = "ACTION_PLAY_MEDIA_FROM_QUEUE";
    static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    static final String ACTION_PLAY_ANYTHING = "ACTION_PLAY_ANYTHING";
    static final String ACTION_PLAY = "ACTION_PLAY";
    static final String ACTION_PAUSE = "ACTION_PAUSE";
    static final String ACTION_STOP = "ACTION_STOP";
    static final String ACTION_STOP_WITH_ERROR = "ACTION_STOP_WITH_ERROR";

    static final String ACTION_PREV = "ACTION_PREV";
    static final String ACTION_NEXT = "ACTION_NEXT";

    static final String ACTION_SEEK = "ACTION_SEEK";
    static final String EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE";
    static final String EXTRA_MEDIA_ID = "EXTRA_MEDIA_ID";
    static final String EXTRA_POSITION = "EXTRA_POSITION";
    static final String EXTRA_POSITION_PERCENT = "EXTRA_POSITION_PERCENT";

    @PlaybackState
    private int mState = STATE_IDLE;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final AudioBecomingNoisyReceiver mBecomingNoisyReceiver
            = new AudioBecomingNoisyReceiver();

    private MediaPlayer mMediaPlayer;

    @Nullable
    private AudioManager mAudioManager;

    private PlaybackReporter mPlaybackReporter;

    private boolean mAudioFocusRequested;
    private boolean mFocusGranted;
    private boolean mPlayOnFocusGain;

    private Media mCurrentTrack;

    private Disposable mDisposableTimer;
    private Disposable mDisposablePauseTimeout;

    private PowerManager.WakeLock mWakeLock;
    private boolean mDestroying;

    private CharSequence mErrorMessage;

    private String mPermissionReceivePlaybackState;

    private PlaybackController mPlaybackController;
    private Disposable mDisposableQueue;

    @Inject
    AlbumThumbHolder mAlbumThumbHolder;

    @Inject
    AudioEffects mAudioEffects;

    @Inject
    MediaPlayerFactory mMediaPlayerFactory;

    @Inject
    MediaSessionHolder mMediaSessionHolder;

    @Inject
    PlaybackData mPlaybackData;

    @Inject
    PlaybackInitializer mPlaybackInitializer;

    @Inject
    PlaybackParams mPlaybackParams;

    @Inject
    PlaybackNotificationFactory mPlaybackNotificationFactory;

    @Inject
    PlaybackReporterFactory mPlaybackReporterFactory;

    @Inject
    QueueProviderRecentlyScanned mRecentlyScannedPlaylistFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerHolder.getInstance(this).mainComponent().inject(this);
        acquireWakeLock();

        mMediaPlayer = mMediaPlayerFactory.newMediaPlayer();

        mDestroying = false;
        mErrorMessage = null;
        mPermissionReceivePlaybackState = getPackageName()
                .concat(SUFFIX_PERMISSION_RECEIVE_PLAYBACK_STATE);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mMediaSessionHolder.openSession();

        final MediaSessionCompat mediaSession = mMediaSessionHolder.getMediaSession();
        if (mediaSession == null) {
            throw new IllegalStateException("MediaSession is null");
        }

        mPlaybackReporter = mPlaybackReporterFactory.newUniversalReporter(mediaSession);

        registerReceiver(mResendStateReceiver, new IntentFilter(ACTION_RESEND_STATE));
        registerReceiver(mBecomingNoisyReceiver, mBecomingNoisyReceiver.mIntentFilter);

        mMediaPlayer.setListener(mMediaPlayerListener);
        mMediaPlayer.init(this);

        mDisposableQueue = mPlaybackData.queueObservable().subscribe(mQueueConsumer);
    }

    @SuppressLint("WakelockTimeout") // User may want this to play forever.
    private void acquireWakeLock() {
        final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            mWakeLock.acquire();
        }
    }

    @NonNull
    private PlaybackController getPlaybackController() {
        boolean created = false;
        if (mPlaybackController == null) {
            mPlaybackController = mPlaybackParams.isShuffleEnabled()
                    ? new PlaybackControllerShuffle()
                    : new PlaybackControllerNormal();
            created = true;
        } else if (mPlaybackParams.isShuffleEnabled()) {
            if (!PlaybackControllerShuffle.class.equals(mPlaybackController.getClass())) {
                mPlaybackController = new PlaybackControllerShuffle();
                created = true;
            }
        } else {
            if (!PlaybackControllerNormal.class.equals(mPlaybackController.getClass())) {
                mPlaybackController = new PlaybackControllerNormal();
                created = true;
            }
        }
        if (created) {
            mPlaybackController.setQueue(mPlaybackData.getQueue());
            mPlaybackController.setPositionInQueue(mPlaybackData.getQueuePosition());
        }
        return mPlaybackController;
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent == null) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        final String action = intent.getAction();
        if (action == null) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        switch (intent.getAction()) {
            case ACTION_PLAY_PAUSE:
                onActionPlayPause();
                break;

            case ACTION_PLAY:
                onActionPlay();
                break;

            case ACTION_PLAY_ANYTHING:
                onActionPlayAnything();
                break;

            case ACTION_PAUSE:
                onActionPause();
                break;

            case ACTION_STOP:
                onActionStop();
                break;

            case ACTION_STOP_WITH_ERROR:
                onActionStopWithError(intent.getCharSequenceExtra(EXTRA_ERROR_MESSAGE));
                break;

            case ACTION_PREV:
                onActionPrev();
                break;

            case ACTION_NEXT:
                onActionNext();
                break;

            case ACTION_SEEK:
                onActionSeek(intent);
                break;

            case ACTION_PLAY_MEDIA_FROM_QUEUE:
                onActionPlayMediaFromQueue(intent);
                break;

            case Intent.ACTION_MEDIA_BUTTON:
                onActionMediaButton(intent);
                break;

            default:
                stopSelf(startId);
                return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void onActionMediaButton(@NonNull final Intent intent) {
        final KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    onActionPlay();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    onActionPause();
                    break;

                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    onActionNext();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    onActionPrev();
                    break;

                case KeyEvent.KEYCODE_MEDIA_STOP:
                    onActionStop();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    onActionPlayPause();
                    break;
            }
        }
    }

    private void onActionPlayPause() {
        switch (mState) {
            case STATE_PLAYING:
                onActionPause();
                break;

            case STATE_PAUSED:
                onActionPlay();
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

    private void onActionPlay() {
        if (mDisposablePauseTimeout != null) {
            mDisposablePauseTimeout.dispose();
            mDisposablePauseTimeout = null;
        }
        mPlayOnFocusGain = true;
        playCurrent(true);
    }

    private void onActionPlayAnything() {
        playCurrentOrNewQueue();
    }

    private void onActionPause() {
        mPlayOnFocusGain = false;
        pause();
        mDisposablePauseTimeout = Observable.timer(8, TimeUnit.SECONDS)
                .subscribe(o -> onActionStop());
        showNotification();
    }

    private void onActionStop() {
        mPlayOnFocusGain = false;
        stopSelf();
    }

    private void onActionStopWithError(@Nullable final CharSequence errorMessage) {
        mPlayOnFocusGain = false;
        mErrorMessage = errorMessage;
        stopSelf();
    }

    private void onActionPrev() {
        playPrev(true);
    }

    private void onActionNext() {
        playNext(true);
    }

    private void onActionSeek(final Intent intent) {
        if (intent.hasExtra(EXTRA_POSITION_PERCENT)) {
            onActionSeek(intent.getFloatExtra(EXTRA_POSITION_PERCENT, 0f));
        } else if (intent.hasExtra(EXTRA_POSITION)) {
            onActionSeek(intent.getLongExtra(EXTRA_POSITION, 0));
        }
    }

    private void onActionSeek(final float positionPercent) {
        final Media media = PlaybackDataUtils.getCurrentMedia(mPlaybackData);
        if (media != null) {
            final long duration = media.getDuration();
            if (duration > 0) {
                final int position = (int) ((float) duration * positionPercent);
                mPlaybackData.setMediaPosition(position);
                mMediaPlayer.seekTo(position);
            }
        }
    }

    private void onActionSeek(final long position) {
        final Media media = PlaybackDataUtils.getCurrentMedia(mPlaybackData);
        if (media != null) {
            final long duration = media.getDuration();
            if (duration > 0 && position < duration) {
                mMediaPlayer.seekTo(position);
            }
        }
    }

    private void onActionPlayMediaFromQueue(final Intent intent) {
        final long mediaId = intent.getLongExtra(EXTRA_MEDIA_ID, 0);
        final List<Media> queue = mPlaybackData.getQueue();
        if (queue != null) {
            int mediaPosition = -1;
            int loopPos = 0;
            for (final Media media : queue) {
                if (media.getId() == mediaId) {
                    mediaPosition = loopPos;
                    break;
                }
                loopPos++;
            }
            if (mediaPosition == -1) {
                Log.w(TAG, "Media with id " + mediaId + " not found in current playlist");
            } else {
                play(queue, mediaPosition, false, false);
            }
        }
    }

    private void pause() {
        mMediaPlayer.pause();
        setState(STATE_PAUSED);
    }

    private void playCurrentOrNewQueue() {
        final List<Media> playlist = mPlaybackData.getQueue();
        if (playlist != null && !playlist.isEmpty()) {
            play(playlist, mPlaybackData.getQueuePosition(), true, false);
        } else {
            mRecentlyScannedPlaylistFactory.recentlyScannedQueue()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            q -> mPlaybackInitializer.setQueueAndPlay(q, 0),
                            t -> Log.w(TAG, "Failed to load recently scanned", t));
        }
    }

    private void playCurrent(final boolean mayContinueWhereStopped) {
        play(mPlaybackData.getQueue(), mPlaybackData.getQueuePosition(),
                mayContinueWhereStopped, false);
    }

    private void playPrev(final boolean isUserAction) {
        getPlaybackController().playPrev(isUserAction);
    }

    private void playNext(final boolean isUserAction) {
        getPlaybackController().playNext(isUserAction);
    }

    private void restart() {
        mCurrentTrack = null;
        if (mState == STATE_PLAYING) {
            mMediaPlayer.stop();
        }
        playCurrent(false);
    }

    private void play(@Nullable final List<Media> queue,
            final int position,
            final boolean mayContinueWhereStopped,
            final boolean fromPlaybackController) {
        if (queue == null) {
            throw new IllegalArgumentException("Play queue is null");
        }

        ensureFocusRequested();
        if (!mFocusGranted) {
            return;
        }

        Media media = CollectionUtils.getItemSafe(queue, position);
        if (media == null) {
            media = queue.get(0);
        }
        final Media finalMedia = media;
        if (mState == STATE_PAUSED && mCurrentTrack != null
                && media.getId() == mCurrentTrack.getId()) {
            mMediaPlayer.play();
            mExecutor.submit(mRunnableReportCurrentMedia);
        } else {
            mExecutor.submit(() -> {
                long seekPosition = 0;
                // If restoring from stopped state, set seek position to what it was
                if (mayContinueWhereStopped && mState == STATE_IDLE
                        && finalMedia.equals(PlaybackDataUtils.getCurrentMedia(mPlaybackData))) {
                    seekPosition = mPlaybackData.getMediaPosition();
                    if (seekPosition >= finalMedia.getDuration() - 100) {
                        seekPosition = 0;
                    }
                }
                mPlaybackData.setPlayQueuePosition(position);
                mPlaybackData.setMediaPosition(seekPosition);
                mCurrentTrack = finalMedia;

                if (!fromPlaybackController) {
                    getPlaybackController().setPositionInQueue(position);
                }

                reportCurrentMedia();
                reportCurrentPlaybackPosition();

                mMediaPlayer.stop();
                mMediaPlayer.load(finalMedia.getData());
                if (seekPosition != 0) {
                    mMediaPlayer.seekTo(seekPosition);
                }
                mMediaPlayer.play();
            });
        }
    }

    @Nullable
    private MediaSessionCompat getMediaSession() {
        return mMediaSessionHolder != null ? mMediaSessionHolder.getMediaSession() : null;
    }

    private void showNotification() {
        final Media media = PlaybackDataUtils.getCurrentMedia(mPlaybackData);
        if (media != null) {
            final MediaSessionCompat mediaSession = getMediaSession();
            if (mediaSession != null) {
                mExecutor.submit(() -> startForeground(NOTIFICATION_ID, mPlaybackNotificationFactory
                        .create(getApplicationContext(), media, mState, mediaSession)));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mMediaPlayer.stop();

        mDisposableQueue.dispose();
        mPlaybackReporter.onDestroy();

        mPlaybackData.setMediaPosition(mMediaPlayer.getCurrentPosition());
        mPlaybackData.persistAsync();

        mDestroying = true;
        mPlayOnFocusGain = false;
        unregisterReceiver(mBecomingNoisyReceiver);
        unregisterReceiver(mResendStateReceiver);
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
        mMediaPlayer.release();
        if (mAudioManager != null) {
            mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        }
        mAudioFocusRequested = false;
        mMediaSessionHolder.closeSession();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void ensureFocusRequested() {
        if (!mAudioFocusRequested) {
            mAudioFocusRequested = true;
            final int result;
            if (mAudioManager != null) {
                result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                        AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            } else {
                result = AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            }
            mPlayOnFocusGain = true;
            mFocusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void setState(@PlaybackState final int state) {
        if (mState != state) {
            mState = state;
            mExecutor.submit(() -> reportPlaybackState(state, mErrorMessage));
            mPlaybackData.setPlaybackState(state);
            broadcastState();
        }
    }

    private void broadcastState() {
        final Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, mState);
        sendBroadcast(intent, mPermissionReceivePlaybackState);
    }

    private void updateMediaPosition() {
        if (mState == STATE_PLAYING) {
            mPlaybackData.setMediaPosition(mMediaPlayer.getCurrentPosition());
            mExecutor.submit(mRunnableReportCurrentPosition);
        }
    }

    @WorkerThread
    private void reportCurrentMedia() {
        final int pos = mPlaybackData.getQueuePosition();
        final Media media = CollectionUtils.getItemSafe(mPlaybackData.getQueue(), pos);
        if (media != null) {
            mPlaybackReporter.reportTrackChanged(media, pos);
        }
    }

    @WorkerThread
    private void reportPlaybackState(@PlaybackState final int state,
            @Nullable final CharSequence errorMessage) {
        mPlaybackReporter.reportPlaybackStateChanged(state, errorMessage);
    }

    @WorkerThread
    private void reportCurrentPlaybackPosition() {
        final Media media = PlaybackDataUtils.getCurrentMedia(mPlaybackData);
        if (media == null) {
            mPlaybackReporter.reportPositionChanged(0, 0);
        } else {
            final Uri mediaUri = media.getData();
            if (mediaUri != null && mediaUri.equals(mMediaPlayer.getLoadedMediaUri())) {
                mPlaybackReporter.reportPositionChanged(
                        media.getId(), mMediaPlayer.getCurrentPosition());
            }
        }
    }

    @WorkerThread
    private void reportCurrentQueue() {
        final List<Media> queue = mPlaybackData.getQueue();
        if (queue != null) {
            mPlaybackReporter.reportQueueChanged(queue);
        }
    }

    private final Runnable mRunnableReportCurrentMedia = this::reportCurrentMedia;
    private final Runnable mRunnableReportCurrentPosition = this::reportCurrentPlaybackPosition;
    private final Runnable mRunnableReportCurrentQueue = this::reportCurrentQueue;

    private final BroadcastReceiver mResendStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            broadcastState();
        }
    };

    private final Consumer<List<Media>> mQueueConsumer = q -> {
        if (q == null || q.isEmpty()) {
            mCurrentTrack = null;
            mAlbumThumbHolder.setAlbumThumb(null);
            stopSelf();
        } else {
            final PlaybackController playbackController = getPlaybackController();
            playbackController.setQueue(q);

            final Media current = mCurrentTrack;
            // Playing some track
            if (current != null) {
                final int indexOf = q.indexOf(current);
                if (indexOf != -1) {
                    // This track position changed in queue
                    playbackController.setPositionInQueue(indexOf);
                    mExecutor.submit(mRunnableReportCurrentQueue);
                } else {
                    // This track is not in new queue
                    restart();
                }
            }
        }
    };

    private final MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener() {

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
            mCurrentTrack = null;
            if (!mDestroying) {
                playNext(false);
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
        public void onPlayerError(final Exception error) {
            mCurrentTrack = null;
            mErrorMessage = errorMessage(error);
            setState(STATE_ERROR);
            Toast.makeText(getApplicationContext(), mErrorMessage, Toast.LENGTH_SHORT).show();
            stopSelf();
        }

        @NonNull
        private CharSequence errorMessage(@NonNull final Exception error) {
            final String message = error.getMessage();
            if (TextUtils.isEmpty(message)) {
                return getText(R.string.Failed_to_start_playback);
            }
            return message;
        }
    };

    private final AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener
            = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                mFocusGranted = true;
                if (mPlayOnFocusGain) {
                    playCurrent(true);
                }
                break;

            default:
                mFocusGranted = false;
                mPlayOnFocusGain = mState == STATE_PLAYING;
                pause();
                break;
        }
    };

    private final class AudioBecomingNoisyReceiver extends BroadcastReceiver {

        final IntentFilter mIntentFilter = new IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY);

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                stopSelf();
            }
        }
    }

    private interface PlaybackController {

        void playNext(boolean isUserAction);

        void playPrev(boolean isUserAction);

        void setQueue(@Nullable List<Media> queue);

        void setPositionInQueue(int position);
    }

    private class PlaybackControllerNormal implements PlaybackController {

        private final Object LOCK = new Object();

        private List<Media> mQueue;
        private int mPosition;

        @Nullable
        protected final List<Media> getQueue() {
            return mQueue;
        }

        @Override
        public void playPrev(final boolean isUserAction) {
            synchronized (LOCK) {
                if (mQueue != null && !mQueue.isEmpty()) {
                    @RepeatMode final int repeatMode = mPlaybackParams.getRepeatMode();
                    switch (repeatMode) {
                        case RepeatMode.NONE:
                            if (!isUserAction && mPosition == 0) {
                                onPlay(mQueue, mPosition);
                            } else {
                                onPlay(mQueue, prevPos(mQueue, mPosition));
                            }
                            break;

                        case RepeatMode.QUEUE:
                            onPlay(mQueue, prevPos(mQueue, mPosition));
                            break;

                        case RepeatMode.TRACK:
                            if (isUserAction) {
                                onPlay(mQueue, prevPos(mQueue, mPosition));
                            } else {
                                onPlay(mQueue, mPosition);
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void playNext(final boolean isUserAction) {
            synchronized (LOCK) {
                if (mQueue != null && !mQueue.isEmpty()) {
                    final int repeatMode = mPlaybackParams.getRepeatMode();
                    switch (repeatMode) {
                        case RepeatMode.NONE:
                            if (!isUserAction && mPosition == mQueue.size() - 1) {
                                stopSelf();
                            } else {
                                onPlay(mQueue, nextPos(mQueue, mPosition));
                            }
                            break;

                        case RepeatMode.QUEUE:
                            onPlay(mQueue, nextPos(mQueue, mPosition));
                            break;

                        case RepeatMode.TRACK:
                            if (isUserAction) {
                                onPlay(mQueue, nextPos(mQueue, mPosition));
                            } else {
                                onPlay(mQueue, mPosition);
                            }
                            break;
                    }
                }
            }
        }

        private void onPlay(@Nullable final List<Media> list, final int position) {
            mPosition = position;
            play(list, position);
        }

        protected void play(@Nullable final List<Media> list, final int position) {
            PlaybackService.this.play(list, position, false, true);
        }

        @Override
        public void setQueue(final List<Media> queue) {
            synchronized (LOCK) {
                mQueue = queue;
            }
        }

        @Override
        public void setPositionInQueue(final int position) {
            synchronized (LOCK) {
                mPosition = position;
            }
        }

        private int prevPos(@Nullable final List<Media> list, final int position) {
            if (list == null) {
                throw new IllegalArgumentException("Playlist is null");
            }
            if (position - 1 < 0) {
                return list.size() - 1;
            }
            return position - 1;
        }

        private int nextPos(@Nullable final List<Media> list,
                final int position) {
            if (list == null) {
                throw new IllegalArgumentException("Playlist is null");
            }
            if (position + 1 >= list.size()) {
                return 0;
            }
            return position + 1;
        }
    }

    private final class PlaybackControllerShuffle extends PlaybackControllerNormal {

        private final Object mLock = new Object();

        @NonNull
        private final SparseIntArray mShuffledPositions = new SparseIntArray();

        @Override
        public void setQueue(@Nullable final List<Media> queue) {
            synchronized (mLock) {
                rebuildShuffledPositions(queue == null ? 0 : queue.size());
            }
            super.setQueue(queue);
        }

        @Override
        protected void play(@Nullable final List<Media> list, final int position) {
            final int shuffledPosition;
            synchronized (mLock) {
                shuffledPosition = mShuffledPositions.get(position);
            }
            super.play(list, shuffledPosition);
        }

        private void rebuildShuffledPositions(final int size) {
            mShuffledPositions.clear();
            if (size != 0) {
                final List<Integer> positions = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    positions.add(i);
                }

                Collections.shuffle(positions, RandomHolder.getInstance().getRandom());

                for (int i = 0; i < size; i++) {
                    mShuffledPositions.put(i, positions.get(i));
                }
            }
        }
    }
}
