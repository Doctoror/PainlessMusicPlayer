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
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Drawable utils
 */
public final class DrawableUtils {

    private DrawableUtils() {

    }

    @Nullable
    public static Drawable getTintedDrawableFromAttrTint(@NonNull final Context context,
            @DrawableRes final int res,
            @AttrRes final int tintAttr) {
        return getTintedDrawable(context, res,
                ThemeUtils.getColorStateList(context.getTheme(), tintAttr));
    }

    @Nullable
    public static Drawable getTintedDrawableFromAttrTint(@NonNull final Context context,
            @Nullable final Drawable src,
            @AttrRes final int tintAttr) {
        return getTintedDrawable(src, ThemeUtils.getColorStateList(context.getTheme(), tintAttr));
    }

    @Nullable
    public static Drawable getTintedDrawable(@NonNull final Context context,
            @DrawableRes final int res,
            @Nullable final ColorStateList tint) {
        final Drawable d = ResourcesCompat.getDrawable(context.getResources(), res,
                context.getTheme());
        return getTintedDrawable(d, tint);
    }

    @Nullable
    public static Drawable getTintedDrawable(@NonNull final Context context,
            @DrawableRes final int res,
            @ColorInt final int tint) {
        final Drawable d = ResourcesCompat.getDrawable(context.getResources(), res,
                context.getTheme());
        return getTintedDrawable(d, tint);
    }

    @Nullable
    public static Drawable getTintedDrawable(@Nullable Drawable drawable,
            @Nullable final ColorStateList tint) {
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tint);
        }
        return drawable;
    }

    @Nullable
    public static Drawable getTintedDrawable(@Nullable Drawable drawable,
            @ColorInt final int tint) {
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, tint);
        }
        return drawable;
    }
}
