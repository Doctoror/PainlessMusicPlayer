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
package com.doctoror.fuckoffmusicplayer.wear.playlist;

import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentPlaylistBinding;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventAlbumArt;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventMedia;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventPlaylist;
import com.doctoror.fuckoffmusicplayer.wear.media.MediaHolder;
import com.doctoror.fuckoffmusicplayer.wear.remote.RemoteControl;
import com.doctoror.fuckoffmusicplayer.wear.root.RootActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import android.app.Activity;
import android.app.Fragment;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
 * "Playlist" screen
 */
public final class PlaylistFragment extends Fragment {

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
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMedia(@NonNull final EventMedia event) {
        // If playlist is a fake list of single media, update it
        if (mAdapter.getItemCount() == 1) {
            bindPlaylist(mPlaylistHolder.getPlaylist(), event.media);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventAlbumArt(@NonNull final EventAlbumArt event) {
        mModel.setBackground(albumArtOrStub(event.albumArt));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventPlaylist(@NonNull final EventPlaylist event) {
        bindPlaylist(event.playlist, mMediaHolder.getMedia());
    }

    private void playMediaFromPlaylist(final long mediaId) {
        RemoteControl.getInstance().playMediaFromPlaylist(mediaId);
        final Activity activity = getActivity();
        if (activity instanceof RootActivity) {
            ((RootActivity) activity).goToNowPlaying();
        }
    }

    @MainThread
    private void bindPlaylist(@Nullable final WearPlaybackData.Playlist playlist,
            @Nullable final WearPlaybackData.Media media) {
        mAdapter.setItems(makePlaylist(playlist, media));
        mModel.setIsEmpty(mAdapter.getItemCount() == 0);
        if (mRecyclerView != null && media != null && playlist != null) {
            mRecyclerView.scrollToPosition(media.positionInPlaylist);
        }
    }

    @NonNull
    private static List<WearPlaybackData.Media> makePlaylist(
            @Nullable final WearPlaybackData.Playlist playlist,
            @Nullable final WearPlaybackData.Media media) {
        List<WearPlaybackData.Media> p = null;
        if (playlist != null) {
            final WearPlaybackData.Media[] medias = playlist.media;
            if (medias != null && medias.length != 0) {
                p = Arrays.asList(medias);
            }
        }

        if (p == null) {
            p = new ArrayList<>(media == null ? 0 : 1);
            if (media != null) {
                p.add(media);
            }
        }

        return p;
    }

    private Drawable albumArtOrStub(@Nullable final Bitmap art) {
        if (art == null) {
            return getActivity().getDrawable(R.drawable.album_art_placeholder);
        }
        return new BitmapDrawable(getResources(), art);
    }
}
