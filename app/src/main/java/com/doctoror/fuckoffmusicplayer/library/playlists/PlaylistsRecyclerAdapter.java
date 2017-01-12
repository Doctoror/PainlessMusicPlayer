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
package com.doctoror.fuckoffmusicplayer.library.playlists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistsProvider;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.widget.SingleLineItemViewHolder;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Playlists recycler view adapter
 */
final class PlaylistsRecyclerAdapter extends CursorRecyclerViewAdapter<SingleLineItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnPlaylistClickListener {

        void onPlaylistClick(@NonNull View itemView, long id, String artist);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;


    private OnPlaylistClickListener mClickListener;

    PlaylistsRecyclerAdapter(final Context context) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
    }

    void setOnPlaylistClickListener(@Nullable final OnPlaylistClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void onPlaylistClick(@NonNull final View itemView,
            final long id, @NonNull final String playlist) {
        if (mClickListener != null) {
            mClickListener.onPlaylistClick(itemView, id, playlist);
        }
    }

    @Override
    public void onBindViewHolder(final SingleLineItemViewHolder viewHolder, final Cursor cursor) {
        viewHolder.text.setText(cursor.getString(PlaylistsProvider.COLUMN_NAME));
    }

    @Override
    public SingleLineItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final SingleLineItemViewHolder vh = new SingleLineItemViewHolder(mLayoutInflater.inflate(
                R.layout.list_item_single_line, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final Cursor item = getCursor();
            if (item != null && item.moveToPosition(vh.getAdapterPosition())) {
                onPlaylistClick(vh.itemView,
                        item.getLong(PlaylistsProvider.COLUMN_ID),
                        item.getString(PlaylistsProvider.COLUMN_NAME));
            }
        });
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            return String.valueOf(c.getString(PlaylistsProvider.COLUMN_NAME).charAt(0));
        }
        return null;
    }
}
