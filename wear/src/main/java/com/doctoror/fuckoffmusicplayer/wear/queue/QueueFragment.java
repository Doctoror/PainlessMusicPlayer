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
package com.doctoror.fuckoffmusicplayer.wear.queue;

import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentQueueBinding;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventAlbumArt;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventMedia;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventQueue;
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
 * "Queue" screen
 */
public final class QueueFragment extends Fragment {

    private final QueueFragmentModel mModel = new QueueFragmentModel();

    private MediaHolder mMediaHolder;
    private QueueHolder mQueueHolder;

    private QueueListAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaHolder = MediaHolder.getInstance(getActivity());
        mQueueHolder = QueueHolder.getInstance(getActivity());

        mAdapter = new QueueListAdapter(getActivity());
        mAdapter.setOnMediaClickListener(this::playMediaFromQueue);
        mModel.setAdapter(mAdapter);
        mModel.setIsEmpty(true);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
            final Bundle savedInstanceState) {
        final FragmentQueueBinding binding = DataBindingUtil
                .inflate(inflater, R.layout.fragment_queue, container, false);
        binding.setModel(mModel);
        mRecyclerView = binding.list;
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mModel.setBackground(albumArtOrStub(mMediaHolder.getAlbumArt()));
        bindQueue(mQueueHolder.getQueue(), mMediaHolder.getMedia());
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMedia(@NonNull final EventMedia event) {
        // If queue is a fake list of single media, update it
        if (mAdapter.getItemCount() == 1) {
            bindQueue(mQueueHolder.getQueue(), event.media);
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventAlbumArt(@NonNull final EventAlbumArt event) {
        mModel.setBackground(albumArtOrStub(event.albumArt));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventQueue(@NonNull final EventQueue event) {
        bindQueue(event.queue, mMediaHolder.getMedia());
    }

    private void playMediaFromQueue(final long mediaId) {
        RemoteControl.getInstance().playMediaFromQueue(mediaId);
        final Activity activity = getActivity();
        if (activity instanceof RootActivity) {
            ((RootActivity) activity).goToNowPlaying();
        }
    }

    @MainThread
    private void bindQueue(@Nullable final WearPlaybackData.Queue queue,
            @Nullable final WearPlaybackData.Media media) {
        mAdapter.setItems(makeQueue(queue, media));
        mModel.setIsEmpty(mAdapter.getItemCount() == 0);
        if (mRecyclerView != null && media != null && queue != null) {
            mRecyclerView.scrollToPosition(media.positionInQueue);
        }
    }

    @NonNull
    private static List<WearPlaybackData.Media> makeQueue(
            @Nullable final WearPlaybackData.Queue queue,
            @Nullable final WearPlaybackData.Media media) {
        List<WearPlaybackData.Media> p = null;
        if (queue != null) {
            final WearPlaybackData.Media[] medias = queue.media;
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
