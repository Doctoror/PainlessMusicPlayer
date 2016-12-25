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

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Global playlist. Contains a playlist and current media.
 */
public final class CurrentPlaylist {

    // Is not a leak since it's an application context
    @SuppressLint("StaticFieldLeak")
    private static volatile CurrentPlaylist sInstance;

    @NonNull
    public static CurrentPlaylist getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (CurrentPlaylist.class) {
                if (sInstance == null) {
                    sInstance = new CurrentPlaylist(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private final Object mPlaylistLock = new Object();
    private final Object mObserversLock = new Object();

    private final List<PlaylistObserver> mObservers = new LinkedList<>();

    @NonNull
    private final Context mContext;

    List<Media> playlist;
    int index;
    Media media;
    long position;

    private CurrentPlaylist(@NonNull final Context context) {
        mContext = context;
        PlaylistPersister.read(context, this);
    }

    private CurrentPlaylist(@NonNull final CurrentPlaylist currentPlaylist) {
        mContext = currentPlaylist.mContext;
        playlist = new ArrayList<>(currentPlaylist.playlist);
        index = currentPlaylist.index;
        media = currentPlaylist.media;
        position = currentPlaylist.position;
    }

    void swap(final int i, final int j) {
        final List<Media> playlist;
        synchronized (mPlaylistLock) {
            playlist = this.playlist;
            if (playlist != null && i < playlist.size() && j < playlist.size()) {
                Collections.swap(playlist, i, j);
                index = playlist.indexOf(media);
            }
        }
        if (playlist != null) {
            notifyPlaylistOrderingChanged(playlist);
        }
    }

    void remove(@NonNull final Media media) {
        boolean removed = false;
        synchronized (mPlaylistLock) {
            if (playlist != null) {
                if (playlist.remove(media)) {
                    if (media.equals(this.media)) {
                        this.media = null;
                    }
                    removed = true;
                }
            }
        }
        if (removed) {
            notifyMediaRemoved(media);
        }
    }

    @Nullable
    public List<Media> getPlaylist() {
        synchronized (mPlaylistLock) {
            return playlist == null ? null : new ArrayList<>(playlist);
        }
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public Media getMedia() {
        return media;
    }

    public long getPosition() {
        return position;
    }

    public void setPlaylist(@Nullable final List<Media> playlist) {
        synchronized (mPlaylistLock) {
            this.playlist = playlist;
        }
        notifyPlaylistChanged(playlist);
        Observable.create(s -> storeToRecentAlbums(getPlaylist()))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    /**
     * Finds albums in playlist and stores them in recently played albums.
     * Album is a sequence of tracks with the same album id.
     * Single playlist can be a concatination of multiple albums.
     *
     * @param playlist The playlist to process
     */
    @WorkerThread
    private void storeToRecentAlbums(@Nullable final List<Media> playlist) {
        if (playlist != null && !playlist.isEmpty()) {
            final Set<Long> albums = new LinkedHashSet<>();
            final long THRESHOLD = 4; // number of items per single album
            long prevAlbumId = playlist.get(0).albumId;
            int sequence = 1;
            for (int i = 1; i < playlist.size(); i++) {
                final long albumId = playlist.get(i).albumId;
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
            if (sequence >= THRESHOLD) {
                albums.add(prevAlbumId);
            }
            RecentPlaylistsManager.getInstance(mContext).storeAlbumsSync(albums);
        }
    }

    public void setIndex(final int index) {
        this.index = index;
    }

    public void persistAsync() {
        PlaylistPersister.persistAsync(mContext, new CurrentPlaylist(this));
    }

    public void setMedia(final Media media) {
        if (this.media == null || !this.media.equals(media)) {
            this.media = media;
            notifyMediaChanged(media);
        }
    }

    public void setPosition(final long position) {
        this.position = position;
        notifyPositionChanged(position);
    }

    public void addObserver(@NonNull final PlaylistObserver observer) {
        synchronized (mObserversLock) {
            mObservers.add(observer);
        }
    }

    public void deleteObserver(@NonNull final PlaylistObserver observer) {
        synchronized (mObserversLock) {
            mObservers.remove(observer);
        }
    }

    private void notifyMediaChanged(final Media media) {
        synchronized (mObserversLock) {
            for (final PlaylistObserver observer : mObservers) {
                observer.onMediaChanged(media);
            }
        }
    }

    private void notifyPositionChanged(final long position) {
        synchronized (mObserversLock) {
            for (final PlaylistObserver observer : mObservers) {
                observer.onPositionChanged(position);
            }
        }
    }

    private void notifyMediaRemoved(@NonNull final Media media) {
        synchronized (mObserversLock) {
            for (final PlaylistObserver observer : mObservers) {
                observer.onMediaRemoved(media);
            }
        }
    }

    private void notifyPlaylistChanged(@Nullable final List<Media> playlist) {
        synchronized (mObserversLock) {
            synchronized (mPlaylistLock) {
                for (final PlaylistObserver observer : mObservers) {
                    observer.onPlaylistChanged(playlist);
                }
            }
        }
    }

    private void notifyPlaylistOrderingChanged(@NonNull final List<Media> playlist) {
        synchronized (mObserversLock) {
            synchronized (mPlaylistLock) {
                for (final PlaylistObserver observer : mObservers) {
                    observer.onPlaylistOrderingChanged(playlist);
                }
            }
        }
    }

    public interface PlaylistObserver {

        void onPlaylistChanged(@Nullable List<Media> playlist);

        void onPlaylistOrderingChanged(@NonNull List<Media> playlist);

        void onPositionChanged(long position);

        void onMediaChanged(Media media);

        void onMediaRemoved(Media media);
    }
}
