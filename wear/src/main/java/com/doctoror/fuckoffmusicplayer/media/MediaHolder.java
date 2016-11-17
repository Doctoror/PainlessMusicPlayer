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
package com.doctoror.fuckoffmusicplayer.media;

import com.doctoror.commons.wear.nano.ProtoPlaybackData;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 21.10.16.
 */

public final class MediaHolder {

    // Is not a leak since it's an application context
    @SuppressLint("StaticFieldLeak")
    private static volatile MediaHolder sInstance;

    @NonNull
    public static MediaHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (MediaHolder.class) {
                if (sInstance == null) {
                    sInstance = new MediaHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private final List<PlaybackInfoObserver> mObservers = new LinkedList<>();

    @NonNull
    private final Context mContext;

    private Bitmap mAlbumArt;
    private ProtoPlaybackData.Media mMedia;
    private ProtoPlaybackData.PlaybackState mPlaybackState;

    private MediaHolder(@NonNull final Context context) {
        mContext = context;
        mMedia = MediaPersister.readMedia(context);
        mAlbumArt = MediaPersister.readAlbumArt(context);
        mPlaybackState = MediaPersister.readPlaybackState(context);
    }

    @WorkerThread
    public synchronized void setAlbumArt(@Nullable final byte[] albumArt) {
        // Empty array means there is no art for current media
        mAlbumArt = albumArt == null || albumArt.length == 0
                ? null : BitmapFactory.decodeByteArray(albumArt, 0, albumArt.length);
        notifyAlbumArtChanged(mAlbumArt);

        // Empty array means the art must be erased
        MediaPersister.persistAlbumArt(mContext,
                albumArt == null || albumArt.length == 0 ? null : albumArt);
    }

    @WorkerThread
    public synchronized void setMedia(@Nullable final ProtoPlaybackData.Media media) {
        if (mMedia != media) {
            mMedia = media;
            if (media == null) {
                MediaPersister.deleteMedia(mContext);
            } else {
                MediaPersister.persistMedia(mContext, media);
            }
            notifyMediaChanged(media);
        }
    }

    @WorkerThread
    public synchronized void setPlaybackState(
            @Nullable final ProtoPlaybackData.PlaybackState state) {
        if (mPlaybackState != state) {
            mPlaybackState = state;
            if (state == null) {
                MediaPersister.deletePlaybackState(mContext);
            } else {
                MediaPersister.persistPlaybackState(mContext, state);
            }
            notifyPlaybackStateChanged(state);
        }
    }

    @Nullable
    public ProtoPlaybackData.Media getMedia() {
        return mMedia;
    }

    @Nullable
    public Bitmap getAlbumArt() {
        return mAlbumArt;
    }

    @Nullable
    public ProtoPlaybackData.PlaybackState getPlaybackState() {
        return mPlaybackState;
    }

    public void addObserver(@NonNull final PlaybackInfoObserver observer) {
        mObservers.add(observer);
    }

    public void deleteObserver(@NonNull final PlaybackInfoObserver observer) {
        mObservers.remove(observer);
    }

    private void notifyAlbumArtChanged(@Nullable final Bitmap albumArt) {
        for (final PlaybackInfoObserver observer : mObservers) {
            observer.onAlbumArtChanged(albumArt);
        }
    }

    private void notifyMediaChanged(@Nullable final ProtoPlaybackData.Media media) {
        for (final PlaybackInfoObserver observer : mObservers) {
            observer.onMediaChanged(media);
        }
    }

    private void notifyPlaybackStateChanged(@Nullable final ProtoPlaybackData.PlaybackState ps) {
        for (final PlaybackInfoObserver observer : mObservers) {
            observer.onPlaybackStateChanged(ps);
        }
    }

    public interface PlaybackInfoObserver {

        @WorkerThread
        void onMediaChanged(@Nullable ProtoPlaybackData.Media media);

        @WorkerThread
        void onPlaybackStateChanged(@Nullable ProtoPlaybackData.PlaybackState playbackState);

        @WorkerThread
        void onAlbumArtChanged(@Nullable Bitmap albumArt);
    }
}
