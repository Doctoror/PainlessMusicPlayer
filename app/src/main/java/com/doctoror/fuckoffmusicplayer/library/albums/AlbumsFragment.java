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
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistActivity;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.fuckoffmusicplayer.widget.SpacesItemDecoration;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class AlbumsFragment extends LibraryListFragment {

    private AlbumsRecyclerAdapter mAdapter;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new AlbumsRecyclerAdapter(getActivity(), Glide.with(this));
        mAdapter.setOnAlbumClickListener(this::onAlbumClick);
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_albums_found));
    }

    @Override
    protected void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        recyclerView.addItemDecoration(new SpacesItemDecoration(
                (int) getResources().getDimension(R.dimen.album_grid_spacing)));
    }

    @Override
    protected RxCursorLoader.Query newQuery(@Nullable final String filter) {
        return AlbumsQuery.newParams(filter);
    }

    @Override
    protected void onDataLoaded(@Nullable final Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    protected void onDataReset() {
        mAdapter.swapCursor(null);
    }

    private void onAlbumClick(@NonNull final View view,
            final long albumId,
            @Nullable final String albumName,
            final String art) {
        Observable.<List<Media>>create(s -> s.onNext(PlaylistUtils.fromAlbum(
                getActivity().getContentResolver(), albumId, art)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((playlist) -> {
                    if (isAdded()) {
                        if (playlist != null && !playlist.isEmpty()) {
                            final Activity activity = getActivity();
                            final Intent intent = Henson.with(activity).gotoPlaylistActivity()
                                    .hasCoverTransition(true)
                                    .hasItemViewTransition(false)
                                    .isNowPlayingPlaylist(false)
                                    .playlist(playlist)
                                    .title(albumName)
                                    .build();

                            final ActivityOptionsCompat options = ActivityOptionsCompat
                                    .makeSceneTransitionAnimation(activity, view,
                                            PlaylistActivity.TRANSITION_NAME_ALBUM_ART);
                            startActivity(intent, options.toBundle());
                        } else {
                            Toast.makeText(getActivity(), R.string.The_playlist_is_empty,
                                    Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }
}
