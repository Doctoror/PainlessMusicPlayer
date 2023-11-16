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
package com.doctoror.fuckoffmusicplayer.presentation.library.artists;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.artists.ArtistsProviderKt;
import com.doctoror.fuckoffmusicplayer.presentation.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.presentation.widget.viewholder.TwoLineItemViewHolder;
import com.l4digital.fastscroll.FastScroller;

/**
 * Artists recycler view adapter
 */
final class ArtistsRecyclerAdapter extends CursorRecyclerViewAdapter<TwoLineItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnArtistClickListener {

        void onArtistClick(int position, long id, @Nullable String artist);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @NonNull
    private final Resources mResources;

    private OnArtistClickListener mClickListener;

    ArtistsRecyclerAdapter(@NonNull final Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mResources = context.getResources();
    }

    void setOnArtistClickListener(@Nullable final OnArtistClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void onItemClick(final int position) {
        final Cursor item = getCursor();
        if (item != null && item.moveToPosition(position)) {
            onArtistClick(position,
                    item.getLong(ArtistsProviderKt.COLUMN_ID),
                    item.getString(ArtistsProviderKt.COLUMN_ARTIST));
        }
    }

    private void onArtistClick(final int position, final long id, @NonNull final String artist) {
        if (mClickListener != null) {
            mClickListener.onArtistClick(position, id, artist);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull final TwoLineItemViewHolder viewHolder,
            @NonNull final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(ArtistsProviderKt.COLUMN_ARTIST));
        final int albumsCount = cursor.getInt(ArtistsProviderKt.COLUMN_NUMBER_OF_ALBUMS);
        viewHolder.text2.setText(mResources.getQuantityString(R.plurals.d_albums,
                albumsCount, albumsCount));
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
            final String artist = c.getString(ArtistsProviderKt.COLUMN_ARTIST);
            if (!TextUtils.isEmpty(artist)) {
                return String.valueOf(artist.charAt(0));
            }
        }
        return null;
    }
}
