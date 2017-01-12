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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.fuckoffmusicplayer.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingComponent;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Yaroslav Mytkaylyk on 6/16/16.
 * <p>
 * General binding adapters
 */
public final class BindingAdapters {

    private BindingAdapters() {

    }

    @NonNull
    public static DataBindingComponent glideBindingComponent(
            @NonNull final RequestManager requestManager) {
        return new GlideBindingComponent(requestManager);
    }

    public static final class GlideBindingComponent implements DataBindingComponent {

        @NonNull
        private final RequestManager mRequestManager;

        GlideBindingComponent(@NonNull final RequestManager requestManager) {
            mRequestManager = requestManager;
        }

        @BindingAdapter({"placeholder", "imageUri"})
        public void setImageUri(@NonNull final ImageView imageView,
                @Nullable final Drawable placeholder,
                @Nullable final String imageUri) {
            if (imageUri == null) {
                Glide.clear(imageView);
                imageView.setImageDrawable(placeholder);
            } else {
                mRequestManager.load(imageUri)
                        .placeholder(placeholder)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView);
            }
        }

        @Override
        public GlideBindingComponent getGlideBindingComponent() {
            return this;
        }
    }

    @BindingAdapter("formattedDuration")
    public static void setFormattedDuration(@NonNull final TextView textView,
            final long seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be a positive value");
        }
        final int secondsInHour = 3600;
        final int secondsInMinute = 60;
        final String time;
        if (seconds >= secondsInHour) {
            time = String.format(Locale.US,
                    "%d:%02d:%02d",
                    seconds / secondsInHour,
                    (seconds % secondsInHour) / secondsInMinute,
                    seconds % secondsInMinute);
        } else {
            time = String.format(Locale.US,
                    "%d:%02d",
                    (seconds % secondsInHour) / secondsInMinute,
                    seconds % secondsInMinute);
        }
        textView.setText(time);
    }

    @BindingAdapter("recyclerAdapter")
    public static void setRecyclerAdapter(@NonNull final RecyclerView recyclerView,
            @Nullable final RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }

    @BindingAdapter({"drawableTop", "tintAttr"})
    public static void setDrawableTopTintedFromAttr(@NonNull final TextView textView,
            @Nullable Drawable top,
            @AttrRes final int tintAttr) {
        final Drawable[] drawables = textView.getCompoundDrawables();
        if (top != null) {
            top = DrawableUtils.getTintedDrawableFromAttrTint(textView.getContext(), top, tintAttr);
        }
        textView.setCompoundDrawablesWithIntrinsicBounds(
                drawables[0],
                top,
                drawables[2],
                drawables[3]);
    }

    @BindingAdapter({"src", "tintNormal", "useActivatedSrcTint"})
    public static void setUseActivatedSrcTint(@NonNull final ImageView imageView,
            @Nullable final Drawable src,
            @ColorInt final int tintNormal,
            final boolean useActivatedSrcTint) {
        if (!useActivatedSrcTint || src == null) {
            imageView.setImageDrawable(src);
        } else {
            imageView.setImageDrawable(DrawableUtils
                    .getTintedDrawable(src, activatedTint(imageView.getContext(), tintNormal)));
        }
    }

    @BindingAdapter("activated")
    public static void setActivated(@NonNull final View view, final boolean activated) {
        view.setActivated(activated);
    }

    @NonNull
    private static ColorStateList activatedTint(@NonNull final Context context,
            @ColorInt final int tintNormal) {

        final int[][] states = new int[][]{
                new int[]{android.R.attr.state_activated},
                new int[0]
        };

        final int[] colors = new int[]{
                ThemeUtils.getColor(context.getTheme(), R.attr.colorAccent),
                tintNormal
        };

        return new ColorStateList(states, colors);
    }
}
