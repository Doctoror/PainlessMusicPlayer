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
package com.doctoror.fuckoffmusicplayer.presentation.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

/**
 * {@link CoordinatorLayout} that can disable touch events
 */
public class DisableableCoordinatorLayout extends CoordinatorLayout {

    private boolean mTouchEnabled = true;

    public DisableableCoordinatorLayout(final Context context) {
        super(context);
    }

    public DisableableCoordinatorLayout(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    public DisableableCoordinatorLayout(final Context context,
            final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTouchEnabled(final boolean touchEnabled) {
        mTouchEnabled = touchEnabled;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        return !mTouchEnabled || super.onTouchEvent(ev);
    }
}
