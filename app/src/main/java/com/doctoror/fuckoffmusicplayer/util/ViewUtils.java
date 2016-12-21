package com.doctoror.fuckoffmusicplayer.util;

import com.doctoror.fuckoffmusicplayer.widget.DisableableAppBarLayout;

import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewGroup;

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
        final int rootViewHeight = rootView.getHeight();
        final int recyclerViewHeight = ViewUtils.childHeights(scrollableView);
        final int appBarHeight = appBar.getHeight();
        appBar.setCollapsible(recyclerViewHeight - overlayTop > rootViewHeight - appBarHeight);
    }
}
