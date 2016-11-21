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
package com.doctoror.fuckoffmusicplayer.root;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class RootActivityModel {

    private final ObservableField<CharSequence> mMessage = new ObservableField<>();
    private final ObservableField<Drawable> mMessageDrawableTop = new ObservableField<>();

    private final ObservableBoolean mFixButtonVisible = new ObservableBoolean();
    private final ObservableBoolean mHandheldConnected = new ObservableBoolean();

    public ObservableBoolean isFixButtonVisible() {
        return mFixButtonVisible;
    }

    public void setFixButtonVisible(final boolean visible) {
        mFixButtonVisible.set(visible);
    }

    public ObservableField<CharSequence> getMessage() {
        return mMessage;
    }

    public void setMessage(@Nullable final CharSequence message) {
        mMessage.set(message);
    }

    public ObservableBoolean isHandheldConnected() {
        return mHandheldConnected;
    }

    public void setHandheldConnected(final boolean connected) {
        mHandheldConnected.set(connected);
    }

    public ObservableField<Drawable> getMessageDrawableTop() {
        return mMessageDrawableTop;
    }

    void setMessageDrawableTop(@Nullable final Drawable drawable) {
        mMessageDrawableTop.set(drawable);
    }
}
