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
package com.doctoror.fuckoffmusicplayer.presentation.nowplaying;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.databinding.ObservableLong;

import com.doctoror.fuckoffmusicplayer.domain.playback.RepeatMode;

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

    private final ObservableField<RepeatMode> mRepeatMode = new ObservableField<>();

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
    public ObservableField<RepeatMode> getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(@NonNull final RepeatMode repeatMode) {
        mRepeatMode.set(repeatMode);
    }
}
