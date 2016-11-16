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

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 16.11.16.
 */

public final class NowPlayingActivityModelViewState {

    private final ObservableInt mBtnPlayRes = new ObservableInt();

    private final ObservableInt mAnimatorChild = new ObservableInt();
    private final ObservableField<CharSequence> mMessage = new ObservableField<>();

    private final ObservableBoolean mProgressVisible = new ObservableBoolean();
    private final ObservableBoolean mFixButtonVisible = new ObservableBoolean();
    private final ObservableBoolean mNavigationButtonsVisible = new ObservableBoolean();

    public ObservableBoolean navigationButtonsVisible() {
        return mNavigationButtonsVisible;
    }

    public void setNavigationButtonsVisible(final boolean visible) {
        mNavigationButtonsVisible.set(visible);
    }

    public ObservableBoolean isFixButtonVisible() {
        return mFixButtonVisible;
    }

    public void setFixButtonVisible(final boolean visible) {
        mFixButtonVisible.set(visible);
    }

    public ObservableBoolean isProgressVisible() {
        return mProgressVisible;
    }

    public void setProgressVisible(final boolean visible) {
        mProgressVisible.set(visible);
    }

    public ObservableInt getAnimatorChild() {
        return mAnimatorChild;
    }

    public void setAnimatorChild(final int child) {
        mAnimatorChild.set(child);
    }

    public ObservableField<CharSequence> getMessage() {
        return mMessage;
    }

    public void setMessage(@Nullable final CharSequence message) {
        mMessage.set(message);
    }

    @NonNull
    public ObservableInt getBtnPlayRes() {
        return mBtnPlayRes;
    }

    public void setBtnPlayRes(final int btnPlayRes) {
        mBtnPlayRes.set(btnPlayRes);
    }
}
