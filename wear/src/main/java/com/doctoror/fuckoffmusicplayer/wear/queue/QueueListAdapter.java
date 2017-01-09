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
package com.doctoror.fuckoffmusicplayer.wear.queue;

import com.doctoror.commons.wear.nano.WearPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.wear.view.BaseRecyclerAdapter;
import com.doctoror.fuckoffmusicplayer.wear.view.TwoLineItemViewHolder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

/**
 * Recycler view adapter for playlsit
 */
final class QueueListAdapter extends BaseRecyclerAdapter
        <WearPlaybackData.Media, TwoLineItemViewHolder> {

    interface OnMediaClickListener {
        void onMediaClick(long id);
    }

    private OnMediaClickListener mListener;

    QueueListAdapter(@NonNull final Context context) {
        super(context);
    }

    void setOnMediaClickListener(@Nullable final OnMediaClickListener listener) {
        mListener = listener;
    }

    private void onMediaClick(final long mediaId) {
        if (mListener != null) {
            mListener.onMediaClick(mediaId);
        }
    }

    @Override
    public TwoLineItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final TwoLineItemViewHolder vh = new TwoLineItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_two_line, parent, false));
        vh.itemView.setOnClickListener(v -> onMediaClick(getItem(vh.getAdapterPosition()).id));
        return vh;
    }

    @Override
    public void onBindViewHolder(final TwoLineItemViewHolder holder, final int position) {
        final WearPlaybackData.Media item = getItem(position);
        holder.text1.setText(item.title);
        holder.text2.setText(item.artist);
    }
}
