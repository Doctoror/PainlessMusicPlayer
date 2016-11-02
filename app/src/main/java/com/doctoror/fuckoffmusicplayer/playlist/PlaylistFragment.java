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

import com.bumptech.glide.Glide;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.FragmentPlaylistBinding;
import com.doctoror.fuckoffmusicplayer.util.BindingAdapters;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.app.Fragment;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Yaroslav Mytkalyk on 20.10.16.
 */
public final class PlaylistFragment extends Fragment {

    @NonNull
    public static PlaylistFragment instantiate(@NonNull final Context context,
            @NonNull final List<Media> playlist,
            @NonNull final Boolean isNowPlayingPlaylist) {
        final PlaylistFragment fragment = new PlaylistFragment();
        final Bundle extras = Henson.with(context).gotoPlaylistFragment()
                .isNowPlayingPlaylist(isNowPlayingPlaylist)
                .playlist(playlist)
                .build()
                .getExtras();
        fragment.setArguments(extras);
        return fragment;
    }

    private final PlaylistFragmentModel mModel = new PlaylistFragmentModel();
    private PlaylistRecyclerAdapter mAdapter;

    private Playlist mPlaylist;

    @InjectExtra
    List<Media> playlist;

    @InjectExtra
    Boolean isNowPlayingPlaylist;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this, getArguments());
        mPlaylist = Playlist.getInstance(getActivity());

        mAdapter = new PlaylistRecyclerAdapter(getActivity(), playlist);
        mAdapter.setOnTrackClickListener(mOnTrackClickListener);
        mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
        mModel.setRecyclerAdpter(mAdapter);

        String pic = null;
        final int size = playlist.size();
        for (int i = 0; i < size; i++) {
            final Media media = playlist.get(i);
            pic = media.albumArt;
            if (pic != null) {
                break;
            }
        }
        mModel.setImageUri(pic);
    }

    private PlaylistActivity getPlaylistActivity() {
        return (PlaylistActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        final FragmentPlaylistBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_playlist, container, false,
                BindingAdapters.glideBindingComponent(Glide.with(this)));
        binding.setModel(mModel);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperImpl(
                (PlaylistRecyclerAdapter) mModel.getRecyclerAdapter().get()));
        itemTouchHelper.attachToRecyclerView(binding.recyclerView);
        getPlaylistActivity().setSupportActionBar(binding.toolbar);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    @OnClick(R.id.fab)
    public void onFabClick() {
        onPlayClick(playlist.get(0), 0);
    }

    private void onPlayClick(final Media media, final int index) {
        PlaylistUtils.play(getActivity(), playlist, media, index);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
    }

    private final PlaylistRecyclerAdapter.OnTrackClickListener mOnTrackClickListener
            = new PlaylistRecyclerAdapter.OnTrackClickListener() {

        @Override
        public void onTrackClick(@NonNull final Media media, final int position) {
            onPlayClick(media, position);
        }

        @Override
        public void onTrackDeleteClick(@NonNull final Media media) {
            getPlaylistActivity().onDeleteClickFromList(media);
        }
    };

    private final class ItemTouchHelperImpl extends ItemTouchHelper.SimpleCallback {

        @NonNull
        private final PlaylistRecyclerAdapter mAdapter;

        ItemTouchHelperImpl(@NonNull final PlaylistRecyclerAdapter adapter) {
            super(0, ItemTouchHelper.LEFT);
            mAdapter = adapter;
        }

        @Override
        public boolean onMove(final RecyclerView recyclerView,
                final RecyclerView.ViewHolder viewHolder,
                final RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public int getMovementFlags(final RecyclerView recyclerView,
                final RecyclerView.ViewHolder viewHolder) {
            final int position = viewHolder.getAdapterPosition();
            return mAdapter.canRemove(position) ?
                    super.getMovementFlags(recyclerView, viewHolder) : 0;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, final int swipeDir) {
            final int pos = viewHolder.getAdapterPosition();
            if (isNowPlayingPlaylist) {
                final Object item = mAdapter.getItem(pos);
                if (item instanceof Media) {
                    mPlaylist.remove((Media) item);
                }
            }
            mAdapter.setItemRemoved(pos);
        }
    }

    private final RecyclerView.AdapterDataObserver mAdapterDataObserver
            = new RecyclerView.AdapterDataObserver() {

        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(final int positionStart, final int itemCount) {
            checkIfEmpty();
        }

        private void checkIfEmpty() {
            if (mAdapter.getItemCount() == 0 && isAdded()) {
                getPlaylistActivity().onPlaylistEmpty();
            }
        }
    };
}
