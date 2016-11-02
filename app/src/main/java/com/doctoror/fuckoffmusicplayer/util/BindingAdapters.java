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

import android.databinding.BindingAdapter;
import android.databinding.DataBindingComponent;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewAnimator;

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

    @BindingAdapter("displayedChild")
    public static void setDisplayedChild(@NonNull final ViewAnimator viewAnimator,
            final int child) {
        if (viewAnimator.getDisplayedChild() != child) {
            viewAnimator.setDisplayedChild(child);
        }
    }

    @BindingAdapter("recyclerAdapter")
    public static void setRecyclerAdapter(@NonNull final RecyclerView recyclerView,
            @Nullable final RecyclerView.Adapter<?> adapter) {
        recyclerView.setAdapter(adapter);
    }

    @BindingAdapter("formattedDuration")
    public static void setFormattedDuration(@NonNull final TextView textView,
            final long seconds) {
        final String time;
        if (seconds > 3600) {
            time = String.format(Locale.US,
                    "%d:%02d:%02d",
                    seconds / 3600,
                    (seconds % 3600) / 60,
                    seconds % 60);
        } else {
            time = String.format(Locale.US,
                    "%d:%02d",
                    (seconds % 3600) / 60,
                    seconds % 60);
        }
        textView.setText(time);
    }

    @BindingAdapter("srcRes")
    public static void setImageResource(@NonNull final ImageView imageView,
            @DrawableRes final int src) {
        imageView.setImageResource(src);
    }
}
