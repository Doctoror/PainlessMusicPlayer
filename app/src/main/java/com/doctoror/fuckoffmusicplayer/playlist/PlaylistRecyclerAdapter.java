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
package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.BindingAdapters;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * "Playlist" recycler adapter
 */
final class PlaylistRecyclerAdapter extends BaseRecyclerAdapter<Object, PlaylistItemViewHolder> {

    interface TrackListener {

        void onTrackClick(@NonNull View itemView, int position);

        void onTracksSwapped(int i, int j);
    }

    private TrackListener mTrackListener;

    PlaylistRecyclerAdapter(@NonNull final Context context,
            @NonNull final List<Media> items) {
        super(context, toObjectList(items), true);
    }

    public void setPlaylist(@Nullable final List<Media> items) {
        setItems(items == null ? null : toObjectList(items));
    }

    @NonNull
    private static List<Object> toObjectList(@NonNull final List<Media> items) {
        return new ArrayList<>(items);
    }

    boolean onItemMove(final int fromPosition, final int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                swap(i, i + 1);
                onTracksSwapped(i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                swap(i, i - 1);
                onTracksSwapped(i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    boolean canRemove(final int position) {
        return getItem(position) instanceof Media;
    }

    boolean canDrag(final int position) {
        return getItem(position) instanceof Media;
    }

    void setTrackListener(@NonNull final TrackListener trackListener) {
        mTrackListener = trackListener;
    }

    private void onTrackClick(@NonNull final View itemView, final int position) {
        if (mTrackListener != null) {
            mTrackListener.onTrackClick(itemView, position);
        }
    }

    private void onTracksSwapped(final int i, final int j) {
        if (mTrackListener != null) {
            mTrackListener.onTracksSwapped(i, j);
        }
    }

    @Override
    public void onBindViewHolder(final PlaylistItemViewHolder viewHolder, final int position) {
        final Media item = (Media) getItem(position);
        viewHolder.textTitle.setText(item.title);
        viewHolder.textArtist.setText(item.artist);
        BindingAdapters.setFormattedDuration(viewHolder.textDuration, item.duration / 1000L);
    }

    @Override
    public PlaylistItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final PlaylistItemViewHolder vh = new PlaylistItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_media, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final int position = vh.getAdapterPosition();
            final Object item = getItem(position);
            if (item instanceof Media) {
                onTrackClick(vh.itemView, position);
            }
        });
        return vh;
    }
}
