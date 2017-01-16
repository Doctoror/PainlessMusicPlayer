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
import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.widget.viewholder.AlbumWithMenuViewHolder;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.l4digital.fastscroll.FastScroller;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

/**
 * "Albums" recycler adapter
 */
final class AlbumsRecyclerAdapter extends CursorRecyclerViewAdapter<AlbumWithMenuViewHolder>
        implements FastScroller.SectionIndexer {

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @NonNull
    private final RequestManager mRequestManager;

    interface OnAlbumClickListener {

        void onAlbumClick(int position, long id, String album);

        void onAlbumDeleteClick(long id, @Nullable String name);
    }

    private OnAlbumClickListener mOnAlbumClickListener;

    AlbumsRecyclerAdapter(final Context context,
            @NonNull final RequestManager requestManager) {
        super(null);
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

    private void onAlbumClick(final int position, final long id, @NonNull final String album) {
        if (mOnAlbumClickListener != null) {
            mOnAlbumClickListener.onAlbumClick(position, id, album);
        }
    }

    private void onMenuClick(@NonNull final View btnView, final int position) {
        final PopupMenu popup = new PopupMenu(btnView.getContext(), btnView);
        final Menu popupMenu = popup.getMenu();

        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.list_item_album, popupMenu);

        final long id = getItemId(position);
        final Cursor item = getCursor();

        if (item.moveToPosition(position)) {
            final String albumName = item.getString(AlbumsProvider.COLUMN_ALBUM);
            popup.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem, id, albumName));
            popup.show();
        }
    }

    private boolean onMenuItemClick(@NonNull final MenuItem menuItem,
            final long itemId,
            @NonNull final String name) {
        switch (menuItem.getItemId()) {
            case R.id.actionDelete:
                if (mOnAlbumClickListener != null) {
                    mOnAlbumClickListener.onAlbumDeleteClick(itemId, name);
                }
                return true;

            default:
                return false;
        }
    }


    @Override
    public void onBindViewHolder(final AlbumWithMenuViewHolder viewHolder, final Cursor cursor) {
        viewHolder.text1.setText(cursor.getString(AlbumsProvider.COLUMN_ALBUM));
        final String artLocation = cursor.getString(AlbumsProvider.COLUMN_ALBUM_ART);
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
    public AlbumWithMenuViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final AlbumWithMenuViewHolder vh = new AlbumWithMenuViewHolder(
                mLayoutInflater.inflate(R.layout.recycler_item_album_with_menu, parent, false));

        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));

        vh.btnMenu.setImageDrawable(DrawableUtils.getTintedDrawable(vh.btnMenu.getContext(),
                R.drawable.ic_more_vert_black_24dp, Color.WHITE));

        vh.btnMenu.setOnClickListener(v -> onMenuClick(v, vh.getAdapterPosition()));

        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            final String album = c.getString(AlbumsProvider.COLUMN_ALBUM);
            if (!TextUtils.isEmpty(album)) {
                return String.valueOf(album.charAt(0));
            }
        }
        return null;
    }
}
