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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.player.MediaPlayerListener;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.Playlist;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 21.10.16.
 */

public final class PlaybackService extends Service {

    private static final String TAG = "PlaybackService";
    private static final int NOTIFICATION_ID = 666;

    private static final String ACTION_RESEND_STATE = "ACTION_RESEND_STATE";
    public static final String ACTION_STATE_CHANGED = "ACTION_STATE_CHANGED";
    public static final String EXTRA_STATE = "EXTRA_STATE";

    private static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    private static final String ACTION_PLAY = "ACTION_PLAY";
    private static final String ACTION_PAUSE = "ACTION_PAUSE";
    private static final String ACTION_STOP = "ACTION_STOP";

    private static final String ACTION_PREV = "ACTION_PREV";
    private static final String ACTION_NEXT = "ACTION_NEXT";

    private static final String ACTION_SEEK = "ACTION_SEEK";
    private static final String EXTRA_POSITION = "EXTRA_POSITION";
    private static final String EXTRA_POSITION_PERCENT = "EXTRA_POSITION_PERCENT";

    public static void resendState(@NonNull final Context context) {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_RESEND_STATE));
    }

    public static void playPause(@NonNull final Context context) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAY_PAUSE);
        context.startService(intent);
    }

    public static void play(@NonNull final Context context) {
        context.startService(playIntent(context));
    }

    public static void pause(@NonNull final Context context) {
        context.startService(pauseIntent(context));
    }

    public static void stop(@NonNull final Context context) {
        context.startService(stopIntent(context));
    }

    public static void prev(@NonNull final Context context) {
        context.startService(prevIntent(context));
    }

    public static void next(@NonNull final Context context) {
        context.startService(nextIntent(context));
    }

    public static void seek(@NonNull final Context context,
            final float positionPercent) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_SEEK);
        intent.putExtra(EXTRA_POSITION_PERCENT, positionPercent);
        context.startService(intent);
    }

    public static void seek(@NonNull final Context context,
            final long position) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_SEEK);
        intent.putExtra(EXTRA_POSITION, position);
        context.startService(intent);
    }

    static Intent playIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PLAY);
        return intent;
    }

    static Intent pauseIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PAUSE);
        return intent;
    }

    static Intent stopIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_STOP);
        return intent;
    }

    static Intent prevIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_PREV);
        return intent;
    }

    static Intent nextIntent(@NonNull final Context context) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(ACTION_NEXT);
        return intent;
    }

    public static final int STATE_IDLE = 0;
    public static final int STATE_LOADING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_ERROR = 4;

    private int mState = STATE_IDLE;

    private final AudioBecomingNoisyReceiver mBecomingNoisyReceiver
            = new AudioBecomingNoisyReceiver();
    private final MediaPlayer mMediaPlayer = MediaPlayerFactory.newMediaPlayer();

    private Playlist mPlaylist;

    private AudioEffects mAudioEffects;

    private RequestManager mGlide;
    private AudioManager mAudioManager;
    private MediaSessionCompat mMediaSession;

    private boolean mAudioFocusRequested;
    private boolean mFocusGranted;
    private boolean mPlayOnFocusGain;

    private LocalBroadcastManager mLocalBroadcastManager;
    private Media mCurrentTrack;

    private Subscription mTimerSubscription;
    private Subscription mPauseTimeoutSubscription;

    private PowerManager.WakeLock mWakeLock;
    private boolean mDestroying;

    private CharSequence mErrorMessage;

    @Override
    public void onCreate() {
        super.onCreate();
        mDestroying = false;
        mPlaylist = Playlist.getInstance(this);
        mPlaylist.addObserver(mPlaylistObserver);

        final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        mWakeLock.acquire();

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mAudioEffects = AudioEffects.getInstance(this);

        final ComponentName mediaButtonReceiver = new ComponentName(this,
                MediaButtonReceiver.class);

        mMediaSession = new MediaSessionCompat(this, TAG, mediaButtonReceiver,
                PendingIntent.getBroadcast(this, 1, new Intent(this, MediaButtonReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT));
        mMediaSession.setCallback(new MediaSessionCallback(this));

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(mResendStateReceiver,
                new IntentFilter(ACTION_RESEND_STATE));
        registerReceiver(mBecomingNoisyReceiver, mBecomingNoisyReceiver.mIntentFilter);

        mGlide = Glide.with(this);

        mMediaPlayer.setListener(mMediaPlayerListener);
        mMediaPlayer.init(this);

        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mMediaSession.setActive(true);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (!RxPermissions.getInstance(this).isGranted(
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
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

                case ACTION_PAUSE:
                    onActionPause();
                    break;

                case ACTION_STOP:
                    onActionStop();
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
            case STATE_IDLE:
            case STATE_PAUSED:
                onActionPlay();
                break;

            case STATE_PLAYING:
                onActionPause();
                break;
        }
    }

    private void onActionPlay() {
        if (mPauseTimeoutSubscription != null) {
            mPauseTimeoutSubscription.unsubscribe();
            mPauseTimeoutSubscription = null;
        }
        mPlayOnFocusGain = true;
        playCurrent();
    }

    private void onActionPause() {
        mPlayOnFocusGain = false;
        pause();
        mPauseTimeoutSubscription = Observable.timer(8, TimeUnit.SECONDS)
                .subscribe(o -> onActionStop());
        Observable.create(s -> showNotification()).subscribeOn(Schedulers.io()).subscribe();
    }

    private void onActionStop() {
        mPlayOnFocusGain = false;
        stopSelf();
    }

    private void onActionPrev() {
        playPrev();
    }

    private void onActionNext() {
        playNext();
    }

    private void onActionSeek(final Intent intent) {
        if (intent.hasExtra(EXTRA_POSITION_PERCENT)) {
            onActionSeek(intent.getFloatExtra(EXTRA_POSITION_PERCENT, 0f));
        } else if (intent.hasExtra(EXTRA_POSITION)) {
            onActionSeek(intent.getLongExtra(EXTRA_POSITION_PERCENT, 0));
        }
    }

    private void onActionSeek(final float positionPercent) {
        final Media media = mPlaylist.getMedia();
        if (media != null) {
            final long duration = media.getDuration();
            if (duration > 0) {
                final int position = (int) ((float) duration * positionPercent);
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

    private void pause() {
        mMediaPlayer.pause();
        setState(STATE_PAUSED);
    }

    private void playCurrent() {
        play(mPlaylist.getPlaylist(), mPlaylist.getIndex());
    }

    private void playPrev() {
        play(mPlaylist.getPlaylist(), prevPos(mPlaylist.getPlaylist(), mPlaylist.getIndex()));
    }

    private void playNext() {
        play(mPlaylist.getPlaylist(), nextPos(mPlaylist.getPlaylist(), mPlaylist.getIndex()));
    }

    private void restart() {
        mCurrentTrack = null;
        if (mState == STATE_PLAYING) {
            mMediaPlayer.stop();
        }
        playCurrent();
    }

    private void play(@NonNull final List<Media> list, final int position) {
        ensureFocusRequested();
        if (!mFocusGranted) {
            return;
        }

        final Media media = list.get(position);
        if (mState == STATE_PAUSED && mCurrentTrack != null
                && media.getId() == mCurrentTrack.getId()) {
            mMediaPlayer.play();
        } else {
            mPlaylist.setIndex(position);
            mPlaylist.setPosition(0);
            mPlaylist.setMedia(media);
            mCurrentTrack = media;
            if (mMediaSession != null) {
                MediaSessionReporter.reportTrackChanged(this, mGlide, mMediaSession, media);
            }

            mMediaPlayer.stop();
            mMediaPlayer.load(media.getData());
            mMediaPlayer.play();
        }
    }

    private void showNotification() {
        final Media media = mPlaylist.getMedia();
        if (media != null) {
            Observable.create(s -> startForeground(NOTIFICATION_ID,
                    PlaybackNotification.create(getApplicationContext(), mGlide, media, mState)))
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    private static int prevPos(@NonNull final List<Media> list, final int position) {
        if (position - 1 < 0) {
            return list.size() - 1;
        }
        return position - 1;
    }

    private static int nextPos(@NonNull final List<Media> list, final int position) {
        if (position + 1 >= list.size()) {
            return 0;
        }
        return position + 1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPlaylist.deleteObserver(mPlaylistObserver);
        mPlaylist.persistAsync();
        mDestroying = true;
        mPlayOnFocusGain = false;
        mMediaSession.setActive(false);
        unregisterReceiver(mBecomingNoisyReceiver);
        mLocalBroadcastManager.unregisterReceiver(mResendStateReceiver);
        setState(STATE_IDLE);
        if (mTimerSubscription != null) {
            mTimerSubscription.unsubscribe();
            mTimerSubscription = null;
        }
        mAudioEffects.relese();
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        mAudioFocusRequested = false;
        mMediaSession.release();
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

    void setMediaSessionPlaybackState(final int state) {
        final MediaSessionCompat mediaSession = mMediaSession;
        final Media media = mPlaylist.getMedia();
        if (mediaSession != null && media != null) {
            MediaSessionReporter.reportStateChanged(this, mediaSession, media, state,
                    mErrorMessage);
        }
    }

    private void setState(final int state) {
        mState = state;
        setMediaSessionPlaybackState(state);
        broadcastState();
    }

    private void updatePosition() {
        if (mState == STATE_PLAYING) {
            mPlaylist.setPosition(mMediaPlayer.getCurrentPosition());
        }
    }

    private void broadcastState() {
        final Intent intent = new Intent(ACTION_STATE_CHANGED);
        intent.putExtra(EXTRA_STATE, mState);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private final BroadcastReceiver mResendStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            broadcastState();
        }
    };

    private final Playlist.PlaylistObserver mPlaylistObserver = new Playlist.PlaylistObserver() {

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
                } else {
                    stopSelf();
                }
            }
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
                setState(STATE_IDLE);
                playNext();
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
                mFocusGranted = true;
                if (mPlayOnFocusGain) {
                    playCurrent();
                }
                break;

            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                mFocusGranted = true;
                if (mPlayOnFocusGain) {
                    playCurrent();
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
}
