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
package com.doctoror.fuckoffmusicplayer.wear.nowplaying;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.NonNull;

/**
 * View state data binding model for {@link NowPlayingFragment} view
 */
public final class NowPlayingFragmentModelViewState {

    private final ObservableInt mBtnPlayRes = new ObservableInt();
    private final ObservableField<CharSequence> mBtnPlayContentDescription
            = new ObservableField<>();

    private final ObservableBoolean mProgressVisible = new ObservableBoolean();
    private final ObservableBoolean mNavigationButtonsVisible = new ObservableBoolean();

    public ObservableBoolean navigationButtonsVisible() {
        return mNavigationButtonsVisible;
    }

    void setNavigationButtonsVisible(final boolean visible) {
        mNavigationButtonsVisible.set(visible);
    }

    public ObservableBoolean isProgressVisible() {
        return mProgressVisible;
    }

    public void setProgressVisible(final boolean visible) {
        mProgressVisible.set(visible);
    }

    @NonNull
    public ObservableInt getBtnPlayRes() {
        return mBtnPlayRes;
    }

    void setBtnPlayRes(final int btnPlayRes) {
        mBtnPlayRes.set(btnPlayRes);
    }

    @NonNull
    public ObservableField<CharSequence> getBtnPlayContentDescription() {
        return mBtnPlayContentDescription;
    }

    void setBtnPlayContentDescription(final CharSequence btnPlayContentDescription) {
        mBtnPlayContentDescription.set(btnPlayContentDescription);
    }
}
