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
package com.doctoror.fuckoffmusicplayer.util;

import com.doctoror.fuckoffmusicplayer.R;

import org.junit.Test;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.v4.content.ContextCompat;

import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link DrawableUtils} test
 */
public final class DrawableUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testGetTintedDrawableFromResNullContext() throws Exception {
        //noinspection ConstantConditions
        DrawableUtils.getTintedDrawable(null, 0, null);
    }

    @Test
    public void testGetTintedDrawableFromResNullTint() throws Exception {
        final Drawable drawable = DrawableUtils.getTintedDrawable(
                InstrumentationRegistry.getTargetContext(),
                R.drawable.appwidget_preview_single_row, null);

        assertHasNoTint(drawable);
    }

    @Test
    public void testGetTintedDrawableFromRes() throws Exception {
        final Context context = InstrumentationRegistry.getTargetContext();
        final Drawable drawable = DrawableUtils.getTintedDrawable(context,
                R.drawable.appwidget_preview_single_row,
                ContextCompat.getColorStateList(context, R.color.colorAccent));

        assertHasTint(drawable);
    }

    @Test
    public void testGetTintedDrawableNullTint() throws Exception {
        final Drawable source = ContextCompat.getDrawable(
                InstrumentationRegistry.getTargetContext(),
                R.drawable.appwidget_preview_single_row);

        final Drawable drawable = DrawableUtils.getTintedDrawable(source, null);

        assertHasNoTint(drawable);
    }

    @Test
    public void testGetTintedDrawableRes() throws Exception {
        final Context context = InstrumentationRegistry.getTargetContext();

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
