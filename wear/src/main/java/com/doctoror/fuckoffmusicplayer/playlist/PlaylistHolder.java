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

import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.eventbus.EventPlaylist;

import org.greenrobot.eventbus.EventBus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

/**
 * Holds current playlist
 */
public final class PlaylistHolder {

    // Is not a leak since it's an application context
    @SuppressLint("StaticFieldLeak")
    private static volatile PlaylistHolder sInstance;

    @NonNull
    public static PlaylistHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (PlaylistHolder.class) {
                if (sInstance == null) {
                    sInstance = new PlaylistHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final Context mContext;

    private WearPlaybackData.Playlist mPlaylist;

    private PlaylistHolder(@NonNull final Context context) {
        mContext = context;
        mPlaylist = PlaylistPersister.read(context);
    }

    @WorkerThread
    public synchronized void setPlaylist(@Nullable final WearPlaybackData.Playlist playlist) {
        if (mPlaylist != playlist) {
            mPlaylist = playlist;
            notifyChanged(playlist);
            PlaylistPersister.persist(mContext, playlist);
        }
    }

    @Nullable
    public WearPlaybackData.Playlist getPlaylist() {
        return mPlaylist;
    }

    private void notifyChanged(@Nullable final WearPlaybackData.Playlist playlist) {
        EventBus.getDefault().post(new EventPlaylist(playlist));
    }
}
