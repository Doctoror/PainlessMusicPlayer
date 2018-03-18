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

import com.doctoror.fuckoffmusicplayer.data.util.Log;
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
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
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

final class ExoMediaPlayer implements MediaPlayer {

    private static final String TAG = "ExoMediaPlayer";

    private SimpleExoPlayer mExoPlayer;
    private DataSource.Factory mDataSourceFactory;
    private ExtractorsFactory mExtractorsFactory;

    private MediaPlayerListener mMediaPlayerListener;
    private MediaSource mMediaSource;

    private Uri mLoadingMediaUri;
    private Uri mLoadedMediaUri;

    ExoMediaPlayer() {

    }

    @Override
    public void setListener(@Nullable final MediaPlayerListener listener) {
        mMediaPlayerListener = listener;
    }

    @Override
    public void init(@NonNull final Context context) {
        final TrackSelector trackSelector = new DefaultTrackSelector();

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(context), trackSelector, new DefaultLoadControl());
        mExoPlayer.addListener(mEventListener);
        mExoPlayer.addAudioDebugListener(mAudioRendererEventListener);

        mDataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "Fuck Off Music Player"));
        mExtractorsFactory = new DefaultExtractorsFactory();
    }

    @Override
    public void load(@NonNull final Uri uri) {
        if (mMediaSource != null) {
            mMediaSource.releaseSource();
        }
        if (mMediaPlayerListener != null) {
            mMediaPlayerListener.onLoading();
        }
        mMediaSource = new ExtractorMediaSource(uri, mDataSourceFactory, mExtractorsFactory,
                null, null);

        mLoadingMediaUri = uri;
        mExoPlayer.prepare(mMediaSource);
    }

    @Override
    public void play() {
        mExoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        mExoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void seekTo(final long millis) {
        mExoPlayer.seekTo(millis);
    }

    @Override
    public long getCurrentPosition() {
        return mExoPlayer.getCurrentPosition();
    }

    @Nullable
    @Override
    public Uri getLoadedMediaUri() {
        return mLoadedMediaUri;
    }

    @Override
    public void stop() {
        mExoPlayer.stop();
        if (mMediaSource != null) {
            mMediaSource.releaseSource();
        }
    }

    @Override
    public void release() {
        mExoPlayer.release();
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
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onAudioSessionId(audioSessionId);
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
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onAudioSessionId(SESSION_ID_NOT_SET);
            }
        }
    };

    private final Player.EventListener mEventListener = new Player.EventListener() {

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
            if (mMediaPlayerListener != null) {
                switch (playbackState) {
                    case Player.STATE_READY:
                        mLoadedMediaUri = mLoadingMediaUri;
                        if (playWhenReady) {
                            mMediaPlayerListener.onPlaybackStarted();
                        } else {
                            mMediaPlayerListener.onPlaybackPaused();
                        }
                        break;

                    case Player.STATE_ENDED:
                        mMediaPlayerListener.onPlaybackFinished();
                        break;
                }
            }
        }

        @Override
        public void onTimelineChanged(final Timeline timeline, final Object manifest) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onTimelineChanged: " + timeline);
            }
        }

        @Override
        public void onPlayerError(final ExoPlaybackException error) {
            if (Log.logDEnabled()) {
                Log.d(TAG, "onPlayerError: " + (error == null ? "null" : error));
            }
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onPlayerError(error);
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
