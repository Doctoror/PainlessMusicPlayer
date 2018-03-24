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
package com.doctoror.fuckoffmusicplayer.presentation.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.doctoror.fuckoffmusicplayer.presentation.widget.DisableableAppBarLayout;

/**
 * View utils
 */
public final class ViewUtils {

    private ViewUtils() {
        throw new UnsupportedOperationException();
    }

    public static int childHeights(@NonNull final ViewGroup viewGroup) {
        int height = 0;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            height += viewGroup.getChildAt(i).getHeight();
        }
        return height;
    }

    public static int getOverlayTop(@NonNull final View view) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params instanceof CoordinatorLayout.LayoutParams) {
            final CoordinatorLayout.Behavior<?> behavior = ((CoordinatorLayout.LayoutParams) params)
                    .getBehavior();
            if (behavior instanceof AppBarLayout.ScrollingViewBehavior) {
                return ((AppBarLayout.ScrollingViewBehavior) behavior).getOverlayTop();
            }
        }
        return 0;
    }

    public static void setAppBarCollapsibleIfScrollableViewIsLargeEnoughToScroll(
            @NonNull final View rootView,
            @NonNull final DisableableAppBarLayout appBar,
            @NonNull final ViewGroup scrollableView,
            final int overlayTop) {
        appBar.setCollapsible(isScrollableViewLargeEnoughToScroll(
                rootView,
                appBar,
                scrollableView,
                overlayTop));
    }

    static boolean isScrollableViewLargeEnoughToScroll(
            @NonNull final View rootView,
            @NonNull final ViewGroup appBar,
            @NonNull final ViewGroup scrollableView,
            final int overlayTop) {
        final int rootViewHeight = rootView.getHeight();
        final int recyclerViewHeight = ViewUtils.childHeights(scrollableView);
        final int appBarHeight = appBar.getHeight();
        return recyclerViewHeight - overlayTop > rootViewHeight - appBarHeight;
    }

    @Nullable
    public static View getItemView(@Nullable final RecyclerView recyclerView, final int position) {
        if (recyclerView != null) {
            final RecyclerView.ViewHolder vh = recyclerView
                    .findViewHolderForAdapterPosition(position);
            if (vh != null) {
                return vh.itemView;
            }
        }
        return null;
    }
}
