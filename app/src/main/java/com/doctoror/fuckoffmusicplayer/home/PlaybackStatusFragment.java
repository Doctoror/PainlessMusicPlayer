/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.home;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.base.BaseFragment;
import com.doctoror.fuckoffmusicplayer.databinding.PlaybackStatusBarBinding;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.util.BindingAdapters;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Playback status bar fragment
 */
public final class PlaybackStatusFragment extends BaseFragment {

    private final PlaybackStatusBarModel mModel = new PlaybackStatusBarModel();

    @Inject
    PlaybackData mPlaybackData;

    @Inject
    PlaybackServiceControl mPlaybackServiceControl;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        final PlaybackStatusBarBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.playback_status_bar, container, false,
                BindingAdapters.glideBindingComponent(Glide.with(this)));
        binding.setModel(mModel);
        binding.btnPlay.setOnClickListener(v -> onBtnPlayClick());
        binding.getRoot().setOnClickListener(this::onRootClick);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        disposeOnStop(mPlaybackData.playbackStateObservable()
                .subscribe(this::onStateChanged));

        disposeOnStop(mPlaybackData.queuePositionObservable()
                .subscribe(this::onQueuePositionChanged));
    }

    private void onStateChanged(@PlaybackState final int state) {
        mModel.setBtnPlayRes(state == PlaybackState.STATE_PLAYING
                ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
    }

    private void onQueuePositionChanged(final int position) {
        final List<Media> queue = mPlaybackData.getQueue();
        if (queue != null && position < queue.size()) {
            onMediaChanged(queue.get(position));
        }
    }

    private void onMediaChanged(@Nullable final Media media) {
        if (media != null) {
            mModel.setTitle(media.getTitle());
            mModel.setArtist(media.getArtist());
            mModel.setImageUri(media.getAlbumArt());
        }
    }

    private void onBtnPlayClick() {
        mPlaybackServiceControl.playPause();
    }

    private void onRootClick(@NonNull final View view) {
        NowPlayingActivity.start(getActivity(), null, view);
    }
}
