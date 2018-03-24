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
package com.doctoror.fuckoffmusicplayer.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import io.reactivex.disposables.Disposable;

/**
 * Will set insets from @{@link InsetsHolder} as padding.
 */
public class PaddingInsetsFrameLayout extends FrameLayout {

    private Disposable mDisposable;

    public PaddingInsetsFrameLayout(@NonNull final Context context) {
        this(context, null);
    }

    public PaddingInsetsFrameLayout(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaddingInsetsFrameLayout(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        dispose();
        subscribe();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        dispose();
    }

    private void dispose() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    private void subscribe() {
        mDisposable = InsetsHolder.getInstance().observable().subscribe(
                (insets) -> setPadding(insets.left, insets.top, insets.right, insets.bottom));
    }
}
