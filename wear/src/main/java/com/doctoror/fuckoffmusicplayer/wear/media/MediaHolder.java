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
package com.doctoror.fuckoffmusicplayer.wear.media;

import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventAlbumArt;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventMedia;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventPlaybackPosition;
import com.doctoror.fuckoffmusicplayer.wear.media.eventbus.EventPlaybackState;

import org.greenrobot.eventbus.EventBus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

/**
 * Holds current album art, {@link WearPlaybackData.Media} and {@link
 * WearPlaybackData.PlaybackState}
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

    private final EventBus mEventBus = EventBus.getDefault();

    @NonNull
    private final Context mContext;

    private Bitmap mAlbumArt;
    private WearPlaybackData.Media mMedia;
    private WearPlaybackData.PlaybackState mPlaybackState;
    private WearPlaybackData.PlaybackPosition mPlaybackPosition;

    private MediaHolder(@NonNull final Context context) {
        mContext = context;
        mMedia = MediaPersister.readMedia(context);
        mAlbumArt = MediaPersister.readAlbumArt(context);
        mPlaybackState = MediaPersister.readPlaybackState(context);
        mPlaybackPosition = MediaPersister.readPlaybackPosition(context);
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
    public synchronized void setMedia(@Nullable final WearPlaybackData.Media media) {
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
            @Nullable final WearPlaybackData.PlaybackState state) {
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

    @WorkerThread
    public synchronized void setPlaybackPosition(
            @Nullable final WearPlaybackData.PlaybackPosition position) {
        if (mPlaybackPosition != position) {
            mPlaybackPosition = position;
            if (position == null) {
                MediaPersister.deletePlaybackPosition(mContext);
            } else {
                MediaPersister.persistPlaybackPosition(mContext, position);
            }
            notifyPlaybackPositionChanged(position);
        }
    }

    @Nullable
    public WearPlaybackData.Media getMedia() {
        return mMedia;
    }

    @Nullable
    public Bitmap getAlbumArt() {
        return mAlbumArt;
    }

    @Nullable
    public WearPlaybackData.PlaybackState getPlaybackState() {
        return mPlaybackState;
    }

    private void notifyAlbumArtChanged(@Nullable final Bitmap albumArt) {
        mEventBus.post(new EventAlbumArt(albumArt));
    }

    private void notifyMediaChanged(@Nullable final WearPlaybackData.Media media) {
        mEventBus.post(new EventMedia(media));
    }

    private void notifyPlaybackStateChanged(@Nullable final WearPlaybackData.PlaybackState ps) {
        mEventBus.post(new EventPlaybackState(ps));
    }

    private void notifyPlaybackPositionChanged(@Nullable final WearPlaybackData.PlaybackPosition p) {
        mEventBus.post(new EventPlaybackPosition(p));
    }
}
