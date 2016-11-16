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
package com.doctoror.fuckoffmusicplayer.ui;

import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

public final class NowPlayingActivityModelPlaybackState {

    private final ObservableLong mDuration = new ObservableLong();
    private final ObservableLong mElapsedTime = new ObservableLong();

    private final ObservableInt mProgress = new ObservableInt();

    public ObservableInt getProgress() {
        return mProgress;
    }

    public void setProgress(final int progress) {
        mProgress.set(progress);
    }

    public ObservableLong getDuration() {
        return mDuration;
    }

    public void setDuration(final long duration) {
        mDuration.set(duration);
    }

    @NonNull
    public ObservableLong getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(final long elapsedTime) {
        mElapsedTime.set(elapsedTime);
    }


}
