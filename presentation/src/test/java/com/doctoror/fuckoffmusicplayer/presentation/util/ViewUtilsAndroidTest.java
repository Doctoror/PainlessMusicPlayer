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

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link ViewUtils} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class ViewUtilsAndroidTest {

    private Pair<Integer, ViewGroup> prepareViewGroupWithValidChildHeights() {
        final Context context = RuntimeEnvironment.application;
        final int v1h = 101;
        final int v2h = 102;
        final int v3h = 103;
        final int v4h = 0;

        final ViewGroup vg = new FrameLayout(context);
        vg.setBottom(666);

        final View v1 = new View(context);
        v1.setBottom(v1h);
        vg.addView(v1);

        final View v2 = new View(context);
        v2.setBottom(v2h);
        vg.addView(v2);

        final View v3 = new View(context);
        v3.setBottom(v3h);
        vg.addView(v3);

        final View v4 = new View(context);
        v4.setBottom(v4h);
        vg.addView(v4);

        return new Pair<>(v1h + v2h + v3h + v4h, vg);
    }

    @Test(expected = NullPointerException.class)
    public void testChildHeightsNull() {
        //noinspection ConstantConditions
        ViewUtils.childHeights(null);
    }

    @Test
    public void testChildHeightsEmptyViewGroup() {
        final ViewGroup vg = new FrameLayout(RuntimeEnvironment.application);
        vg.setBottom(666);

        assertEquals(0, ViewUtils.childHeights(vg));
    }

    @Test
    public void testChildHeights() {
        final Pair<Integer, ViewGroup> result = prepareViewGroupWithValidChildHeights();
        assertEquals(result.first.intValue(), ViewUtils.childHeights(result.second));
    }

    @Test(expected = NullPointerException.class)
    public void testGetOverlayTopNull() {
        //noinspection ConstantConditions
        ViewUtils.getOverlayTop(null);
    }

    @Test
    public void testGetOverlayTopOrdinaryView() {
        final View view = new View(RuntimeEnvironment.application);
        assertEquals(0, ViewUtils.getOverlayTop(view));
    }

    @Test
    public void testGetOverlayTopNoBehavior() {
        final View view = new View(RuntimeEnvironment.application);
        view.setLayoutParams(new CoordinatorLayout.LayoutParams(0, 0));
        assertEquals(0, ViewUtils.getOverlayTop(view));
    }

    @Test
    public void testGetOverlayTopDifferentBehavior() {
        final View view = new View(RuntimeEnvironment.application);

        final CoordinatorLayout.LayoutParams p = new CoordinatorLayout.LayoutParams(0, 0);
        p.setBehavior(new FloatingActionButton.Behavior());

        view.setLayoutParams(p);
        assertEquals(0, ViewUtils.getOverlayTop(view));
    }

    @Test
    public void testGetOverlayTop() {
        final View view = new View(RuntimeEnvironment.application);

        final CoordinatorLayout.LayoutParams p = new CoordinatorLayout.LayoutParams(0, 0);
        final AppBarLayout.ScrollingViewBehavior b = new AppBarLayout.ScrollingViewBehavior();
        b.setOverlayTop(128);
        p.setBehavior(b);

        view.setLayoutParams(p);
        assertEquals(128, ViewUtils.getOverlayTop(view));
    }

    @Test
    public void testIsScrollableViewLargeEnoughToScrollWhenFalse() {
        final Context context = RuntimeEnvironment.application;
        final Pair<Integer, ViewGroup> heightsResult = prepareViewGroupWithValidChildHeights();

        final ViewGroup rootView = new FrameLayout(context);
        rootView.setBottom(heightsResult.first);

        final ViewGroup appBarLayout = new FrameLayout(context);
        final ViewGroup scrollableView = heightsResult.second;

        rootView.addView(appBarLayout);
        rootView.addView(scrollableView);

        assertFalse(ViewUtils.isScrollableViewLargeEnoughToScroll(rootView,
                appBarLayout,
                scrollableView,
                0));
    }

    @Test
    public void testIsScrollableViewLargeEnoughToScrollWhenTrue() {
        final Context context = RuntimeEnvironment.application;
        final Pair<Integer, ViewGroup> heightsResult = prepareViewGroupWithValidChildHeights();

        final ViewGroup rootView = new FrameLayout(context);
        rootView.setBottom(heightsResult.first / 2);

        final ViewGroup appBarLayout = new FrameLayout(context);
        appBarLayout.setBottom(heightsResult.first / 2);

        final ViewGroup scrollableView = heightsResult.second;

        rootView.addView(appBarLayout);
        rootView.addView(scrollableView);

        assertTrue(ViewUtils.isScrollableViewLargeEnoughToScroll(rootView,
                appBarLayout,
                scrollableView,
                0));
    }
}
