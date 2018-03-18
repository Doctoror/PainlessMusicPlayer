/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * {@link AppBarLayout.Behavior} which can be disabled for scrolling
 */
public class DisableableAppBarLayoutBehavior extends AppBarLayout.Behavior {

    private boolean mEnabled = true;

    public DisableableAppBarLayoutBehavior() {
    }

    public DisableableAppBarLayoutBehavior(
            @NonNull final Context context,
            @Nullable final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnabled(final boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean onStartNestedScroll(
            @NonNull final CoordinatorLayout parent,
            @NonNull final AppBarLayout child,
            @NonNull final View directTargetChild,
            @NonNull final View target,
            final int nestedScrollAxes,
            final int type) {
        return mEnabled && super.onStartNestedScroll(
                parent, child, directTargetChild, target, nestedScrollAxes, type);
    }
}
