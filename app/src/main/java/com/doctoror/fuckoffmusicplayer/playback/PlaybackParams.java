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
package com.doctoror.fuckoffmusicplayer.playback;

import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.Handlers;
import com.doctoror.fuckoffmusicplayer.playback.nano.PlaybackParamsProto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Playback config
 */
public final class PlaybackParams {

    // Application context is not considered as leak
    @SuppressLint("StaticFieldLeak")
    private static volatile PlaybackParams sInstance;

    @NonNull
    public static PlaybackParams getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (PlaybackParams.class) {
                if (sInstance == null) {
                    sInstance = new PlaybackParams(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private static final String FILE_NAME = "playback_params";

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_QUEUE = 1;
    public static final int REPEAT_MODE_TRACK = 2;

    @IntDef({
            REPEAT_MODE_NONE,
            REPEAT_MODE_QUEUE,
            REPEAT_MODE_TRACK
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {

    }

    @NonNull
    private final Object mLock = new Object();

    @NonNull
    private final Object mLockIO = new Object();

    @NonNull
    private final Context mContext;

    private boolean mShuffleEnabled;

    @RepeatMode
    private int mRepeatMode = REPEAT_MODE_QUEUE;

    private PlaybackParams(@NonNull final Context context) {
        mContext = context;

        final PlaybackParamsProto.PlaybackParams persistent = read();
        if (persistent != null) {
            synchronized (mLock) {
                mShuffleEnabled = persistent.shuffle;
                mRepeatMode = persistent.repeatMode;
            }
        }
    }

    public void setShuffleEnabled(final boolean shuffleEnabled) {
        synchronized (mLock) {
            if (mShuffleEnabled != shuffleEnabled) {
                mShuffleEnabled = shuffleEnabled;
                persistAsync();
            }
        }
    }

    public boolean isShuffleEnabled() {
        synchronized (mLock) {
            return mShuffleEnabled;
        }
    }

    public void setRepeatMode(@RepeatMode final int repeatMode) {
        synchronized (mLock) {
            if (mRepeatMode != repeatMode) {
                mRepeatMode = repeatMode;
                persistAsync();
            }
        }
    }

    @RepeatMode
    public int getRepeatMode() {
        synchronized (mLock) {
            return mRepeatMode;
        }
    }

    @Nullable
    private PlaybackParamsProto.PlaybackParams read() {
        return ProtoUtils.readFromFile(mContext, FILE_NAME,
                new PlaybackParamsProto.PlaybackParams());
    }

    private void persistAsync() {
        Handlers.runOnIoThread(this::persistBlocking);
    }

    @WorkerThread
    private void persistBlocking() {
        final PlaybackParamsProto.PlaybackParams p = new PlaybackParamsProto.PlaybackParams();
        synchronized (mLock) {
            p.shuffle = mShuffleEnabled;
            p.repeatMode = mRepeatMode;
        }
        synchronized (mLockIO) {
            ProtoUtils.writeToFile(mContext, FILE_NAME, p);
        }
    }
}
