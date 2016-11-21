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
package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.RemoteControl;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentPlaylistBinding;
import com.doctoror.fuckoffmusicplayer.media.MediaHolder;
import com.doctoror.fuckoffmusicplayer.root.RootActivity;

import android.app.Activity;
import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class PlaylistFragment extends Fragment {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final PlaylistFragmentModel mModel = new PlaylistFragmentModel();

    private MediaHolder mMediaHolder;
    private PlaylistHolder mPlaylistHolder;

    private PlaylistListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaHolder = MediaHolder.getInstance(getActivity());
        mPlaylistHolder = PlaylistHolder.getInstance(getActivity());

        mAdapter = new PlaylistListAdapter(getActivity());
        mAdapter.setOnMediaClickListener(this::playMediaFromPlaylist);
        mModel.setAdapter(mAdapter);
        mModel.setIsEmpty(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final FragmentPlaylistBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_playlist, container, false);
        binding.setModel(mModel);
        mRecyclerView = binding.list;
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mModel.setBackground(albumArtOrStub(mMediaHolder.getAlbumArt()));
        bindPlaylist(mPlaylistHolder.getPlaylist(), mMediaHolder.getMedia());
        mMediaHolder.addObserver(mPlaybackInfoObserver);
        mPlaylistHolder.addObserver(mPlaylistObserver);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPlaylistHolder.deleteObserver(mPlaylistObserver);
        mMediaHolder.deleteObserver(mPlaybackInfoObserver);
    }

    private void playMediaFromPlaylist(final long mediaId) {
        RemoteControl.getInstance().playMediaFromPlaylist(mediaId);
        final Activity activity = getActivity();
        if (activity instanceof RootActivity) {
            ((RootActivity) activity).goToNowPlaying();
        }
    }

    @MainThread
    private void bindPlaylist(@Nullable final ProtoPlaybackData.Playlist playlist,
            @Nullable final ProtoPlaybackData.Media media) {
        mAdapter.setItems(makePlaylist(playlist, media));
        mModel.setIsEmpty(mAdapter.getItemCount() == 0);
        if (mRecyclerView != null && media != null && playlist != null) {
            mRecyclerView.scrollToPosition(media.playlistPosition);
        }
    }

    @NonNull
    private static List<ProtoPlaybackData.Media> makePlaylist(
            @Nullable final ProtoPlaybackData.Playlist playlist,
            @Nullable final ProtoPlaybackData.Media media) {
        List<ProtoPlaybackData.Media> p = null;
        if (playlist != null) {
            final ProtoPlaybackData.Media[] medias = playlist.media;
            if (medias != null && medias.length != 0) {
                p = Arrays.asList(medias);
            }
        }

        if (p == null) {
            p = new ArrayList<>(1);
            p.add(media);
        }

        return p;
    }

    private Drawable albumArtOrStub(@Nullable final Bitmap art) {
        if (art == null) {
            return getActivity().getDrawable(R.drawable.album_art_placeholder);
        }
        return new BitmapDrawable(getResources(), art);
    }

    private final PlaylistHolder.PlaylistObserver mPlaylistObserver
            = new PlaylistHolder.PlaylistObserver() {

        @Override
        public void onPlaylistChanged(@Nullable final ProtoPlaybackData.Playlist playlist) {
            //noinspection WrongThread
            mHandler.post(() -> bindPlaylist(playlist, mMediaHolder.getMedia()));
        }
    };

    private final MediaHolder.PlaybackInfoObserver mPlaybackInfoObserver
            = new MediaHolder.PlaybackInfoObserver() {

        @Override
        public void onMediaChanged(@Nullable final ProtoPlaybackData.Media media) {
            // If playlist is a fake list of single media, update it
            if (mAdapter.getItemCount() == 1) {
                //noinspection WrongThread
                mHandler.post(() -> bindPlaylist(mPlaylistHolder.getPlaylist(), media));
            }
        }

        @Override
        public void onPlaybackStateChanged(
                @Nullable final ProtoPlaybackData.PlaybackState playbackState) {

        }

        @Override
        public void onAlbumArtChanged(@Nullable final Bitmap albumArt) {
            mModel.setBackground(albumArtOrStub(albumArt));
        }
    };
}
