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
package com.doctoror.fuckoffmusicplayer.player;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.audio.AudioTrack;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 23.10.16.
 */

final class ExoMediaPlayer implements MediaPlayer {

    private static final String TAG = "ExoMediaPlayer";

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private SimpleExoPlayer mExoPlayer;
    private DataSource.Factory mDataSourceFactory;
    private ExtractorsFactory mExtractorsFactory;

    private MediaPlayerListener mMediaPlayerListener;
    private MediaSource mMediaSource;

    ExoMediaPlayer() {

    }

    @Override
    public void setListener(@Nullable final MediaPlayerListener listener) {
        mMediaPlayerListener = listener;
    }

    @Override
    public void init(@NonNull final Context context) {
        final TrackSelection.Factory factory = new FixedTrackSelection.Factory();
        final TrackSelector trackSelector = new DefaultTrackSelector(mMainHandler, factory);

        mExoPlayer = ExoPlayerFactory
                .newSimpleInstance(context, trackSelector, new DefaultLoadControl());
        mExoPlayer.addListener(mEventListener);
        mExoPlayer.setAudioDebugListener(mAudioRendererEventListener);

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

        }

        @Override
        public void onAudioSessionId(final int audioSessionId) {
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onAudioSessionId(audioSessionId);
            }
        }

        @Override
        public void onAudioDecoderInitialized(final String decoderName,
                final long initializedTimestampMs,
                final long initializationDurationMs) {

        }

        @Override
        public void onAudioInputFormatChanged(final Format format) {

        }

        @Override
        public void onAudioTrackUnderrun(final int bufferSize, final long bufferSizeMs,
                final long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onAudioDisabled(final DecoderCounters counters) {
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onAudioSessionId(AudioTrack.SESSION_ID_NOT_SET);
            }
        }
    };

    private final ExoPlayer.EventListener mEventListener = new ExoPlayer.EventListener() {

        @Override
        public void onLoadingChanged(final boolean isLoading) {
        }

        @Override
        public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
            if (mMediaPlayerListener != null) {
                switch (playbackState) {
                    case ExoPlayer.STATE_READY:
                        if (playWhenReady) {
                            mMediaPlayerListener.onPlaybackStarted();
                        } else {
                            mMediaPlayerListener.onPlaybackPaused();
                        }
                        break;

                    case ExoPlayer.STATE_ENDED:
                        mMediaPlayerListener.onPlaybackFinished();
                        break;
                }
            }
        }

        @Override
        public void onTimelineChanged(final Timeline timeline, final Object manifest) {

        }

        @Override
        public void onPlayerError(final ExoPlaybackException error) {
            if (mMediaPlayerListener != null) {
                mMediaPlayerListener.onPlayerError(error);
            }
        }

        @Override
        public void onPositionDiscontinuity() {

        }
    };
}
