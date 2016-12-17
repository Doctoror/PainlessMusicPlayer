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
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * "Playlist" recycler adapter
 */
final class PlaylistRecyclerAdapter extends BaseRecyclerAdapter<Object, RecyclerView.ViewHolder> {

    private static final int ITEM_MEDIA = 0;
    private static final int ITEM_MEDIA_REMOVED = 1;

    interface OnTrackClickListener {

        void onTrackClick(@NonNull View itemView, @NonNull Media media, int position);

        void onTrackRemoved(int position, @NonNull Media media);

        void onTracksSwapped(int i, int j);
    }

    private OnTrackClickListener mOnTrackClickListener;

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

    void setItemRemoved(final int position) {
        final Object item = getItem(position);
        if (item instanceof Media) {
            final Media media = (Media) item;
            // Replace with "swiped out" state
            final RemovedMedia removedMedia = new RemovedMedia(media);
            setItem(position, removedMedia);
            // After timeout, removed the "swiped out" item completely
            Observable.timer(4, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> removeItem(removedMedia));
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

    boolean canRemove(final int position) {
        return getItem(position) instanceof Media;
    }

    boolean canDrag(final int position) {
        return !containsRemovedItems() && getItem(position) instanceof Media;
    }

    private boolean containsRemovedItems() {
        final int count = getItemCount();
        for (int i = 0; i < count; i++) {
            if (getItem(i) instanceof RemovedMedia) {
                return true;
            }
        }
        return false;
    }

    void setOnTrackClickListener(@NonNull final OnTrackClickListener onTrackClickListener) {
        mOnTrackClickListener = onTrackClickListener;
    }

    private void onTrackClick(@NonNull final View itemView, @NonNull final Media media,
            final int position) {
        if (mOnTrackClickListener != null) {
            mOnTrackClickListener.onTrackClick(itemView, media, position);
        }
    }

    private void onTrackRemoved(final int position, @NonNull final Media media) {
        if (mOnTrackClickListener != null) {
            mOnTrackClickListener.onTrackRemoved(position, media);
        }
    }

    private void onTracksSwapped(final int i, final int j) {
        if (mOnTrackClickListener != null) {
            mOnTrackClickListener.onTracksSwapped(i, j);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position) instanceof Media ? ITEM_MEDIA : ITEM_MEDIA_REMOVED;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_MEDIA:
                onBindViewHolderMedia((PlaylistItemViewHolder) viewHolder, position);
                break;

            case ITEM_MEDIA_REMOVED:
                onBindViewHolderMediaRemoved((ViewHolderMediaRemoved) viewHolder, position);
                break;

            default:
                throw new IllegalStateException("Unexpected view type: " + viewType);
        }
    }

    private void onBindViewHolderMedia(final PlaylistItemViewHolder viewHolder, final int position) {
        final Media item = (Media) getItem(position);
        viewHolder.textTitle.setText(item.title);
        viewHolder.textArtist.setText(item.artist);
        BindingAdapters.setFormattedDuration(viewHolder.textDuration, item.duration / 1000L);
    }

    private void onBindViewHolderMediaRemoved(final ViewHolderMediaRemoved viewHolder,
            final int position) {
        final RemovedMedia item = (RemovedMedia) getItem(position);
        viewHolder.text.setText(viewHolder.text.getResources().getString(
                R.string.s_removed_from_playlist, item.media.title));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case ITEM_MEDIA:
                return onCreateViewHolderMedia(parent);

            case ITEM_MEDIA_REMOVED:
                return onCreateViewHolderMediaRemoved(parent);

            default:
                throw new IllegalStateException("Unexpected view type: " + viewType);
        }
    }

    @NonNull
    private RecyclerView.ViewHolder onCreateViewHolderMedia(final ViewGroup parent) {
        final PlaylistItemViewHolder vh = new PlaylistItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_media, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final int position = vh.getAdapterPosition();
            final Media item = (Media) getItem(position);
            if (item != null) {
                onTrackClick(vh.itemView, item, position);
            }
        });
        return vh;
    }

    @NonNull
    private RecyclerView.ViewHolder onCreateViewHolderMediaRemoved(final ViewGroup parent) {
        final ViewHolderMediaRemoved vh = new ViewHolderMediaRemoved(
                getLayoutInflater().inflate(R.layout.item_playlist_dismissed, parent, false));
        vh.btnDelete.setOnClickListener(v -> {
            final int position = vh.getAdapterPosition();
            final RemovedMedia item = (RemovedMedia) getItem(position);
            onTrackRemoved(position, item.media);
            // Remove item that is about to be deleted
            removeItem(position);
        });
        return vh;
    }

    private static final class ViewHolderMediaRemoved extends RecyclerView.ViewHolder {

        final TextView text;
        final View btnDelete;

        ViewHolderMediaRemoved(@NonNull final View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(android.R.id.text1);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private static final class RemovedMedia {

        @NonNull
        final Media media;

        RemovedMedia(@NonNull final Media media) {
            this.media = media;
        }
    }
}
