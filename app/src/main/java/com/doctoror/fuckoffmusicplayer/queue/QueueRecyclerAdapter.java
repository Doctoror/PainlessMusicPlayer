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
package com.doctoror.fuckoffmusicplayer.queue;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.BindingAdapters;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.util.List;

/**
 * "Playlist" recycler adapter
 */
final class QueueRecyclerAdapter extends BaseRecyclerAdapter<Media, QueueItemViewHolder> {

    interface TrackListener {

        void onTrackClick(@NonNull View itemView, int position);

        void onTrackDeleteClick(@NonNull Media item);

        void onTracksSwapped(int i, int j);
    }

    private final Context mContext;

    private TrackListener mTrackListener;

    QueueRecyclerAdapter(@NonNull final Context context, @NonNull final List<Media> items) {
        super(context, items);
        mContext = context;
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

    void setTrackListener(@NonNull final TrackListener trackListener) {
        mTrackListener = trackListener;
    }

    private void onMenuClick(@NonNull final View itemView, final int position) {
        final PopupMenu popup = new PopupMenu(itemView.getContext(), itemView);
        final Menu popupMenu = popup.getMenu();

        final MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.list_item_media, popupMenu);

        final Media item = getItem(position);
        popup.setOnMenuItemClickListener(menuItem -> onMenuItemClick(menuItem, item));
        popup.show();
    }

    private boolean onMenuItemClick(@NonNull final MenuItem menuItem,
            @NonNull final Media item) {
        switch (menuItem.getItemId()) {
            case R.id.actionDelete:
                if (mTrackListener != null) {
                    mTrackListener.onTrackDeleteClick(item);
                }
                return true;

            default:
                return false;
        }
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
    public void onBindViewHolder(final QueueItemViewHolder viewHolder, final int position) {
        final Media item = getItem(position);
        viewHolder.textTitle.setText(item.title);
        viewHolder.textArtist.setText(item.artist);
        BindingAdapters.setFormattedDuration(viewHolder.textDuration, item.duration / 1000L);
    }

    @Override
    public QueueItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final QueueItemViewHolder vh = new QueueItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_media, parent, false));

        vh.btnMenu.setImageDrawable(DrawableUtils.getTintedDrawableFromAttrTint(mContext,
                R.drawable.ic_more_vert_black_24dp,
                android.R.attr.textColorPrimary));

        vh.itemView.setOnClickListener(v -> onTrackClick(vh.itemView, vh.getAdapterPosition()));
        vh.btnMenu.setOnClickListener(v -> onMenuClick(v, vh.getAdapterPosition()));
        return vh;
    }
}
