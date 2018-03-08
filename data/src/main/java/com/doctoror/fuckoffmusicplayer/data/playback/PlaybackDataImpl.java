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
package com.doctoror.fuckoffmusicplayer.data.playback;

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.data.concurrent.Handlers;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_IDLE;

/**
 * Default {@link PlaybackData} implementation
 */
public final class PlaybackDataImpl implements PlaybackData {

    private final BehaviorSubject<List<Media>> mQueueSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> mQueuePositionSubject = BehaviorSubject.create();
    private final BehaviorSubject<Long> mMediaPositionSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> mPlaybackStateSubject =
            BehaviorSubject.createDefault(STATE_IDLE);

    private final Object mQueueSubjectLock = new Object();
    private final Object mQueuePositionSubjectLock = new Object();
    private final Object mMediaPositionSubjectLock = new Object();
    private final Object mPlaybackStateSubjectLock = new Object();

    @NonNull
    private final Context mContext;

    @NonNull
    private final RecentActivityManager mRecentActivityManager;

    public PlaybackDataImpl(@NonNull final Context context,
            @NonNull final RecentActivityManager recentActivityManager) {
        mContext = context;
        mRecentActivityManager = recentActivityManager;
        PlaybackDataPersister.restoreFromFile(context, this);
    }

    @NonNull
    @Override
    public Observable<List<Media>> queueObservable() {
        return mQueueSubject.hide();
    }

    @NonNull
    @Override
    public Observable<Integer> queuePositionObservable() {
        return mQueuePositionSubject.hide();
    }

    @NonNull
    @Override
    public Observable<Long> mediaPositionObservable() {
        return mMediaPositionSubject.hide();
    }

    @NonNull
    @Override
    public Observable<Integer> playbackStateObservable() {
        return mPlaybackStateSubject.hide();
    }

    @Nullable
    @Override
    public List<Media> getQueue() {
        synchronized (mQueueSubjectLock) {
            final List<Media> queue = mQueueSubject.getValue();
            return queue != null ? new ArrayList<>(queue) : null;
        }
    }

    @NonNull
    @Override
    public Integer getQueuePosition() {
        synchronized (mQueuePositionSubjectLock) {
            final Integer value = mQueuePositionSubject.getValue();
            return value != null ? value : 0;
        }
    }

    @NonNull
    @Override
    public Long getMediaPosition() {
        synchronized (mMediaPositionSubjectLock) {
            final Long value = mMediaPositionSubject.getValue();
            return value != null ? value : 0L;
        }
    }

    @Override
    public int getPlaybackState() {
        synchronized (mPlaybackStateSubjectLock) {
            final Integer value = mPlaybackStateSubject.getValue();
            //noinspection WrongConstant
            return value != null ? value : PlaybackState.STATE_IDLE;
        }
    }

    @Override
    public void setPlayQueue(@Nullable final List<Media> queue) {
        synchronized (mQueueSubjectLock) {
            final List<Media> newQueue = new ArrayList<>(queue != null ? queue.size() : 0);
            if (queue != null) {
                newQueue.addAll(queue);
            }
            final int pos = getQueuePosition();
            final Media current = CollectionUtils.getItemSafe(getQueue(), pos);

            mQueueSubject.onNext(newQueue);

            Handlers.runOnIoThread(() -> storeToRecentAlbums(newQueue));

            if (!newQueue.isEmpty() && current != null) {
                final int newPos = newQueue.indexOf(current);
                if (newPos != -1 && pos != newPos) {
                    setPlayQueuePosition(newPos);
                }
            }
        }
    }

    @Override
    public void setPlayQueuePosition(final int position) {
        synchronized (mQueuePositionSubjectLock) {
            mQueuePositionSubject.onNext(position);
        }
    }

    @Override
    public void setMediaPosition(final long position) {
        synchronized (mMediaPositionSubjectLock) {
            mMediaPositionSubject.onNext(position);
        }
    }

    @Override
    public void setPlaybackState(@PlaybackState final int state) {
        synchronized (mPlaybackStateSubjectLock) {
            mPlaybackStateSubject.onNext(state);
        }
    }

    @Override
    public void persistAsync() {
        PlaybackDataPersister.persistAsync(mContext, this);
    }

    /**
     * Finds albums in queue and stores them in recently played albums.
     * Album is a sequence of tracks with the same album id.
     * Single playlist can be a concatination of multiple albums.
     *
     * @param queue The queue to process
     */
    @WorkerThread
    private void storeToRecentAlbums(@Nullable final List<Media> queue) {
        if (queue != null && !queue.isEmpty()) {
            final Set<Long> albums = new LinkedHashSet<>();
            final long THRESHOLD = 4; // number of items per single album
            long prevAlbumId = -1;
            int sequence = 1;
            boolean first = true;
            for (final Media item : queue) {
                if (first) {
                    first = false;
                    prevAlbumId = item.getAlbumId();
                } else {
                    final long albumId = item.getAlbumId();
                    if (albumId == prevAlbumId) {
                        sequence++;
                    } else {
                        if (sequence >= THRESHOLD) {
                            albums.add(prevAlbumId);
                        }
                        sequence = 1;
                        prevAlbumId = albumId;
                    }
                }
            }
            if (sequence >= THRESHOLD) {
                albums.add(prevAlbumId);
            }
            mRecentActivityManager.onAlbumsPlayed(albums);
        }
    }
}
