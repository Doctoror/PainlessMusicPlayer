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
package com.doctoror.fuckoffmusicplayer.presentation.library.tracks;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueConfig;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks;
import com.doctoror.fuckoffmusicplayer.domain.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * "Tracks" list fragment.
 */
public final class TracksFragment extends LibraryListFragment {

    private final Object CURSOR_LOCK = new Object();

    private TracksRecyclerAdapter mAdapter;
    private Cursor mData;

    @Inject
    PlaybackInitializer mPlaybackInitializer;

    @Inject
    TracksProvider mTracksProvider;

    @Inject
    QueueProviderTracks mPlaylistFactory;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);

        mAdapter = new TracksRecyclerAdapter(getActivity());
        mAdapter.setOnTrackClickListener((startPosition, trackId) -> onTrackClick(startPosition));
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
    private long[] createLimitedQueue(final int startPosition) {
        final long[] tracks;
        synchronized (CURSOR_LOCK) {
            final Cursor data = mData;
            if (data != null) {
                int limit = QueueConfig.MAX_QUEUE_SIZE;
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
        return mPlaylistFactory.fromTracks(ids);
    }

    private void onTrackClick(final int startPosition) {
        disposeOnStop(Observable.fromCallable(() -> createLimitedQueue(startPosition))
                .flatMap(this::queueFromIds)
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        q -> onQueueLoaded(startPosition, q),
                        t -> onQueueEmpty()));
    }

    private void onQueueLoaded(final int startPosition,
                               @NonNull final List<Media> queue) {
        if (isAdded()) {
            if (queue.isEmpty()) {
                onQueueEmpty();
            } else {
                mPlaybackInitializer.setQueueAndPlay(queue, 0);
                NowPlayingActivity.start(getActivity(), null, getItemView(startPosition));
            }
        }
    }

    private void onQueueEmpty() {
        if (isAdded()) {
            Toast.makeText(getActivity(), R.string.The_queue_is_empty, Toast.LENGTH_LONG).show();
        }
    }
}
