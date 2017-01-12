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

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistsProvider;
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

import java.util.List;

/**
 * Playlists recycler view adapter
 */
final class PlaylistsRecyclerAdapter
        extends CursorRecyclerViewAdapter<SingleLineWithIconItemViewHolder>
        implements FastScroller.SectionIndexer {

    interface OnPlaylistClickListener {

        void onPlaylistClick(long id, String name, int position);
        void onLivePlaylistClick(LivePlaylist playlist, int position);
    }

    @NonNull
    private final List<LivePlaylist> mLivePlaylists;

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @Nullable
    private final Drawable mIcon;

    private OnPlaylistClickListener mClickListener;

    PlaylistsRecyclerAdapter(@NonNull final Context context,
            @NonNull final List<LivePlaylist> livePlaylists) {
        super(null);
        mLayoutInflater = LayoutInflater.from(context);
        mLivePlaylists = livePlaylists;
        mIcon = DrawableUtils.getTintedDrawable(context, R.drawable.ic_queue_music_black_40dp,
                ThemeUtils.getColorStateList(context.getTheme(), android.R.attr.textColorPrimary));
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
                        item.getLong(PlaylistsProvider.COLUMN_ID),
                        item.getString(PlaylistsProvider.COLUMN_NAME),
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

    @Override
    public void onBindViewHolder(final SingleLineWithIconItemViewHolder viewHolder,
            final int position) {
        if (position < mLivePlaylists.size()) {
            onBindViewHolderLivePlaylist(viewHolder, mLivePlaylists.get(position));
        } else {
            final Cursor cursor = getCursor();
            final int cursorPos = position - mLivePlaylists.size();
            if (!cursor.moveToPosition(cursorPos)) {
                throw new IllegalStateException("couldn't move cursor to position " + cursorPos);
            }
            onBindViewHolder(viewHolder, cursor);
        }
    }

    @Override
    public void onBindViewHolder(final SingleLineWithIconItemViewHolder viewHolder,
            final Cursor cursor) {
        viewHolder.text.setText(cursor.getString(PlaylistsProvider.COLUMN_NAME));
    }

    private void onBindViewHolderLivePlaylist(final SingleLineWithIconItemViewHolder viewHolder,
            @NonNull final LivePlaylist livePlaylist) {
        viewHolder.text.setText(livePlaylist.getTitle());
    }

    @Override
    public SingleLineWithIconItemViewHolder onCreateViewHolder(final ViewGroup parent,
            final int viewType) {
        final SingleLineWithIconItemViewHolder vh = new SingleLineWithIconItemViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_single_line_icon, parent, false));
        vh.icon.setImageDrawable(mIcon);
        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));
        return vh;
    }

    @Override
    public String getSectionText(final int position) {
        final Cursor c = getCursor();
        if (c != null && c.moveToPosition(position)) {
            return String.valueOf(c.getString(PlaylistsProvider.COLUMN_NAME).charAt(0));
        }
        return null;
    }
}
