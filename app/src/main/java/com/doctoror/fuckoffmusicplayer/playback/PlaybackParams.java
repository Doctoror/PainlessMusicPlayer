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
import com.doctoror.fuckoffmusicplayer.playback.nano.PlaybackParamsProto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import rx.Observable;
import rx.schedulers.Schedulers;

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
    public static final int REPEAT_MODE_PLAYLIST = 1;
    public static final int REPEAT_MODE_TRACK = 2;

    @IntDef({
            REPEAT_MODE_NONE,
            REPEAT_MODE_PLAYLIST,
            REPEAT_MODE_TRACK
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {

    }

    @NonNull
    private final Context mContext;

    private boolean mShuffleEnabled;

    @RepeatMode
    private int mRepeatMode = REPEAT_MODE_PLAYLIST;

    private PlaybackParams(@NonNull final Context context) {
        mContext = context;

        final PlaybackParamsProto.PlaybackParams persistent = read();
        if (persistent != null) {
            mShuffleEnabled = persistent.shuffle;
            mRepeatMode = persistent.repeatMode;
        }
    }

    public void setShuffleEnabled(final boolean shuffleEnabled) {
        if (mShuffleEnabled != shuffleEnabled) {
            mShuffleEnabled = shuffleEnabled;
            Observable.create(s -> persist()).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    public boolean isShuffleEnabled() {
        return mShuffleEnabled;
    }

    public void setRepeatMode(@RepeatMode final int repeatMode) {
        if (mRepeatMode != repeatMode) {
            mRepeatMode = repeatMode;
            Observable.create(s -> persist()).subscribeOn(Schedulers.io()).subscribe();
        }
    }

    @RepeatMode
    public int getRepeatMode() {
        return mRepeatMode;
    }

    @Nullable
    private PlaybackParamsProto.PlaybackParams read() {
        return ProtoUtils.readFromFile(mContext, FILE_NAME,
                new PlaybackParamsProto.PlaybackParams());
    }

    private void persist() {
        final PlaybackParamsProto.PlaybackParams p = new PlaybackParamsProto.PlaybackParams();
        p.shuffle = mShuffleEnabled;
        p.repeatMode = mRepeatMode;
        ProtoUtils.writeToFile(mContext, FILE_NAME, p);
    }
}
