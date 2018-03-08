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
package com.doctoror.fuckoffmusicplayer.domain.playback;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a playback state
 */
@IntDef({
        PlaybackState.STATE_IDLE,
        PlaybackState.STATE_LOADING,
        PlaybackState.STATE_PLAYING,
        PlaybackState.STATE_PAUSED,
        PlaybackState.STATE_ERROR
})
@Retention(RetentionPolicy.SOURCE)
public @interface PlaybackState {

    int STATE_IDLE = 0;
    int STATE_LOADING = 1;
    int STATE_PLAYING = 2;
    int STATE_PAUSED = 3;
    int STATE_ERROR = 4;
}
