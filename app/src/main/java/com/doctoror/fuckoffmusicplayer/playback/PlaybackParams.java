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

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Playback config
 */
public final class PlaybackParams {

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

    private final PlaybackParamsPrefs mPrefs;

    private boolean mShuffleEnabled;

    @RepeatMode private int mRepeatMode;

    private PlaybackParams(@NonNull final Context context) {
        mPrefs = PlaybackParamsPrefs.with(context);
        mShuffleEnabled = mPrefs.isShuffleEnabled();
        //noinspection WrongConstant
        mRepeatMode = mPrefs.getRepeatMode();
    }

    public void setShuffleEnabled(final boolean shuffleEnabled) {
        if (mShuffleEnabled != shuffleEnabled) {
            mShuffleEnabled = shuffleEnabled;
            mPrefs.setShuffleEnabled(mShuffleEnabled);
        }
    }

    public boolean isShuffleEnabled() {
        return mShuffleEnabled;
    }

    public void setRepeatMode(@RepeatMode final int repeatMode) {
        if (mRepeatMode != repeatMode) {
            mRepeatMode = repeatMode;
            mPrefs.setRepeatMode(repeatMode);
        }
    }

    @RepeatMode
    public int getRepeatMode() {
        return mRepeatMode;
    }
}
