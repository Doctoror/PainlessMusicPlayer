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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.doctoror.fuckoffmusicplayer.playback.PlaybackParams;

import android.databinding.BaseObservable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.support.annotation.NonNull;

/**
 * {@link NowPlayingActivity} model
 */
public final class NowPlayingActivityModel extends BaseObservable {

    private String mTitle;
    private String mArtistAndAlbum;
    private long mDuration;

    private final ObservableLong mElapsedTime = new ObservableLong();
    private final ObservableInt mProgress = new ObservableInt();

    private final ObservableInt mBtnPlayRes = new ObservableInt();
    private final ObservableBoolean mShuffleEnabled = new ObservableBoolean();

    private final ObservableInt mRepeatMode = new ObservableInt();

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(final String title) {
        mTitle = title;
    }

    public String getArtistAndAlbum() {
        return mArtistAndAlbum;
    }

    public void setArtistAndAlbum(final String artistAndAlbum) {
        mArtistAndAlbum = artistAndAlbum;
    }

    public ObservableInt getProgress() {
        return mProgress;
    }

    public void setProgress(final int progress) {
        mProgress.set(progress);
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(final long duration) {
        mDuration = duration;
    }

    @NonNull
    public ObservableLong getElapsedTime() {
        return mElapsedTime;
    }

    public void setElapsedTime(final long elapsedTime) {
        mElapsedTime.set(elapsedTime);
    }

    @NonNull
    public ObservableInt getBtnPlayRes() {
        return mBtnPlayRes;
    }

    public void setBtnPlayRes(final int btnPlayRes) {
        mBtnPlayRes.set(btnPlayRes);
    }

    @NonNull
    public ObservableBoolean isShuffleEnabled() {
        return mShuffleEnabled;
    }

    public void setShuffleEnabled(final boolean enabled) {
        mShuffleEnabled.set(enabled);
    }

    @NonNull
    public ObservableInt getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(@PlaybackParams.RepeatMode final int repeatMode) {
        mRepeatMode.set(repeatMode);
    }
}
