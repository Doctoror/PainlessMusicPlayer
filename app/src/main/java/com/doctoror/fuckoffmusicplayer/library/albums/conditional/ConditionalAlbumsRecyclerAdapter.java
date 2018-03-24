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

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;

/**
 * Albums list
 */
final class ConditionalAlbumsRecyclerAdapter
        extends CursorRecyclerViewAdapter<AlbumListViewHolder> {

    interface OnAlbumClickListener {

        void onAlbumClick(int position, long albumId, @Nullable String albumName);
    }

    private final RequestOptions requestOptions = new RequestOptions()
            .error(R.drawable.album_art_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE);

    private final LayoutInflater mLayoutInflater;

    private final RequestManager mRequestManager;

    private OnAlbumClickListener mOnAlbumClickListener;

    ConditionalAlbumsRecyclerAdapter(
            @NonNull final Context context,
            @NonNull final RequestManager requestManager) {
        mLayoutInflater = LayoutInflater.from(context);
        mRequestManager = requestManager;
    }

    void setOnAlbumClickListener(@Nullable final OnAlbumClickListener onAlbumClickListener) {
        mOnAlbumClickListener = onAlbumClickListener;
    }

    private void onItemClick(final int position) {
        final Cursor item = getCursor();
        if (item != null && item.moveToPosition(position)) {
            onAlbumClick(position,
                    item.getLong(AlbumsProvider.COLUMN_ID),
                    item.getString(AlbumsProvider.COLUMN_ALBUM));
        }
    }

    private void onAlbumClick(final int positon,
                              final long id,
                              @NonNull final String album) {
        if (mOnAlbumClickListener != null) {
            mOnAlbumClickListener.onAlbumClick(positon, id, album);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull final AlbumListViewHolder viewHolder,
            @NonNull final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(AlbumsProvider.COLUMN_ALBUM));
        viewHolder.text2.setText(cursor.getString(AlbumsProvider.COLUMN_FIRST_YEAR));
        final String artLocation = cursor.getString(AlbumsProvider.COLUMN_ALBUM_ART);
        if (TextUtils.isEmpty(artLocation)) {
            mRequestManager.clear(viewHolder.image);
            viewHolder.image.setImageResource(R.drawable.album_art_placeholder);
        } else {
            mRequestManager
                    .load(artLocation)
                    .apply(requestOptions)
                    .into(viewHolder.image);
        }
    }

    @NonNull
    @Override
    public AlbumListViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent, final int viewType) {
        final AlbumListViewHolder vh = new AlbumListViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_two_line_icon, parent, false));
        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));
        return vh;
    }
}
