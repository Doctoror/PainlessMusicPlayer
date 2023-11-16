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

import android.content.res.ColorStateList;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Utils for obtaining {@link Theme} attributes
 */
public final class ThemeUtils {

    private ThemeUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns color for attr from the {@link Theme}
     *
     * @param theme {@link Theme} to get int from
     * @param attr  Attribute of the int
     * @return color for attr from the {@link Theme}
     */
    @ColorInt
    public static int getColor(@NonNull final Theme theme, @AttrRes final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[]{attr});
        try {
            return array.getColor(0, Color.TRANSPARENT);
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns {@link ColorStateList} for attr from the {@link Theme}
     *
     * @param theme {@link Theme} to get int from
     * @param attr  Attribute of the int
     * @return {@link ColorStateList} for attr from the {@link Theme}
     */
    @Nullable
    public static ColorStateList getColorStateList(@NonNull final Theme theme,
            @AttrRes final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[]{attr});
        try {
            return array.getColorStateList(0);
        } finally {
            array.recycle();
        }
    }

    /**
     * Returns {@link ColorStateList} for attr from the {@link Theme}
     *
     * @param theme {@link Theme} to get int from
     * @param attr  Attribute of the int
     * @return {@link Drawable} for attr from the {@link Theme}
     */
    @Nullable
    public static Drawable getDrawable(@NonNull final Theme theme,
            @AttrRes final int attr) {
        final TypedArray array = theme.obtainStyledAttributes(new int[]{attr});
        try {
            return array.getDrawable(0);
        } finally {
            array.recycle();
        }
    }
}
