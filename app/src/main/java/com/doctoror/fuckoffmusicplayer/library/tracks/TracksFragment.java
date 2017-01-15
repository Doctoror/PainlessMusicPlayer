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
package com.doctoror.fuckoffmusicplayer.library.tracks;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.QueueConfig;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderTracks;
import com.doctoror.fuckoffmusicplayer.db.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueUtils;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * "Tracks" list fragment
 */
public final class TracksFragment extends LibraryListFragment {

    private final Object CURSOR_LOCK = new Object();

    private TracksRecyclerAdapter mAdapter;
    private Cursor mData;

    @Inject
    TracksProvider mTracksProvider;

    @Inject
    PlaylistProviderTracks mPlaylistFactory;

    @Inject
    PlaybackData mPlaybackData;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);

        mAdapter = new TracksRecyclerAdapter(getActivity());
        mAdapter.setOnTrackClickListener(this::playTrack);
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_tracks_found));
    }

    @Override
    protected Observable<Cursor> load(@Nullable final String filter) {
        return mTracksProvider.load(filter);
    }

    @Override
    protected void onDataLoaded(@NonNull final Cursor data) {
        synchronized (CURSOR_LOCK) {
            mData = data;
            mAdapter.changeCursor(data);
        }
    }

    @Override
    protected void onDataReset() {
        synchronized (CURSOR_LOCK) {
            mData = null;
            mAdapter.changeCursor(null);
        }
    }

    @NonNull
    private long[] createLimitedPlaylist(final int startPosition) {
        final long[] tracks;
        synchronized (CURSOR_LOCK) {
            final Cursor data = mData;
            if (data != null) {
                int limit = QueueConfig.MAX_PLAYLIST_SIZE;
                final int count = data.getCount();
                if (startPosition + limit > count) {
                    limit = count - startPosition;
                }
                tracks = new long[limit];
                for (int trackIndex = 0, i = startPosition; i < startPosition + limit;
                        trackIndex++, i++) {
                    if (data.moveToPosition(i)) {
                        tracks[trackIndex] = data.getLong(TracksProvider.COLUMN_ID);
                    } else {
                        throw new RuntimeException("Could not move Cursor to position " + i);
                    }
                }
            } else {
                throw new IllegalStateException("Cursor is null");
            }
        }
        return tracks;
    }

    @NonNull
    private Observable<List<Media>> queueFromIds(@NonNull final long[] ids) {
        return mPlaylistFactory.fromTracks(ids, MediaStoreTracksProvider.SORT_ORDER);
    }

    private void playTrack(@NonNull final View itemView,
            final int startPosition,
            final long trackId) {
        Observable.<long[]>create(s -> s.onNext(createLimitedPlaylist(startPosition)))
                .flatMap(this::queueFromIds)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(p -> onPlaylistLoaded(itemView, p));
    }

    private void onPlaylistLoaded(@NonNull final View itemView,
            @Nullable final List<Media> p) {
        if (isAdded()) {
            if (p != null && !p.isEmpty()) {
                QueueUtils.play(getActivity(), mPlaybackData, p, 0);
                NowPlayingActivity.start(getActivity(), null, itemView);
            } else {
                Toast.makeText(getActivity(), R.string.The_queue_is_empty,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
