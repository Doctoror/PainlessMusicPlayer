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
package com.doctoror.fuckoffmusicplayer.library.albums.conditional;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
final class ConditionalAlbumsRecyclerAdapter
        extends CursorRecyclerViewAdapter<AlbumListViewHolder> {

    interface OnAlbumClickListener {

        void onAlbumClick(long id, String album, String art);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @NonNull
    private final RequestManager mRequestManager;

    private OnAlbumClickListener mOnAlbumClickListener;

    ConditionalAlbumsRecyclerAdapter(final Context context,
            @NonNull final RequestManager requestManager) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
        mRequestManager = requestManager;
    }

    void setOnAlbumClickListener(@Nullable final OnAlbumClickListener onAlbumClickListener) {
        mOnAlbumClickListener = onAlbumClickListener;
    }

    private void onAlbumClick(final long id, @NonNull final String album,
            @NonNull final String art) {
        if (mOnAlbumClickListener != null) {
            mOnAlbumClickListener.onAlbumClick(id, album, art);
        }
    }

    @Override
    public void onBindViewHolder(final AlbumListViewHolder viewHolder, final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(ConditionalAlbumListQuery.COLUMN_ALBUM));
        viewHolder.text2.setText(cursor.getString(ConditionalAlbumListQuery.COLUMN_FIRST_YEAR));
        final String artLocation = cursor.getString(ConditionalAlbumListQuery.COLUMN_ALBUM_ART);
        if (TextUtils.isEmpty(artLocation)) {
            Glide.clear(viewHolder.image);
            viewHolder.image.setImageResource(R.drawable.album_art_placeholder);
        } else {
            mRequestManager
                    .load(artLocation)
                    .placeholder(R.drawable.album_art_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(viewHolder.image);
        }
    }

    @Override
    public AlbumListViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final AlbumListViewHolder vh = new AlbumListViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_two_line_icon, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final Cursor item = getCursor();
            if (item != null && item.moveToPosition(vh.getAdapterPosition())) {
                onAlbumClick(item.getLong(ConditionalAlbumListQuery.COLUMN_ID),
                        item.getString(ConditionalAlbumListQuery.COLUMN_ALBUM),
                        item.getString(ConditionalAlbumListQuery.COLUMN_ALBUM_ART));
            }
        });
        return vh;
    }
}
