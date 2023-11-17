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
package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.PlaybackStatusBarBinding;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseFragment;
import com.doctoror.fuckoffmusicplayer.presentation.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.presentation.util.AlbumArtIntoTargetApplier;
import com.doctoror.fuckoffmusicplayer.presentation.util.BindingAdapters;

import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * Playback status bar fragment
 */
public final class PlaybackStatusFragment extends BaseFragment {

    private final PlaybackStatusBarModel model = new PlaybackStatusBarModel();

    @Inject
    AlbumArtIntoTargetApplier albumArtIntoTargetApplier;

    @Inject
    PlaybackData playbackData;

    @Inject
    PlaybackServiceControl playbackServiceControl;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final PlaybackStatusBarBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.playback_status_bar, container, false,
                BindingAdapters.albumArtIntoTargetApplierComponent(albumArtIntoTargetApplier));
        binding.setModel(model);
        binding.btnPlay.setOnClickListener(v -> onBtnPlayClick());
        binding.getRoot().setOnClickListener(this::onRootClick);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        disposeOnStop(playbackData.playbackStateObservable()
                .subscribe(this::onStateChanged));

        disposeOnStop(playbackData.queuePositionObservable()
                .subscribe(this::onQueuePositionChanged));
    }

    private void onStateChanged(@NonNull final PlaybackState state) {
        model.setBtnPlayRes(state == PlaybackState.STATE_PLAYING
                ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);
    }

    private void onQueuePositionChanged(final int position) {
        final List<Media> queue = playbackData.getQueue();
        if (queue != null && position < queue.size()) {
            onMediaChanged(queue.get(position));
        }
    }

    private void onMediaChanged(@Nullable final Media media) {
        if (media != null) {
            model.setTitle(media.getTitle());
            model.setArtist(media.getArtist());
            model.setImageUri(media.getAlbumArt());
        }
    }

    private void onBtnPlayClick() {
        playbackServiceControl.playPause();
    }

    private void onRootClick(@NonNull final View view) {
        final Activity activity = getActivity();
        if (activity != null) {
            NowPlayingActivity.start(activity, null, view);
        }
    }
}
