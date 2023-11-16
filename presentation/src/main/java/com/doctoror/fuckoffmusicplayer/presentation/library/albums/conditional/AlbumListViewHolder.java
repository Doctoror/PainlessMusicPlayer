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
package com.doctoror.fuckoffmusicplayer.presentation.library.albums.conditional;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * View holder for albums list
 */
final class AlbumListViewHolder extends RecyclerView.ViewHolder {

    public final TextView text1;
    public final TextView text2;
    public final ImageView image;

    AlbumListViewHolder(@NonNull final View itemView) {
        super(itemView);
        text1 = itemView.findViewById(android.R.id.text1);
        text2 = itemView.findViewById(android.R.id.text2);
        image = itemView.findViewById(android.R.id.icon);
    }
}
