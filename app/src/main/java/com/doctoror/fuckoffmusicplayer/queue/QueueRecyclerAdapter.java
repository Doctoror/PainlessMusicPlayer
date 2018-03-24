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
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.util.BindingAdapters;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
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

    private static final int VIEW_TYPE_DEFAULT = 0;
    private static final int VIEW_TYPE_NOW_PLAYING = 1;

    private final Context mContext;

    private TrackListener mTrackListener;

    private long mNowPlayingId;

    QueueRecyclerAdapter(@NonNull final Context context, @NonNull final List<Media> items) {
        super(context, items);
        setHasStableIds(true);
        mContext = context;
    }

    @UiThread
    private int getItemPosForId(final long id) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItemId(i) == id) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getId();
    }

    @Override
    public int getItemViewType(final int position) {
        return getItemId(position) == mNowPlayingId ?
                VIEW_TYPE_NOW_PLAYING : VIEW_TYPE_DEFAULT;
    }

    @UiThread
    void setNowPlayingId(final long nowPlayingId) {
        if (mNowPlayingId != nowPlayingId) {
            final int oldItemPos = getItemPosForId(mNowPlayingId);
            final int newItemPos = getItemPosForId(nowPlayingId);
            mNowPlayingId = nowPlayingId;

            if (oldItemPos != -1 && newItemPos != -1) {
                if (Math.abs(oldItemPos - newItemPos) == 1) {
                    // Update as range
                    notifyItemRangeChanged(oldItemPos < newItemPos
                            ? oldItemPos : newItemPos, 2);
                } else {
                    // Update both
                    notifyItemChanged(oldItemPos);
                    notifyItemChanged(newItemPos);
                }
            } else if (oldItemPos != -1) {
                notifyItemChanged(oldItemPos);
            } else if (newItemPos != -1) {
                notifyItemChanged(newItemPos);
            }
        }
    }

    void removeItemWithId(final long id) {
        for (int i = 0; i < getItemCount(); i++) {
            final Media item = getItem(i);
            if (item.getId() == id) {
                removeItem(i);
                break;
            }
        }
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
    public void onBindViewHolder(
            @NonNull final QueueItemViewHolder viewHolder, final int position) {
        final Media item = getItem(position);
        viewHolder.textTitle.setText(item.getTitle());
        viewHolder.textArtist.setText(item.getArtist());
        BindingAdapters.setFormattedDuration(viewHolder.textDuration, item.getDuration() / 1000L);
    }

    @NonNull
    @Override
    public QueueItemViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case VIEW_TYPE_DEFAULT:
                return onCreateViewHolderDefault(parent);

            case VIEW_TYPE_NOW_PLAYING:
                return onCreateViewHolderNowPlaying(parent);

            default:
                throw new IllegalArgumentException("Unexpected viewType: " + viewType);
        }
    }

    @NonNull
    private QueueItemViewHolder onCreateViewHolderDefault(final ViewGroup parent) {
        final QueueItemViewHolder vh = new QueueItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_media, parent, false));
        initViewHolder(vh);
        return vh;
    }

    @NonNull
    private QueueItemViewHolder onCreateViewHolderNowPlaying(final ViewGroup parent) {
        final QueueItemViewHolder vh = new QueueItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_media_now_playing, parent, false));
        initViewHolder(vh);
        return vh;
    }

    private void initViewHolder(@NonNull final QueueItemViewHolder vh) {
        vh.btnMenu.setImageDrawable(DrawableUtils.getTintedDrawableFromAttrTint(mContext,
                R.drawable.ic_more_vert_black_24dp,
                android.R.attr.textColorPrimary));

        vh.itemView.setOnClickListener(v -> onTrackClick(vh.itemView, vh.getAdapterPosition()));
        vh.btnMenu.setOnClickListener(v -> onMenuClick(v, vh.getAdapterPosition()));
    }
}
