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

import com.bumptech.glide.Glide;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentNowplayingBinding;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.Playlist;
import com.doctoror.fuckoffmusicplayer.util.BindingAdapters;
import com.jakewharton.rxbinding.widget.RxSeekBar;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.functions.Action1;

/**
 * Created by Yaroslav Mytkalyk on 21.10.16.
 */

public final class NowPlayingFragment extends Fragment {

    private final NowPlayingFragmentModel mModel = new NowPlayingFragmentModel();
    private final Receiver mReceiver = new Receiver();
    private Playlist mPlaylist;

    private int mState = PlaybackService.STATE_IDLE;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();
        mPlaylist = Playlist.getInstance(context);
        bindTrack(mPlaylist.getMedia());
        mPlaylist.addObserver(mPlaylistObserver);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                mReceiver, mReceiver.mIntentFilter);
        mModel.setBtnPlayRes(R.drawable.ic_play_arrow_white_36dp);
        PlaybackService.resendState(context);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentNowplayingBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_nowplaying, container, false,
                BindingAdapters.glideBindingComponent(Glide.with(this)));
        binding.setModel(mModel);

        final AppCompatActivity a = (AppCompatActivity) getActivity();
        a.setSupportActionBar(binding.toolbar);

        RxSeekBar.userChanges(binding.seekBar).subscribe(new Action1<Integer>() {

            private boolean mFirst = true;

            @Override
            public void call(final Integer progress) {
                if (mFirst) {
                    mFirst = false;
                } else {
                    PlaybackService.seek(getActivity(), (float) progress / 200f);
                }
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Playlist.getInstance(getActivity()).deleteObserver(mPlaylistObserver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    void bindTrack(@Nullable Media track) {
        if (track != null) {
            mModel.setArt(track.getAlbumArt());
            mModel.setArtist(track.getArtist());
            mModel.setAlbum(track.getAlbum());
            mModel.setTitle(track.getTitle());
            mModel.setDuration(track.getDuration());
            mModel.setProgress(0);
            mModel.notifyChange();
        }
    }

    void bindProgress(final long progress) {
        mModel.setElapsedTime(progress);
        final long duration = mModel.getDuration();
        if (duration > 0) {
            // Max is 200 so progress is a fraction of 200
            mModel.setProgress((int) (((double) progress / (double) duration) * 200f));
        }
    }

    void bindState(final int state) {
        mState = state;
        final int playBtnRes;
        switch (state) {
            case PlaybackService.STATE_IDLE:
                playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                break;

            case PlaybackService.STATE_LOADING:
                playBtnRes = R.drawable.ic_pause_white_36dp;
                break;

            case PlaybackService.STATE_PLAYING:
                playBtnRes = R.drawable.ic_pause_white_36dp;
                break;

            case PlaybackService.STATE_PAUSED:
                playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                break;

            default:
                playBtnRes = R.drawable.ic_play_arrow_white_36dp;
                break;
        }
        mModel.setBtnPlayRes(playBtnRes);
    }

    @OnClick(R.id.btnPlay)
    public void onPlayClick() {
        switch (mState) {
            case PlaybackService.STATE_IDLE:
                PlaybackService.play(getActivity());
                break;

            case PlaybackService.STATE_PAUSED:
                PlaybackService.play(getActivity());
                break;

            case PlaybackService.STATE_PLAYING:
                PlaybackService.pause(getActivity());
                break;
        }
    }

    @OnClick(R.id.btnPrev)
    public void onPrevClick() {
        PlaybackService.prev(getActivity());
    }

    @OnClick(R.id.btnNext)
    public void onNextClick() {
        PlaybackService.next(getActivity());
    }

    private final Playlist.PlaylistObserver mPlaylistObserver = new Playlist.PlaylistObserver() {

        @Override
        public void onPositionChanged(final long position) {
            bindProgress(position);
        }

        @Override
        public void onMediaChanged(final Media media) {
            bindTrack(media);
        }

        @Override
        public void onMediaRemoved(final Media media) {
            final List<Media> playlist = mPlaylist.getPlaylist();
            if (playlist == null || playlist.isEmpty()) {
                if (isAdded()) {
                    getActivity().finish();
                }
            }
        }
    };

    private final class Receiver extends BroadcastReceiver {

        final IntentFilter mIntentFilter = new IntentFilter();

        Receiver() {
            mIntentFilter.addAction(PlaybackService.ACTION_STATE_CHANGED);
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            switch (intent.getAction()) {
                case PlaybackService.ACTION_STATE_CHANGED:
                    bindState(intent.getIntExtra(PlaybackService.EXTRA_STATE, 0));
                    break;
            }
        }
    }
}
