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
package com.doctoror.fuckoffmusicplayer.data.player;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerListener;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Locale;

import static com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerKt.SESSION_ID_NOT_SET;

final class ExoMediaPlayer implements MediaPlayer {

    private static final String TAG = "ExoMediaPlayer";

    private SimpleExoPlayer exoPlayer;
    private DataSource.Factory dataSourceFactory;

    private MediaPlayerListener mediaPlayerListener;
    private MediaSource mediaSource;

    private Uri loadingMediaUri;
    private Uri loadedMediaUri;

    ExoMediaPlayer() {

    }

    @Override
    public void setListener(@Nullable final MediaPlayerListener listener) {
        mediaPlayerListener = listener;
    }

    @Override
    public void init(@NonNull final Context context) {
        final TrackSelector trackSelector = new DefaultTrackSelector();

        exoPlayer = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context), trackSelector, new DefaultLoadControl());
        exoPlayer.addListener(mEventListener);
        exoPlayer.addAudioDebugListener(mAudioRendererEventListener);

        dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Painless Music Player"));
    }

    @Override
    public void load(@NonNull final Uri uri) {
        if (mediaSource != null) {
            mediaSource.releaseSource();
        }
        if (mediaPlayerListener != null) {
            mediaPlayerListener.onLoading();
        }
        mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri);

        loadingMediaUri = uri;
        exoPlayer.prepare(mediaSource);
    }

    @Override
    public void play() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(final long millis) {
        exoPlayer.seekTo(millis);
    }

    @Override
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Nullable
    @Override
    public Uri getLoadedMediaUri() {
        return loadedMediaUri;
    }

    @Override
    public void stop() {
        exoPlayer.stop();
        if (mediaSource != null) {
            mediaSource.releaseSource();
        }
    }

    @Override
    public void release() {
        exoPlayer.release();
    }

    private final AudioRendererEventListener mAudioRendererEventListener
            = new AudioRendererEventListener() {

        @Override
        public void onAudioEnabled(final DecoderCounters counters) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onAudioEnabled");
            }
        }

        @Override
        public void onAudioSessionId(final int audioSessionId) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onAudioSessionId: " + audioSessionId);
            }
            if (mediaPlayerListener != null) {
                mediaPlayerListener.onAudioSessionId(audioSessionId);
            }
        }

        @Override
        public void onAudioDecoderInitialized(final String decoderName,
                                              final long initializedTimestampMs,
                                              final long initializationDurationMs) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onAudioDecoderInitialized: " + (decoderName == null ? "null"
                        : decoderName));
            }
        }

        @Override
        public void onAudioInputFormatChanged(final Format format) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onAudioInputFormatChanged: " + (format == null ? "null" : format));
            }
        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs,
                                        long elapsedSinceLastFeedMs) {
            if (Log.logDEnabled()) {
                Log.d(TAG, String.format(Locale.US,
                        "onAudioSinkUnderrun, bufferSize = '%d', bufferSizeMs = '%d', elapsedSinceLastFeedMs = '%d",
                        bufferSize, bufferSizeMs, elapsedSinceLastFeedMs));
            }
        }

        @Override
        public void onAudioDisabled(final DecoderCounters counters) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onAudioDisabled");
            }
            if (mediaPlayerListener != null) {
                mediaPlayerListener.onAudioSessionId(SESSION_ID_NOT_SET);
            }
        }
    };

    private final Player.EventListener mEventListener = new Player.EventListener() {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onTimelineChanged()");
            }
        }

        @Override
        public void onTracksChanged(final TrackGroupArray trackGroups,
                                    final TrackSelectionArray trackSelections) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onTracksChanged()");
            }
        }

        @Override
        public void onLoadingChanged(final boolean isLoading) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onLoadingChanged: " + isLoading);
            }
        }

        @Override
        public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onPlayerStateChanged: " + playbackState);
            }
            if (mediaPlayerListener != null) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        loadedMediaUri = loadingMediaUri;
                        if (playWhenReady) {
                            mediaPlayerListener.onPlaybackStarted();
                        } else {
                            mediaPlayerListener.onPlaybackPaused();
                        }
                        break;

                    case Player.STATE_ENDED:
                        mediaPlayerListener.onPlaybackFinished();
                        break;
                }
            }
        }

        @Override
        public void onPlayerError(final ExoPlaybackException error) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onPlayerError: " + (error == null ? "null" : error));
            }
            if (mediaPlayerListener != null) {
                mediaPlayerListener.onPlayerError(error);
            }
        }

        @Override
        public void onRepeatModeChanged(final int repeatMode) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onRepeatModeChanged: " + repeatMode);
            }
        }

        @Override
        public void onShuffleModeEnabledChanged(final boolean shuffleModeEnabled) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onShuffleModeEnabledChanged: " + shuffleModeEnabled);
            }
        }

        @Override
        public void onPositionDiscontinuity(final int reason) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onPositionDiscontinuity: " + reason);
            }
        }

        @Override
        public void onPlaybackParametersChanged(final PlaybackParameters playbackParameters) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onPlaybackParametersChanged: " + playbackParameters);
            }
        }

        @Override
        public void onSeekProcessed() {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onSeekProcessed");
            }
        }
    };
}
