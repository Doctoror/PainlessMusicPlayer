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
package com.doctoror.fuckoffmusicplayer.library.albums;

import com.bumptech.glide.Glide;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.widget.SpacesItemDecoration;

import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class AlbumsFragment extends LibraryListFragment {

    private static final String TAG_DIALOG_DELETE = "AlbumsFragment.TAG_DIALOG_DELETE";

    private AlbumsRecyclerAdapter mAdapter;

    private RecyclerView mRecyclerView;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Inject
    QueueProviderAlbums mQueueProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);

        mAdapter = new AlbumsRecyclerAdapter(getActivity(), Glide.with(this));
        mAdapter.setOnAlbumClickListener(new AlbumsRecyclerAdapter.OnAlbumClickListener() {

            @Override
            public void onAlbumClick(final int position, final long id, final String album) {
                AlbumsFragment.this.onAlbumClick(position, id, album);
            }

            @Override
            public void onAlbumDeleteClick(final long id, @Nullable final String name) {
                AlbumsFragment.this.onAlbumDeleteClick(id, name);
            }
        });
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_albums_found));
    }

    @Override
    protected void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        applyLayoutManager(recyclerView);
        recyclerView.addItemDecoration(new SpacesItemDecoration(
                (int) getResources().getDimension(R.dimen.album_grid_spacing)));
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mRecyclerView != null) {
            applyLayoutManager(mRecyclerView);
        }
    }

    private void applyLayoutManager(@NonNull final RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getResources().getInteger(R.integer.albums_grid_columns)));
    }

    @Override
    protected Observable<Cursor> load(@Nullable final String filter) {
        return mAlbumsProvider.load(filter);
    }

    @Override
    protected void onDataLoaded(@NonNull final Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    protected void onDataReset() {
        mAdapter.changeCursor(null);
    }

    private void onAlbumDeleteClick(final long albumId, @Nullable final String name) {
        DeleteAlbumDialogFragment.show(getActivity(), getFragmentManager(), TAG_DIALOG_DELETE,
                albumId, name);
    }

    private void onAlbumClick(final int position, final long albumId,
            @Nullable final String albumName) {
        AlbumClickHandler.onAlbumClick(this,
                mQueueProvider,
                albumId,
                albumName,
                () -> getItemView(position));
    }
}
