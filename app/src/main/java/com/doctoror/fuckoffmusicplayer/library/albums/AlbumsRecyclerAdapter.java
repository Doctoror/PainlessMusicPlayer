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
package com.doctoror.fuckoffmusicplayer.library.albums;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.widget.AlbumViewHolder;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
final class AlbumsRecyclerAdapter extends CursorRecyclerViewAdapter<AlbumViewHolder>
    implements FastScroller.SectionIndexer {

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @NonNull
    private final RequestManager mRequestManager;

    interface OnAlbumClickListener {
        void onAlbumClick(View albumArtView, long id, String album, String art);
    }

    private OnAlbumClickListener mOnAlbumClickListener;

    AlbumsRecyclerAdapter(final Context context, @NonNull final RequestManager requestManager) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
        mRequestManager = requestManager;
    }

    public void setOnAlbumClickListener(@Nullable final OnAlbumClickListener onAlbumClickListener) {
        mOnAlbumClickListener = onAlbumClickListener;
    }

    private void onAlbumClick(@NonNull final View view, final long id, @NonNull final String album,
            @NonNull final String art) {
        if (mOnAlbumClickListener != null) {
            mOnAlbumClickListener.onAlbumClick(view, id, album, art);
        }
    }

    @Override
    public void onBindViewHolder(final AlbumViewHolder viewHolder, final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(AlbumsQuery.COLUMN_ALBUM));
        final String artLocation = cursor.getString(AlbumsQuery.COLUMN_ALBUM_ART);
        if (TextUtils.isEmpty(artLocation)) {
            Glide.clear(viewHolder.image);
            viewHolder.image.setImageResource(R.drawable.album_art_placeholder);
        } else {
            mRequestManager
                    .load(artLocation)
                    .placeholder(R.drawable.album_art_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontTransform()
                    .into(viewHolder.image);
        }
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final AlbumViewHolder vh = new AlbumViewHolder(
                mLayoutInflater.inflate(R.layout.recycler_item_album, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final Cursor item = getCursor();
            if (item != null && item.moveToPosition(vh.getAdapterPosition())) {
                onAlbumClick(vh.image,
                        item.getLong(AlbumsQuery.COLUMN_ID),
                        item.getString(AlbumsQuery.COLUMN_ALBUM),
                        item.getString(AlbumsQuery.COLUMN_ALBUM_ART));
            }
        });
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            return String.valueOf(c.getString(AlbumsQuery.COLUMN_ALBUM).charAt(0));
        }
        return null;
    }
}
