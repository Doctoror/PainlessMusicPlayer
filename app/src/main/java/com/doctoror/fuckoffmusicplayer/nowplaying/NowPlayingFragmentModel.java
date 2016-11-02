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

import android.databinding.BaseObservable;
import android.databinding.ObservableInt;
import android.databinding.ObservableLong;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 21.10.16.
 */

public final class NowPlayingFragmentModel extends BaseObservable {

    private String mArt;
    private String mTitle;
    private String mArtist;
    private String mAlbum;
    private Drawable mStateIcon;
    private long mDuration;

    private final ObservableLong mElapsedTime = new ObservableLong();
    private final ObservableInt mProgress = new ObservableInt();

    private final ObservableInt mBtnPlayRes = new ObservableInt();

    public String getArt() {
        return mArt;
    }

    public void setArt(final String art) {
        mArt = art;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(final String title) {
        mTitle = title;
    }

    public String getArtist() {
        return mArtist;
    }

    public void setArtist(final String artist) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(final String album) {
        mAlbum = album;
    }

    public Drawable getStateIcon() {
        return mStateIcon;
    }

    public void setStateIcon(final Drawable stateIcon) {
        mStateIcon = stateIcon;
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
}
