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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.doctoror.fuckoffmusicplayer.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Method;

/**
 * {@link DrawableUtils} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class DrawableUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testGetTintedDrawableFromResNullContext() {
        //noinspection ConstantConditions
        DrawableUtils.getTintedDrawable(null, 0, null);
    }

    @Test
    public void testGetTintedDrawableFromResNullTint() throws Exception {
        final Drawable drawable = DrawableUtils.getTintedDrawable(
                RuntimeEnvironment.application,
                R.drawable.appwidget_preview_single_row, null);

        assertHasNoTint(drawable);
    }

    @Test
    public void testGetTintedDrawableFromRes() throws Exception {
        final Context context = RuntimeEnvironment.application;
        final Drawable drawable = DrawableUtils.getTintedDrawable(context,
                R.drawable.appwidget_preview_single_row,
                ContextCompat.getColorStateList(context, R.color.colorAccent));

        assertHasTint(drawable);
    }

    @Test
    public void testGetTintedDrawableNullTint() throws Exception {
        final Drawable source = ContextCompat.getDrawable(
                RuntimeEnvironment.application,
                R.drawable.appwidget_preview_single_row);

        final Drawable drawable = DrawableUtils.getTintedDrawable(source, null);

        assertHasNoTint(drawable);
    }

    @Test
    public void testGetTintedDrawableRes() throws Exception {
        final Context context = RuntimeEnvironment.application;

        final Drawable source = ContextCompat.getDrawable(context,
                R.drawable.appwidget_preview_single_row);

        final Drawable drawable = DrawableUtils.getTintedDrawable(source,
                ContextCompat.getColorStateList(context, R.color.colorAccent));

        assertHasTint(drawable);
    }

    private void assertHasNoTint(@Nullable final Drawable drawable) throws Exception {
        assertNotNull(drawable);
        // For pre-M, DrawableCompat returns a DrawableWrapper, which is harder to check.
        // And probably not necessary.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertTrue(drawable instanceof BitmapDrawable);
            assertNull(getTint(drawable));
        }
    }

    private void assertHasTint(@Nullable final Drawable drawable) throws Exception {
        assertNotNull(drawable);
        // For pre-M, DrawableCompat returns a DrawableWrapper, which is harder to check.
        // And probably not necessary.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            assertTrue(drawable instanceof BitmapDrawable);
            assertNotNull(getTint(drawable));
        }
    }

    @Nullable
    private Object getTint(@NonNull final Drawable drawable) throws Exception {
        final Method methodGetTint = BitmapDrawable.class.getDeclaredMethod("getTint");
        methodGetTint.setAccessible(true);
        return methodGetTint.invoke(drawable);
    }
}
