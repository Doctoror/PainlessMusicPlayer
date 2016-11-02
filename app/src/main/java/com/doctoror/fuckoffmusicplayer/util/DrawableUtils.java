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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;

/**
 * Created by Yaroslav Mytkalyk on 7/13/16.
 */
public final class DrawableUtils {

    private DrawableUtils() {

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
    public static Drawable getTintedDrawable(@Nullable Drawable drawable,
            @Nullable final ColorStateList tint) {
        if (drawable != null && tint != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTintList(drawable, tint);
        }
        return drawable;
    }
}
