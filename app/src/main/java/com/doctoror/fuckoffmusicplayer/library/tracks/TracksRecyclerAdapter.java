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
import com.doctoror.fuckoffmusicplayer.domain.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.widget.viewholder.TwoLineItemViewHolder;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Recycler view adapter for "Tracks" list
 */
final class TracksRecyclerAdapter extends CursorRecyclerViewAdapter<TwoLineItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnTrackClickListener {
        void onTrackClick(int position, long id);
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

    private void onItemClick(final int position) {
        final Cursor item = getCursor();
        if (item != null && item.moveToPosition(position)) {
            onTrackClick(position, item.getLong(TracksProvider.COLUMN_ID));
        }
    }

    private void onTrackClick(final int position, final long id) {
        if (mClickListener != null) {
            mClickListener.onTrackClick(position, id);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull final TwoLineItemViewHolder viewHolder,
            @NonNull final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(TracksProvider.COLUMN_TITLE));
        viewHolder.text2.setText(cursor.getString(TracksProvider.COLUMN_ARTIST));
    }

    @NonNull
    @Override
    public TwoLineItemViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent, final int viewType) {
        final TwoLineItemViewHolder vh = new TwoLineItemViewHolder(mLayoutInflater.inflate(
                R.layout.list_item_two_line, parent, false));
        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            final String title = c.getString(TracksProvider.COLUMN_TITLE);
            if (!TextUtils.isEmpty(title)) {
                return String.valueOf(title.charAt(0));
            }
        }
        return null;
    }
}
