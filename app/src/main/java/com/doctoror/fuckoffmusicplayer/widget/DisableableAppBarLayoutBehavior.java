package com.doctoror.fuckoffmusicplayer.widget;

import android.content.Context;
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

    public DisableableAppBarLayoutBehavior(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
    }

    public void setEnabled(final boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout parent, final AppBarLayout child,
            final View directTargetChild, final View target, final int nestedScrollAxes) {
        return mEnabled && super
                .onStartNestedScroll(parent, child, directTargetChild, target, nestedScrollAxes);
    }
}
