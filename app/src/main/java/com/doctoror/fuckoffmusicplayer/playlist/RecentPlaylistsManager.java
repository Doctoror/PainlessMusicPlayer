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

import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.playlist.nano.RecentPlaylists;
import com.doctoror.fuckoffmusicplayer.util.CollectionUtils;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.Collection;
import java.util.Queue;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Used for storing recent playlists
 */
public final class RecentPlaylistsManager {

    private static final int MAX_LENGTH = 10;

    private static final String FILE_NAME_RECENT_PLAYLISTS = "recent_playlists_albums";


    // Is not a leak since it's an application context
    @SuppressLint("StaticFieldLeak")
    private static volatile RecentPlaylistsManager sInstance;

    @NonNull
    public static RecentPlaylistsManager getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (RecentPlaylistsManager.class) {
                if (sInstance == null) {
                    sInstance = new RecentPlaylistsManager(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final Object LOCK = new Object();

    @NonNull
    private final Context mContext;

    @NonNull
    private final Queue<Long> mRecentAlbums = new CircularFifoQueue<>(MAX_LENGTH);

    private RecentPlaylistsManager(@NonNull final Context context) {
        mContext = context;
        read();
    }

    public void clear() {
        synchronized (LOCK) {
            mRecentAlbums.clear();
        }
        persist();
    }

    private void read() {
        final RecentPlaylists.Albums albums = ProtoUtils
                .readFromFile(mContext, FILE_NAME_RECENT_PLAYLISTS, new RecentPlaylists.Albums());
        if (albums != null) {
            final long[] ids = albums.ids;
            if (ids != null) {
                synchronized (LOCK) {
                    //noinspection ForLoopReplaceableByForEach
                    for (int i = 0; i < ids.length; i++) {
                        mRecentAlbums.add(ids[i]);
                    }
                }
            }
        }
    }

    public void storeAlbum(final long albumId) {
        if (albumId > 0) {
            storeAlbumInternal(albumId, true);
        }
    }

    @WorkerThread
    public void storeAlbumsSync(@NonNull final Collection<Long> albumIds) {
        boolean result = false;
        for (final Long albumId : albumIds) {
            if (albumId > 0) {
                result |= storeAlbumInternal(albumId, false);
            }
        }
        if (result) {
            persistSync();
        }
    }

    private boolean storeAlbumInternal(final long albumId, boolean persist) {
        boolean result = false;
        synchronized (LOCK) {
            // If already contains
            final long[] array = CollectionUtils.toLongArray(mRecentAlbums);
            if (array.length == 0 || array[array.length - 1] != albumId) {
                final Long albumIdLong = albumId;
                if (mRecentAlbums.contains(albumIdLong)) {
                    // Remove duplicate
                    if (mRecentAlbums.remove(albumIdLong)) {
                        // Add to head
                        result = mRecentAlbums.add(albumIdLong);
                    }
                } else {
                    // Does not contain. Add.
                    result = mRecentAlbums.add(albumIdLong);
                }
            }
        }
        if (persist && result) {
            persist();
        }
        return result;
    }

    @NonNull
    public long[] getRecentAlbums() {
        synchronized (LOCK) {
            return CollectionUtils.toReverseLongArray(mRecentAlbums);
        }
    }

    private void persist() {
        Observable.create(s -> persistSync()).subscribeOn(Schedulers.io()).subscribe();
    }

    @WorkerThread
    private void persistSync() {
        synchronized (LOCK) {
            final RecentPlaylists.Albums albums = new RecentPlaylists.Albums();
            albums.ids = CollectionUtils.toLongArray(mRecentAlbums);
            ProtoUtils.writeToFile(mContext, FILE_NAME_RECENT_PLAYLISTS, albums);
        }
    }
}
