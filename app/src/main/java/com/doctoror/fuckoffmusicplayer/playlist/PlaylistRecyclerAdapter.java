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
import com.doctoror.commons.view.BaseRecyclerAdapter;
import com.doctoror.commons.view.TwoLineItemViewHolder;

import android.content.Context;
import android.support.annotation.NonNull;
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
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
final class PlaylistRecyclerAdapter extends BaseRecyclerAdapter<Object, RecyclerView.ViewHolder> {

    private static final int ITEM_MEDIA = 0;
    private static final int ITEM_MEDIA_REMOVED = 1;

    interface OnTrackClickListener {

        void onTrackClick(@NonNull Media media, int position);

        void onTrackDeleteClick(@NonNull Media media);
    }

    private OnTrackClickListener mOnTrackClickListener;

    PlaylistRecyclerAdapter(@NonNull final Context context,
            @NonNull final List<Media> items) {
        super(context, toObjectList(items), true);
    }

    @NonNull
    private static List<Object> toObjectList(@NonNull final List<Media> items) {
        return new ArrayList<>(items);
    }

    void setItemRemoved(final int position) {
        final Object item = getItem(position);
        if (item instanceof Media) {
            final Media media = (Media) item;
            final long id = media.getId();
            // Replace with "swiped out" state
            setItem(position, new RemovedMedia(media));
            // After timeout, removed the "swiped out" item completely
            Observable.timer(4, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(aLong -> {
                        if (position < getItemCount()) {
                            final Object itemNow = getItem(position);
                            if (itemNow instanceof RemovedMedia) {
                                // Is the same item we waited for
                                if (((RemovedMedia) itemNow).media.id == id) {
                                    removeItem(position);
                                }
                            }
                        }
                    });
        }
    }

    boolean canRemove(final int position) {
        return getItem(position) instanceof Media;
    }

    void setOnTrackClickListener(@NonNull final OnTrackClickListener onTrackClickListener) {
        mOnTrackClickListener = onTrackClickListener;
    }

    private void onTrackClick(@NonNull final Media media, final int position) {
        if (mOnTrackClickListener != null) {
            mOnTrackClickListener.onTrackClick(media, position);
        }
    }

    private void onTrackDeleteClick(@NonNull final Media media) {
        if (mOnTrackClickListener != null) {
            mOnTrackClickListener.onTrackDeleteClick(media);
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
                onBindViewHolderMedia((TwoLineItemViewHolder) viewHolder, position);
                break;

            case ITEM_MEDIA_REMOVED:
                onBindViewHolderMediaRemoved((ViewHolderMediaRemoved) viewHolder, position);
                break;

            default:
                throw new IllegalStateException("Unexpected view type: " + viewType);
        }
    }

    private void onBindViewHolderMedia(final TwoLineItemViewHolder viewHolder, final int position) {
        final Media item = (Media) getItem(position);
        viewHolder.text1.setText(item.title);
        viewHolder.text2.setText(item.artist);
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
        final TwoLineItemViewHolder vh = new TwoLineItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_two_line, parent, false));
        vh.itemView.setOnClickListener(v -> {
            final int position = vh.getAdapterPosition();
            final Media item = (Media) getItem(position);
            if (item != null) {
                onTrackClick(item, position);
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
            onTrackDeleteClick(item.media);
            // Remove item that is about to be deleted
            removeItem(position);
        });
        return vh;
    }

    private static final class ViewHolderMediaRemoved extends RecyclerView.ViewHolder {

        final TextView text;
        final View btnDelete;

        ViewHolderMediaRemoved(final View itemView) {
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
