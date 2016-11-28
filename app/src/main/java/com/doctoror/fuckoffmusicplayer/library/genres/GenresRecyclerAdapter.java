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
package com.doctoror.fuckoffmusicplayer.library.genres;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.util.ThemeUtils;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.widget.SingleLineWithIconItemViewHolder;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
final class GenresRecyclerAdapter
        extends CursorRecyclerViewAdapter<SingleLineWithIconItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnGenreClickListener {

        void onGenreClick(@NonNull View itemView, long id, @NonNull String genre);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    private final Drawable mIcon;

    private OnGenreClickListener mClickListener;

    GenresRecyclerAdapter(final Context context) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
        mIcon = DrawableUtils.getTintedDrawable(context, R.drawable.ic_library_music_black_40dp,
                ThemeUtils.getColorStateList(context.getTheme(), android.R.attr.textColorPrimary));
    }

    void setOnGenreClickListener(@Nullable final OnGenreClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void onGenreClick(@NonNull final View itemView, final long id,
            @NonNull final String genre) {
        if (mClickListener != null) {
            mClickListener.onGenreClick(itemView, id, genre);
        }
    }

    @Override
    public void onBindViewHolder(final SingleLineWithIconItemViewHolder viewHolder,
            final Cursor cursor) {
        viewHolder.text.setText(cursor.getString(GenresQuery.COLUMN_NAME));
    }

    @Override
    public SingleLineWithIconItemViewHolder onCreateViewHolder(
            final ViewGroup parent, final int viewType) {
        final SingleLineWithIconItemViewHolder vh = new SingleLineWithIconItemViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_single_line_icon, parent, false));
        vh.icon.setImageDrawable(mIcon);
        vh.itemView.setOnClickListener(v -> {
            final Cursor item = getCursor();
            if (item != null && item.moveToPosition(vh.getAdapterPosition())) {
                onGenreClick(
                        vh.itemView,
                        item.getLong(GenresQuery.COLUMN_ID),
                        item.getString(GenresQuery.COLUMN_NAME));
            }
        });
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            return String.valueOf(c.getString(GenresQuery.COLUMN_NAME).charAt(0));
        }
        return null;
    }
}
