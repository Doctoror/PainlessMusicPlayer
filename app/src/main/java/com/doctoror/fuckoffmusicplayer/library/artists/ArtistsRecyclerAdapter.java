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
package com.doctoror.fuckoffmusicplayer.library.artists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.widget.TwoLineItemViewHolder;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
final class ArtistsRecyclerAdapter extends CursorRecyclerViewAdapter<TwoLineItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnArtistClickListener {
        void onArtistClick(long id, String artist);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @NonNull
    private final Resources mResources;

    private OnArtistClickListener mClickListener;

    ArtistsRecyclerAdapter(final Context context) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    void setOnArtistClickListener(@Nullable final OnArtistClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void onArtistClick(final long id, @NonNull final String artist) {
        if (mClickListener != null) {
            mClickListener.onArtistClick(id, artist);
        }
    }

    @Override
    public void onBindViewHolder(final TwoLineItemViewHolder viewHolder, final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(ArtistsQuery.COLUMN_ARTIST));
        final int albumsCount = cursor.getInt(ArtistsQuery.COLUMN_NUMBER_OF_ALBUMS);
        viewHolder.text2.setText(mResources.getQuantityString(R.plurals.d_albums,
                albumsCount, albumsCount));
    }

    @Override
    public TwoLineItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final TwoLineItemViewHolder vh = new TwoLineItemViewHolder(mLayoutInflater.inflate(
                R.layout.list_item_two_line, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final Cursor item = getCursor();
            if (item != null && item.moveToPosition(vh.getAdapterPosition())) {
                onArtistClick(item.getLong(ArtistsQuery.COLUMN_ID),
                        item.getString(ArtistsQuery.COLUMN_ARTIST));
            }
        });
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            return String.valueOf(c.getString(ArtistsQuery.COLUMN_ARTIST).charAt(0));
        }
        return null;
    }
}
