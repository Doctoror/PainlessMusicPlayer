/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.queue;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.presentation.util.ThemeUtils;
import com.doctoror.fuckoffmusicplayer.presentation.widget.ItemTouchHelperViewHolder;

/**
 * View holder for media in playlist
 */
final class QueueItemViewHolder extends RecyclerView.ViewHolder implements
        ItemTouchHelperViewHolder {

    final ImageView btnMenu;

    final TextView textTitle;

    final TextView textArtist;

    final TextView textDuration;

    @Nullable
    private final Drawable mDefaultBackground;
    private final float mDefaultElevation;

    final float mElevationSelected;

    private Drawable mSelectedBackground;

    QueueItemViewHolder(@NonNull final View itemView) {
        super(itemView);
        btnMenu = itemView.findViewById(R.id.btnMenu);
        textTitle = itemView.findViewById(R.id.textTitle);
        textArtist = itemView.findViewById(R.id.textArtist);
        textDuration = itemView.findViewById(R.id.textDuration);

        mDefaultBackground = itemView.getBackground();
        mDefaultElevation = ViewCompat.getElevation(itemView);

        mElevationSelected = itemView.getResources()
                .getDimension(R.dimen.list_drag_selected_item_elevation);
    }

    @Override
    public void onItemSelected() {
        itemView.setBackground(getSelectedBackground(itemView.getContext()));
        ViewCompat.setElevation(itemView, mElevationSelected);
    }

    @Override
    public void onItemClear() {
        itemView.setBackground(mDefaultBackground);
        ViewCompat.setElevation(itemView, mDefaultElevation);
    }

    @NonNull
    private Drawable getSelectedBackground(@NonNull final Context context) {
        if (mSelectedBackground != null) {
            return mSelectedBackground;
        }

        mSelectedBackground = new LayerDrawable(new Drawable[]{
                new ColorDrawable(ThemeUtils.getColor(context.getTheme(),
                        android.R.attr.windowBackground)),
                new ColorDrawable(ContextCompat.getColor(context, R.color.dividerBackground))
        });

        return mSelectedBackground;
    }
}
