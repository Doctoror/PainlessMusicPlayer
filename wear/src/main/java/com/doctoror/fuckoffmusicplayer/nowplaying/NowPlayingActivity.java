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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import com.doctoror.commons.util.StringUtils;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.RemoteControl;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityNowPlayingBinding;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;
import com.doctoror.fuckoffmusicplayer.util.GooglePlayServicesUtil;

import android.app.Activity;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

public final class NowPlayingActivity extends Activity {

    private static final String TAG = "WearActivity";

    private static final int REQUEST_CODE_GOOGLE_API = 1;

    private static final int ANIMATOR_CHILD_PRGORESS = 0;
    private static final int ANIMATOR_CHILD_CONTENT = 1;

    private final NowPlayingActivityModelPlaybackState mModelPlaybackState
            = new NowPlayingActivityModelPlaybackState();
    private final NowPlayingActivityModelViewState
            mModelViewState = new NowPlayingActivityModelViewState();
    private final NowPlayingActivityModelMedia mModelMedia = new NowPlayingActivityModelMedia();

    private final RemoteControl mRemoteControl = new RemoteControl();

    private MediaHolder mMediaHolder;

    private GoogleApiClient mGoogleApiClient;
    private View mBtnFix;

    private volatile boolean mSeekBarTracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModelViewState.setBtnPlayRes(R.drawable.ic_play_arrow_white_24dp);

        final ActivityNowPlayingBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_now_playing);
        binding.setPlaybackState(mModelPlaybackState);
        binding.setViewState(mModelViewState);
        binding.setMedia(mModelMedia);
        binding.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl());
        binding.btnPrev.setOnClickListener(v -> mRemoteControl.prev(mGoogleApiClient));
        binding.btnNext.setOnClickListener(v -> mRemoteControl.next(mGoogleApiClient));
        binding.btnPlayPause.setOnClickListener(v -> mRemoteControl.playPause(mGoogleApiClient));
        mBtnFix = binding.btnFix;
        mMediaHolder = MediaHolder.getInstance(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();

        setViewConnecting();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindArt(mMediaHolder.getAlbumArt());
        bindMedia(mMediaHolder.getMedia());
        bindPlaybackState(mMediaHolder.getPlaybackState());
        mMediaHolder.addObserver(mPlaybackInfoObserver);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaHolder.deleteObserver(mPlaybackInfoObserver);
        mRemoteControl.onGoogleApiClientDisconnected(mGoogleApiClient);
        mGoogleApiClient.disconnect();
    }

    private void setViewConnecting() {
        mModelViewState.setFixButtonVisible(false);
        mModelViewState.setProgressVisible(true);
        mModelViewState.setMessage(getText(R.string.Connecting));
        mModelViewState.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);
    }

    private void setViewConnected() {
        mModelViewState.setFixButtonVisible(false);
        mModelViewState.setProgressVisible(false);
        mModelViewState.setAnimatorChild(ANIMATOR_CHILD_CONTENT);
    }

    private void bindMedia(@Nullable final ProtoPlaybackData.Media media) {
        if (media != null) {
            mModelMedia.setArtistAndAlbum(StringUtils.formatArtistAndAlbum(getResources(),
                    media.artist, media.album));
            mModelMedia.setTitle(media.title);
            bindProgress(media.duration, media.progress);

            mModelViewState.setNavigationButtonsVisible(true);
        } else {
            mModelMedia.setArtistAndAlbum(null);
            mModelMedia.setTitle(getText(R.string.Start_playing));
            bindArt(null);
            bindProgress(0, 0);

            mModelViewState.setNavigationButtonsVisible(false);
        }
    }

    private void bindArt(@Nullable final Bitmap albumArt) {
        if (albumArt == null) {
            mModelMedia.setArt(getDrawable(R.drawable.album_art_placeholder));
        } else {
            mModelMedia.setArt(new BitmapDrawable(getResources(), albumArt));
        }
    }

    private void bindPlaybackState(@Nullable final ProtoPlaybackData.PlaybackState playbackState) {
        if (playbackState != null) {
            bindProgress(playbackState.duration, playbackState.progress);
            mModelViewState.setBtnPlayRes(playbackState.state == PlaybackStateCompat.STATE_PLAYING
                    ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
        } else {
            mModelViewState.setBtnPlayRes(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    private void bindProgress(final long duration, final long elapsedTime) {
        mModelPlaybackState.setDuration(duration);
        mModelPlaybackState.setElapsedTime(elapsedTime);
        if (!mSeekBarTracking && duration > 0 && elapsedTime <= duration) {
            // Max is 200 so progress is a fraction of 200
            mModelPlaybackState
                    .setProgress((int) (((double) elapsedTime / (double) duration) * 200f));
        }
    }

    private final class OnSeekBarChangeListenerImpl implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(final SeekBar seekBar, final int i, final boolean fromUser) {
            // stub
        }

        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) {
            mSeekBarTracking = true;
        }

        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            mSeekBarTracking = false;
            mRemoteControl.seek(mGoogleApiClient,
                    (float) seekBar.getProgress() / (float) seekBar.getMax());
        }
    }

    private final MediaHolder.PlaybackInfoObserver mPlaybackInfoObserver
            = new MediaHolder.PlaybackInfoObserver() {

        @Override
        public void onMediaChanged(@Nullable final ProtoPlaybackData.Media media) {
            bindMedia(media);
        }

        @Override
        public void onAlbumArtChanged(@Nullable final Bitmap albumArt) {
            bindArt(albumArt);
        }

        @Override
        public void onPlaybackStateChanged(
                @Nullable final ProtoPlaybackData.PlaybackState playbackState) {
            bindPlaybackState(playbackState);
        }
    };

    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            setViewConnected();
            mRemoteControl.onGoogleApiClientConnected(NowPlayingActivity.this, mGoogleApiClient);
        }

        @Override
        public void onConnectionSuspended(final int i) {
            setViewConnecting();
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener
            = connectionResult -> {
        mModelViewState.setProgressVisible(false);
        mModelViewState.setMessage(GooglePlayServicesUtil
                .toHumanReadableMessage(getResources(), connectionResult.getErrorCode()));
        mModelViewState.setFixButtonVisible(connectionResult.hasResolution());
        if (connectionResult.hasResolution()) {
            mBtnFix.setOnClickListener(v -> {
                try {
                    connectionResult.startResolutionForResult(this, REQUEST_CODE_GOOGLE_API);
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(this, R.string.Could_not_fix_this_issue, Toast.LENGTH_LONG)
                            .show();
                    mModelViewState.setFixButtonVisible(false);
                }
            });
        }
        mModelViewState.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);
    };
}
