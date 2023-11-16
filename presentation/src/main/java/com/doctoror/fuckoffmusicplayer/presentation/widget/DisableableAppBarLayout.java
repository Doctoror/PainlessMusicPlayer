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

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;

/**
 * {@link AppBarLayout} that can disable collapsing.
 */
public class DisableableAppBarLayout extends AppBarLayout {

    private final DisableableAppBarLayoutBehavior mBehavior = new DisableableAppBarLayoutBehavior();

    private DisableableCoordinatorLayout mParent;

    private boolean mCollapsible = true;

    public DisableableAppBarLayout(final Context context) {
        super(context);
    }

    public DisableableAppBarLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setLayoutParams(final ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        if (params instanceof CoordinatorLayout.LayoutParams) {
            ((CoordinatorLayout.LayoutParams) params).setBehavior(mBehavior);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ViewParent parent = getParent();
        if (parent instanceof DisableableCoordinatorLayout) {
            mParent = (DisableableCoordinatorLayout) parent;
            mParent.setTouchEnabled(mCollapsible);
        } else {
            throw new RuntimeException(
                    "DisableableAppBarLayout must be a child of DisableableCoordinatorLayout");
        }
    }

    public boolean isCollapsible() {
        return mCollapsible;
    }

    public void setCollapsible(final boolean collapsible) {
        mCollapsible = collapsible;
        mBehavior.setEnabled(collapsible);

        if (mParent != null) {
            mParent.setTouchEnabled(collapsible);
        }
    }
}
