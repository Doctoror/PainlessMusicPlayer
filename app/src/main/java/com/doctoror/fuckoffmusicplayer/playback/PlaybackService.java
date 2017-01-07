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

import com.google.android.exoplayer2.audio.AudioTrack;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.doctoror.commons.playback.PlaybackState.State;
import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.appwidget.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.db.playlist.RecentlyScannedPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.player.MediaPlayerListener;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.fuckoffmusicplayer.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.util.RandomHolder;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
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

import rx.Observable;
import rx.Subscription;

import static com.doctoror.commons.playback.PlaybackState.STATE_ERROR;
import static com.doctoror.commons.playback.PlaybackState.STATE_IDLE;
import static com.doctoror.commons.playback.PlaybackState.STATE_LOADING;
import static com.doctoror.commons.playback.PlaybackState.STATE_PAUSED;
import static com.doctoror.commons.playback.PlaybackState.STATE_PLAYING;


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
    static final String ACTION_PLAY_MEDIA_FROM_PLAYLIST = "ACTION_PLAY_MEDIA_FROM_PLAYLIST";
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

    @State
    private static int sLastKnownState;

    @State
    public static int getLastKnownState() {
        return sLastKnownState;
    }

    @State
    private int mState = STATE_IDLE;

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private final AudioBecomingNoisyReceiver mBecomingNoisyReceiver
            = new AudioBecomingNoisyReceiver();

    private final MediaPlayer mMediaPlayer = MediaPlayerFactory.newMediaPlayer();

    private CurrentPlaylist mPlaylist;

    private AudioEffects mAudioEffects;

    private RequestManager mGlide;
    private AudioManager mAudioManager;

    private MediaSessionHolder mMediaSessionHolder;
    private PlaybackReporter mPlaybackReporter;
    private PlaybackParams mPlaybackParams;

    private boolean mAudioFocusRequested;
    private boolean mFocusGranted;
    private boolean mPlayOnFocusGain;

    private Media mCurrentTrack;

    private Subscription mTimerSubscription;
    private Subscription mPauseTimeoutSubscription;

    private PowerManager.WakeLock mWakeLock;
    private boolean mDestroying;

    private CharSequence mErrorMessage;

    private GoogleApiClient mGoogleApiClientWear;
    private String mPermissionReceivePlaybackState;

    private PlaybackController mPlaybackController;

    @Inject
    RecentlyScannedPlaylistFactory mRecentlyScannedPlaylistFactory;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerHolder.getInstance(this).mainComponent().inject(this);

        mDestroying = false;
        mErrorMessage = null;
        mPermissionReceivePlaybackState = getPackageName()
                .concat(SUFFIX_PERMISSION_RECEIVE_PLAYBACK_STATE);

        mPlaylist = CurrentPlaylist.getInstance(this);
        mPlaylist.addObserver(mPlaylistObserver);

        final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioEffects = AudioEffects.getInstance(this);

        mGlide = Glide.with(this);
        mGoogleApiClientWear = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mGoogleApiClientCallbacks)
                .build();

        mMediaSessionHolder = MediaSessionHolder.getInstance(this);
        mMediaSessionHolder.openSession();

        final MediaSessionCompat mediaSession = mMediaSessionHolder.getMediaSession();
        if (mediaSession == null) {
            throw new IllegalStateException("MediaSession is null");
        }

        mPlaybackReporter = PlaybackReporterFactory
                .newUniversalReporter(this, mGoogleApiClientWear, mediaSession, mGlide);

        mPlaybackParams = PlaybackParams.getInstance(this);

        registerReceiver(mResendStateReceiver, new IntentFilter(ACTION_RESEND_STATE));
        registerReceiver(mBecomingNoisyReceiver, mBecomingNoisyReceiver.mIntentFilter);

        mMediaPlayer.setListener(mMediaPlayerListener);
        mMediaPlayer.init(this);

        mGoogleApiClientWear.connect();
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
            mPlaybackController.setPlaylist(mPlaylist.getPlaylist());
            mPlaybackController.setPositionInPlaylist(mPlaylist.getIndex());
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
        if (intent != null) {
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
                    onActionStopWithError(intent.getStringExtra(EXTRA_ERROR_MESSAGE));
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

                case ACTION_PLAY_MEDIA_FROM_PLAYLIST:
                    onActionPlayMediaFromPlaylist(intent);
                    break;

                case Intent.ACTION_MEDIA_BUTTON:
                    onActionMediaButton(intent);
                    break;

                default:
                    stopSelf(startId);
                    break;
            }
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
                playCurrentOrNewPlaylist();
                break;

            case STATE_LOADING:
            default:
                // Do nothing
                break;
        }
    }

    private void onActionPlay() {
        if (mPauseTimeoutSubscription != null) {
            mPauseTimeoutSubscription.unsubscribe();
            mPauseTimeoutSubscription = null;
        }
        mPlayOnFocusGain = true;
        playCurrent(true);
    }

    private void onActionPlayAnything() {
        playCurrentOrNewPlaylist();
    }

    private void onActionPause() {
        mPlayOnFocusGain = false;
        pause();
        mPauseTimeoutSubscription = Observable.timer(8, TimeUnit.SECONDS)
                .subscribe(o -> onActionStop());
        showNotification();
    }

    private void onActionStop() {
        mPlayOnFocusGain = false;
        stopSelf();
    }

    private void onActionStopWithError(@Nullable final String errorMessage) {
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
        final Media media = mPlaylist.getMedia();
        if (media != null) {
            final long duration = media.getDuration();
            if (duration > 0) {
                final int position = (int) ((float) duration * positionPercent);
                mPlaylist.setPosition(position);
                mMediaPlayer.seekTo(position);
            }
        }
    }

    private void onActionSeek(final long position) {
        final Media media = mPlaylist.getMedia();
        if (media != null) {
            final long duration = media.getDuration();
            if (duration > 0 && position < duration) {
                mMediaPlayer.seekTo(position);
            }
        }
    }

    private void onActionPlayMediaFromPlaylist(final Intent intent) {
        final long mediaId = intent.getLongExtra(EXTRA_MEDIA_ID, 0);
        final List<Media> playlist = mPlaylist.getPlaylist();
        if (playlist != null) {
            int mediaPosition = -1;
            for (int i = 0; i < playlist.size(); i++) {
                final Media m = playlist.get(i);
                if (m.getId() == mediaId) {
                    mediaPosition = i;
                    break;
                }
            }
            if (mediaPosition == -1) {
                Log.w(TAG, "Media with id " + mediaId + " not found in current playlist");
            } else {
                play(playlist, mediaPosition, false, false);
            }
        }
    }

    private void pause() {
        mMediaPlayer.pause();
        setState(STATE_PAUSED);
    }

    private void playCurrentOrNewPlaylist() {
        final List<Media> playlist = mPlaylist.getPlaylist();
        if (playlist != null && !playlist.isEmpty()) {
            play(playlist, mPlaylist.getIndex(), true, false);
        } else {
            PlaylistUtils.play(this, mRecentlyScannedPlaylistFactory.loadRecentlyScannedPlaylist());
        }
    }

    private void playCurrent(final boolean mayContinueWhereStopped) {
        play(mPlaylist.getPlaylist(), mPlaylist.getIndex(), mayContinueWhereStopped, false);
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

    private void play(@Nullable final List<Media> list,final int position,
            final boolean mayContinueWhereStopped,
            final boolean fromPlaybackController) {
        if (list == null) {
            throw new IllegalArgumentException("Playlist is null");
        }

        ensureFocusRequested();
        if (!mFocusGranted) {
            return;
        }

        final Media media = list.get(position);
        if (mState == STATE_PAUSED && mCurrentTrack != null
                && media.getId() == mCurrentTrack.getId()) {
            mMediaPlayer.play();
            mExecutor.submit(mRunnableReportCurrentMedia);
        } else {
            mExecutor.submit(() -> {
                long seekPosition = 0;
                // If restoring from stopped state, set seek position to what it was
                if (mayContinueWhereStopped && mState == STATE_IDLE
                        && media.equals(mPlaylist.getMedia())) {
                    seekPosition = mPlaylist.getPosition();
                }
                mPlaylist.setIndex(position);
                mPlaylist.setPosition(seekPosition);
                mPlaylist.setMedia(media);
                mCurrentTrack = media;

                if (!fromPlaybackController) {
                    getPlaybackController().setPositionInPlaylist(position);
                }

                reportCurrentMedia();
                reportCurrentPlaybackPosition();

                mMediaPlayer.stop();
                mMediaPlayer.load(media.getData());
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
        final Media media = mPlaylist.getMedia();
        if (media != null) {
            final MediaSessionCompat mediaSession = getMediaSession();
            if (mediaSession != null) {
                mExecutor.submit(() -> startForeground(NOTIFICATION_ID, PlaybackNotification
                        .create(getApplicationContext(), mGlide, media, mState, mediaSession)));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mGoogleApiClientWear.disconnect();
        mMediaPlayer.stop();
        mPlaylist.deleteObserver(mPlaylistObserver);
        mPlaylist.setPosition(mMediaPlayer.getCurrentPosition());
        mPlaylist.persistAsync();
        mDestroying = true;
        mPlayOnFocusGain = false;
        unregisterReceiver(mBecomingNoisyReceiver);
        unregisterReceiver(mResendStateReceiver);
        if (mErrorMessage != null) {
            setState(STATE_ERROR);
        } else {
            setState(STATE_IDLE);
        }
        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
            mTimerSubscription = null;
        }
        mAudioEffects.relese();
        mMediaPlayer.release();
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        mAudioFocusRequested = false;
        mMediaSessionHolder.closeSession();
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    private void ensureFocusRequested() {
        if (!mAudioFocusRequested) {
            mAudioFocusRequested = true;
            final int result = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            mPlayOnFocusGain = true;
            mFocusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }
    }

    private void setState(@State final int state) {
        if (mState != state) {
            mState = state;
            sLastKnownState = state;
            mExecutor.submit(() -> reportPlaybackState(state, mErrorMessage));
            broadcastState();
        }
    }

    private void broadcastState() {
        final Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, mState);
        sendBroadcast(intent, mPermissionReceivePlaybackState);
    }

    private void updatePosition() {
        if (mState == STATE_PLAYING) {
            mPlaylist.setPosition(mMediaPlayer.getCurrentPosition());
            mExecutor.submit(mRunnableReportCurrentPosition);
        }
    }

    @WorkerThread
    private void reportCurrentMedia() {
        final Media media = mPlaylist.getMedia();
        if (media != null) {
            mPlaybackReporter.reportTrackChanged(media, mPlaylist.getIndex());
        }
    }

    @WorkerThread
    private void reportCurrentPlaybackState() {
        reportPlaybackState(mState, mErrorMessage);
    }

    @WorkerThread
    private void reportPlaybackState(@State final int state,
            @Nullable final CharSequence errorMessage) {
        mPlaybackReporter.reportPlaybackStateChanged(state, errorMessage);
    }

    @WorkerThread
    private void reportCurrentPlaybackPosition() {
        final Media media = mPlaylist.getMedia();
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
    private void reportCurrentPlaylist() {
        final List<Media> playlist = mPlaylist.getPlaylist();
        if (playlist != null) {
            mPlaybackReporter.reportPlaylistChanged(playlist);
        }
    }

    private final GoogleApiClient.ConnectionCallbacks mGoogleApiClientCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            mExecutor.submit(() -> {
                reportCurrentPlaylist();
                reportCurrentMedia();
                reportCurrentPlaybackState();
                reportCurrentPlaybackPosition();
            });
        }

        @Override
        public void onConnectionSuspended(final int i) {
            // ignored
        }
    };

    private final BroadcastReceiver mResendStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            broadcastState();
        }
    };

    private final CurrentPlaylist.PlaylistObserver mPlaylistObserver
            = new CurrentPlaylist.PlaylistObserver() {

        @Override
        public void onPositionChanged(final long position) {
            // Stub
        }

        @Override
        public void onMediaChanged(final Media media) {
            // Stub
        }

        @Override
        public void onMediaRemoved(final Media media) {
            if (mCurrentTrack != null && mCurrentTrack.getId() == media.getId()) {
                final List<Media> playlist = mPlaylist.getPlaylist();
                if (playlist != null && !playlist.isEmpty()) {
                    // Stop current and play the other track from playlist
                    restart();
                    mExecutor.submit(mRunnableReportCurrentPlaylist);
                } else {
                    mCurrentTrack = null;
                    AlbumThumbHolder.getInstance(PlaybackService.this).setAlbumThumb(null);
                    stopSelf();
                }
            }
        }

        @Override
        public void onPlaylistChanged(@Nullable final List<Media> playlist) {
            final PlaybackController playbackController = getPlaybackController();
            playbackController.setPlaylist(playlist);
            playbackController.setPositionInPlaylist(mPlaylist.getIndex());
            mExecutor.submit(mRunnableReportCurrentPlaylist);
        }

        @Override
        public void onPlaylistOrderingChanged(@NonNull final List<Media> playlist) {
            final PlaybackController playbackController = getPlaybackController();
            playbackController.setPlaylist(playlist);
            playbackController.setPositionInPlaylist(mPlaylist.getIndex());
            mExecutor.submit(mRunnableReportCurrentPlaylist);
        }
    };

    private final MediaPlayerListener mMediaPlayerListener = new MediaPlayerListener() {

        @Override
        public void onAudioSessionId(final int audioSessionId) {
            mErrorMessage = null;
            if (audioSessionId == AudioTrack.SESSION_ID_NOT_SET) {
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
            mTimerSubscription = Observable.interval(1L, TimeUnit.SECONDS)
                    .subscribe(o -> updatePosition());
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
            if (mTimerSubscription != null) {
                mTimerSubscription.unsubscribe();
                mTimerSubscription = null;
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

    private final Runnable mRunnableReportCurrentMedia = this::reportCurrentMedia;
    private final Runnable mRunnableReportCurrentPosition = this::reportCurrentPlaybackPosition;
    private final Runnable mRunnableReportCurrentPlaylist = this::reportCurrentPlaylist;

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

        void setPlaylist(@Nullable List<Media> playlist);
        void setPositionInPlaylist(int position);
    }

    private class PlaybackControllerNormal implements PlaybackController {

        private final Object LOCK = new Object();

        private List<Media> mPlaylist;
        private int mPosition;

        @Nullable
        protected final List<Media> getPlaylist() {
            return mPlaylist;
        }

        @Override
        public void playPrev(final boolean isUserAction) {
            synchronized (LOCK) {
                if (mPlaylist != null && !mPlaylist.isEmpty()) {
                    final int repeatMode = mPlaybackParams.getRepeatMode();
                    switch (repeatMode) {
                        case PlaybackParams.REPEAT_MODE_NONE:
                            if (!isUserAction && mPosition == 0) {
                                onPlay(mPlaylist, mPosition);
                            } else {
                                onPlay(mPlaylist, prevPos(mPlaylist, mPosition));
                            }
                            break;

                        case PlaybackParams.REPEAT_MODE_PLAYLIST:
                            onPlay(mPlaylist, prevPos(mPlaylist, mPosition));
                            break;

                        case PlaybackParams.REPEAT_MODE_TRACK:
                            if (isUserAction) {
                                onPlay(mPlaylist, prevPos(mPlaylist, mPosition));
                            } else {
                                onPlay(mPlaylist, mPosition);
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void playNext(final boolean isUserAction) {
            synchronized (LOCK) {
                if (mPlaylist != null && !mPlaylist.isEmpty()) {
                    final int repeatMode = mPlaybackParams.getRepeatMode();
                    switch (repeatMode) {
                        case PlaybackParams.REPEAT_MODE_NONE:
                            if (!isUserAction && mPosition == mPlaylist.size() - 1) {
                                stopSelf();
                            } else {
                                onPlay(mPlaylist, nextPos(mPlaylist, mPosition));
                            }
                            break;

                        case PlaybackParams.REPEAT_MODE_PLAYLIST:
                            onPlay(mPlaylist, nextPos(mPlaylist, mPosition));
                            break;

                        case PlaybackParams.REPEAT_MODE_TRACK:
                            if (isUserAction) {
                                onPlay(mPlaylist, nextPos(mPlaylist, mPosition));
                            } else {
                                onPlay(mPlaylist, mPosition);
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
        public void setPlaylist(final List<Media> playlist) {
            synchronized (LOCK) {
                mPlaylist = playlist;
            }
        }

        @Override
        public void setPositionInPlaylist(final int position) {
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
        public void setPlaylist(@Nullable final List<Media> playlist) {
            synchronized (mLock) {
                rebuildShuffledPositions(playlist == null ? 0 : playlist.size());
            }
            super.setPlaylist(playlist);
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
