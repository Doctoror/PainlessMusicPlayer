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
package com.doctoror.fuckoffmusicplayer.library.recent;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;
import com.doctoror.fuckoffmusicplayer.util.Log;
import com.l4digital.fastscroll.FastScrollRecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 26.10.16.
 */

public final class RecentTracksFragment extends LibraryListFragment {

    private final Object CURSOR_LOCK = new Object();

    private RecentTracksRecyclerAdapter mAdapter;
    private Cursor mData;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new RecentTracksRecyclerAdapter(getActivity());
        mAdapter.setOnTrackClickListener(this::playTrack);
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_tracks_found));
    }

    @Override
    protected void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.setupRecyclerView(recyclerView);
        if (recyclerView instanceof FastScrollRecyclerView) {
            ((FastScrollRecyclerView) recyclerView).setFastScrollEnabled(false);
        }
        recyclerView.setVerticalScrollBarEnabled(true);
    }

    @Override
    protected RxCursorLoader.Query newQuery(@Nullable final String filter) {
        return RecentTracksQuery.newParams(filter);
    }

    @Override
    protected void onDataLoaded(@Nullable final Cursor data) {
        synchronized (CURSOR_LOCK) {
            mData = data;
            mAdapter.swapCursor(data);
        }
    }

    @Override
    protected void onDataReset() {
        synchronized (CURSOR_LOCK) {
            mData = null;
            mAdapter.swapCursor(null);
        }
    }

    private void playTrack(final int startPosition, final long trackId) {
        Observable.<List<Media>>create(s -> {
            final long[] tracks;
            synchronized (CURSOR_LOCK) {
                final Cursor data = mData;
                if (data != null) {
                    int limit = 50;
                    final int count = data.getCount();
                    if (startPosition + limit > count) {
                        limit = count - startPosition;
                    }
                    tracks = new long[limit];
                    for (int trackIndex = 0, i = startPosition; i < startPosition + limit;
                            trackIndex++, i++) {
                        if (data.moveToPosition(i)) {
                            tracks[trackIndex] = data.getLong(RecentTracksQuery.COLUMN_ID);
                        } else {
                            throw new RuntimeException("Could not move Cursor to position " + i);
                        }
                    }
                } else {
                    s.onError(new IllegalStateException("Cursor is null"));
                    return;
                }
            }

            s.onNext(PlaylistUtils.forTracks(getActivity().getContentResolver(),
                    tracks, RecentTracksQuery.SORT_ORDER));

        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Media>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(final Throwable e) {
                        Log.w("RecentTracksFragment", e);
                    }

                    @Override
                    public void onNext(final List<Media> playlist) {
                        if (isAdded()) {
                            if (playlist != null && !playlist.isEmpty()) {
                                PlaylistUtils.play(getActivity(), playlist, playlist.get(0), 0);
                            } else {
                                Toast.makeText(getActivity(), R.string.The_playlist_is_empty,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}
