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

import com.doctoror.commons.util.StringUtils;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.remote.RemoteControl;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentNowPlayingBinding;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;

import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public final class NowPlayingFragment extends Fragment {

    private final NowPlayingFragmentModelPlaybackState mModelPlaybackState
            = new NowPlayingFragmentModelPlaybackState();

    private final NowPlayingFragmentModelViewState mModelViewState
            = new NowPlayingFragmentModelViewState();

    private final NowPlayingFragmentModelMedia mModelMedia
            = new NowPlayingFragmentModelMedia();

    private final RemoteControl mRemoteControl = RemoteControl.getInstance();

    private MediaHolder mMediaHolder;

    private volatile boolean mSeekBarTracking;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModelViewState.setBtnPlayRes(R.drawable.ic_play_arrow_white_24dp);
        mMediaHolder = MediaHolder.getInstance(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final FragmentNowPlayingBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_now_playing, container, false);
        binding.setPlaybackState(mModelPlaybackState);
        binding.setViewState(mModelViewState);
        binding.setMedia(mModelMedia);
        binding.seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerImpl());
        binding.btnPrev.setOnClickListener(v -> mRemoteControl.prev());
        binding.btnNext.setOnClickListener(v -> mRemoteControl.next());
        binding.btnPlayPause.setOnClickListener(v -> mRemoteControl.playPause());
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        bindArt(mMediaHolder.getAlbumArt());
        bindMedia(mMediaHolder.getMedia());
        bindPlaybackState(mMediaHolder.getPlaybackState());
        mMediaHolder.addObserver(mPlaybackInfoObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mMediaHolder.deleteObserver(mPlaybackInfoObserver);
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
            mModelMedia.setArt(getActivity().getDrawable(R.drawable.album_art_placeholder));
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
            mRemoteControl.seek((float) seekBar.getProgress() / (float) seekBar.getMax());
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
}
