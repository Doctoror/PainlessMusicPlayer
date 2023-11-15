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
package com.doctoror.fuckoffmusicplayer.presentation.library.playlists;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.doctoror.fuckoffmusicplayer.R;

/**
 * "Live Playlist" ViewHolder
 */
final class LivePlaylistViewHolder extends RecyclerView.ViewHolder {

    public final ImageView icon;
    public final TextView text;
    public final View progress;

    LivePlaylistViewHolder(@NonNull final View itemView) {
        super(itemView);
        icon = itemView.findViewById(android.R.id.icon);
        text = itemView.findViewById(android.R.id.text1);
        progress = itemView.findViewById(R.id.progress);
    }
}
