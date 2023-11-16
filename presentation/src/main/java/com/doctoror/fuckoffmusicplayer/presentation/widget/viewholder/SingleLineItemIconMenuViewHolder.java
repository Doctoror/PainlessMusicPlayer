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
package com.doctoror.fuckoffmusicplayer.presentation.widget.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.doctoror.fuckoffmusicplayer.R;

/**
 * Single line item with icon and menu view holder
 */
public final class SingleLineItemIconMenuViewHolder extends RecyclerView.ViewHolder {

    public final ImageView icon;
    public final TextView text;
    public final ImageView btnMenu;

    public SingleLineItemIconMenuViewHolder(final View itemView) {
        super(itemView);
        icon = itemView.findViewById(android.R.id.icon);
        text = itemView.findViewById(android.R.id.text1);
        btnMenu = itemView.findViewById(R.id.btnMenu);
    }
}
