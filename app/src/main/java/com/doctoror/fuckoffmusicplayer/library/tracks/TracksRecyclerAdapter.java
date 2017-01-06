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
package com.doctoror.fuckoffmusicplayer.library.tracks;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.widget.TwoLineItemViewHolder;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Recycler view adapter for "Tracks" list
 */
final class TracksRecyclerAdapter extends CursorRecyclerViewAdapter<TwoLineItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnTrackClickListener {
        void onTrackClick(View itemView, int position, long id);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    private OnTrackClickListener mClickListener;

    TracksRecyclerAdapter(final Context context) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
    }

    void setOnTrackClickListener(@Nullable final OnTrackClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void onTrackClick(@NonNull final View itemView, final int position, final long id) {
        if (mClickListener != null) {
            mClickListener.onTrackClick(itemView, position, id);
        }
    }

    @Override
    public void onBindViewHolder(final TwoLineItemViewHolder viewHolder, final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(TracksProvider.COLUMN_TITLE));
        viewHolder.text2.setText(cursor.getString(TracksProvider.COLUMN_ARTIST));
    }

    @Override
    public TwoLineItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final TwoLineItemViewHolder vh = new TwoLineItemViewHolder(mLayoutInflater.inflate(
                R.layout.list_item_two_line, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final Cursor item = getCursor();
            final int position = vh.getAdapterPosition();
            if (item != null && item.moveToPosition(vh.getAdapterPosition())) {
                onTrackClick(vh.itemView, position, item.getLong(TracksProvider.COLUMN_ID));
            }
        });
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            return String.valueOf(c.getString(TracksProvider.COLUMN_TITLE).charAt(0));
        }
        return null;
    }
}
