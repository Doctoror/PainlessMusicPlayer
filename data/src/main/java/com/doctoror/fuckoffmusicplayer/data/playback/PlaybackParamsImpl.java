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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.fuckoffmusicplayer.data.playback.nano.PlaybackParamsProto;
import com.doctoror.fuckoffmusicplayer.data.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.RepeatMode;
import com.doctoror.fuckoffmusicplayer.domain.playback.RepeatModeKt;

import io.reactivex.Completable;

public final class PlaybackParamsImpl implements PlaybackParams {

    private static final String FILE_NAME = "playback_params";

    @NonNull
    private final Object mLock = new Object();

    @NonNull
    private final Object mLockIO = new Object();

    @NonNull
    private final Context mContext;

    @NonNull
    private final SchedulersProvider schedulersProvider;

    private boolean mShuffleEnabled;

    @NonNull
    private RepeatMode mRepeatMode = RepeatMode.QUEUE;

    public PlaybackParamsImpl(
            @NonNull final Context context,
            @NonNull final SchedulersProvider schedulersProvider) {
        mContext = context;
        this.schedulersProvider = schedulersProvider;

        final PlaybackParamsProto.PlaybackParams persistent = read();
        if (persistent != null) {
            synchronized (mLock) {
                mShuffleEnabled = persistent.shuffle;
                mRepeatMode = RepeatModeKt.repeatModeFromIndex(persistent.repeatMode);
            }
        }
    }

    @Override
    public void setShuffleEnabled(final boolean shuffleEnabled) {
        synchronized (mLock) {
            if (mShuffleEnabled != shuffleEnabled) {
                mShuffleEnabled = shuffleEnabled;
                persistAsync();
            }
        }
    }

    @Override
    public boolean isShuffleEnabled() {
        synchronized (mLock) {
            return mShuffleEnabled;
        }
    }

    @Override
    public void setRepeatMode(@NonNull final RepeatMode repeatMode) {
        synchronized (mLock) {
            if (mRepeatMode != repeatMode) {
                mRepeatMode = repeatMode;
                persistAsync();
            }
        }
    }

    @NonNull
    @Override
    public RepeatMode getRepeatMode() {
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
        Completable.fromAction(this::persistBlocking)
                .subscribeOn(schedulersProvider.io())
                .subscribe();
    }

    @WorkerThread
    private void persistBlocking() {
        final PlaybackParamsProto.PlaybackParams p = new PlaybackParamsProto.PlaybackParams();
        synchronized (mLock) {
            p.shuffle = mShuffleEnabled;
            p.repeatMode = mRepeatMode.getIndex();
        }
        synchronized (mLockIO) {
            ProtoUtils.writeToFile(mContext, FILE_NAME, p);
        }
    }
}
