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

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderPlaylists;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.widget.CursorRecyclerViewAdapter;
import com.doctoror.fuckoffmusicplayer.widget.viewholder.SingleLineItemIconMenuViewHolder;
import com.doctoror.fuckoffmusicplayer.widget.viewholder.SingleLineItemIconViewHolder;
import com.l4digital.fastscroll.FastScroller;

import java.util.List;

/**
 * Playlists recycler view adapter
 */
final class PlaylistsRecyclerAdapter
        extends CursorRecyclerViewAdapter<RecyclerView.ViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnPlaylistClickListener {

        void onLivePlaylistClick(@NonNull LivePlaylist playlist, int position);

        void onPlaylistClick(long id, @Nullable String name, int position);

        void onPlaylistDeleteClick(long id, @Nullable String name);
    }

    private static final int VIEW_TYPE_PLAYLIST_LIVE = 0;
    private static final int VIEW_TYPE_PLAYLIST = 1;

    @NonNull
    private final List<LivePlaylist> mLivePlaylists;

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @Nullable
    private final Drawable mIcon;

    @Nullable
    private final Drawable mIconMenu;

    private OnPlaylistClickListener mClickListener;

    PlaylistsRecyclerAdapter(
            @NonNull final Context context,
            @NonNull final List<LivePlaylist> livePlaylists) {
        mLayoutInflater = LayoutInflater.from(context);
        mLivePlaylists = livePlaylists;
        mIcon = DrawableUtils.getTintedDrawableFromAttrTint(context,
                R.drawable.ic_queue_music_black_40dp, android.R.attr.textColorPrimary);

        mIconMenu = DrawableUtils.getTintedDrawableFromAttrTint(context,
                R.drawable.ic_more_vert_black_24dp, android.R.attr.textColorPrimary);
    }

    @Override
    public int getItemViewType(final int position) {
        return position < mLivePlaylists.size() ? VIEW_TYPE_PLAYLIST_LIVE : VIEW_TYPE_PLAYLIST;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + mLivePlaylists.size();
    }

    @Override
    public long getItemId(final int position) {
        if (position < mLivePlaylists.size()) {
            return mLivePlaylists.get(position).getType();
        }
        return super.getItemId(position - mLivePlaylists.size());
    }

    @Nullable
    private Cursor getCursorItem(final int position) {
        if (getItemViewType(position) == VIEW_TYPE_PLAYLIST) {
            final Cursor cursor = getCursor();
            if (cursor != null && cursor.moveToPosition(position - mLivePlaylists.size())) {
                return cursor;
            }
        }
        return null;
    }

    void setOnPlaylistClickListener(@Nullable final OnPlaylistClickListener clickListener) {
        mClickListener = clickListener;
    }

    private void onItemClick(final int position) {
        if (position < mLivePlaylists.size()) {
            onLivePlaylistClick(mLivePlaylists.get(position), position);
        } else {
            final int cursorPosition = position - mLivePlaylists.size();
            final Cursor item = getCursor();
            if (item != null && item.moveToPosition(cursorPosition)) {
                onPlaylistClick(
                        item.getLong(QueueProviderPlaylists.COLUMN_ID),
                        item.getString(QueueProviderPlaylists.COLUMN_NAME),
                        position);
            }
        }
    }

    private void onLivePlaylistClick(@NonNull final LivePlaylist livePlaylist, final int position) {
        if (mClickListener != null) {
            mClickListener.onLivePlaylistClick(livePlaylist, position);
        }
    }

    private void onPlaylistClick(final long id, @NonNull final String playlist,
                                 final int position) {
        if (mClickListener != null) {
            mClickListener.onPlaylistClick(id, playlist, position);
        }
    }

    private void onMenuClick(@NonNull final View btnView, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_PLAYLIST) {
            final PopupMenu popup = new PopupMenu(btnView.getContext(), btnView);
            final Menu popupMenu = popup.getMenu();

            final MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.list_item_playlist, popupMenu);

            final Cursor item = getCursorItem(position);
            if (item != null) {
                final long id = item.getLong(QueueProviderPlaylists.COLUMN_ID);
                final String name = item.getString(QueueProviderPlaylists.COLUMN_NAME);
                popup.setOnMenuItemClickListener(
                        menuItem -> onMenuItemClick(menuItem, id, name));
                popup.show();
            }
        }
    }

    private boolean onMenuItemClick(@NonNull final MenuItem menuItem,
                                    final long itemId,
                                    @NonNull final String name) {
        switch (menuItem.getItemId()) {
            case R.id.actionDelete:
                if (mClickListener != null) {
                    mClickListener.onPlaylistDeleteClick(itemId, name);
                }
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder,
                                 final int position) {
        if (position < mLivePlaylists.size()) {
            onBindViewHolderLivePlaylist((SingleLineItemIconViewHolder) viewHolder,
                    mLivePlaylists.get(position));
        } else {
            final Cursor cursor = getCursor();
            if (cursor == null) {
                throw new IllegalStateException("Cursor is null, position = " + position);
            }
            final int cursorPos = position - mLivePlaylists.size();
            if (!cursor.moveToPosition(cursorPos)) {
                throw new IllegalStateException("couldn't move cursor to position " + cursorPos);
            }
            onBindViewHolder(viewHolder, cursor);
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull final RecyclerView.ViewHolder viewHolder,
            @NonNull final Cursor cursor) {
        final SingleLineItemIconMenuViewHolder vh = (SingleLineItemIconMenuViewHolder) viewHolder;
        vh.text.setText(cursor.getString(QueueProviderPlaylists.COLUMN_NAME));
    }

    private void onBindViewHolderLivePlaylist(
            @NonNull final SingleLineItemIconViewHolder viewHolder,
            @NonNull final LivePlaylist livePlaylist) {
        viewHolder.text.setText(livePlaylist.getTitle());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            final int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PLAYLIST_LIVE:
                return onCreateViewHolderLivePlaylist(parent);

            case VIEW_TYPE_PLAYLIST:
                return onCreateViewHolderPlaylist(parent);

            default:
                throw new IllegalArgumentException("Unexpected viewType: " + viewType);
        }
    }

    @NonNull
    private RecyclerView.ViewHolder onCreateViewHolderLivePlaylist(
            @NonNull final ViewGroup parent) {
        final SingleLineItemIconViewHolder vh = new SingleLineItemIconViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_single_line_icon, parent, false));
        vh.icon.setImageDrawable(mIcon);
        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));
        return vh;
    }

    @NonNull
    private RecyclerView.ViewHolder onCreateViewHolderPlaylist(
            @NonNull final ViewGroup parent) {
        final SingleLineItemIconMenuViewHolder vh = new SingleLineItemIconMenuViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_single_line_icon_with_menu, parent,
                        false));

        vh.icon.setImageDrawable(mIcon);
        vh.btnMenu.setImageDrawable(mIconMenu);

        vh.btnMenu.setOnClickListener(v -> onMenuClick(v, vh.getAdapterPosition()));
        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            final String name = c.getString(QueueProviderPlaylists.COLUMN_NAME);
            if (!TextUtils.isEmpty(name)) {
                return String.valueOf(name.charAt(0));
            }
        }
        return null;
    }
}
