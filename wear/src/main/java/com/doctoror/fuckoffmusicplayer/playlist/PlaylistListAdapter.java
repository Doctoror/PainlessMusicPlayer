package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.commons.wear.nano.ProtoPlaybackData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.base.BaseRecyclerAdapter;
import com.doctoror.fuckoffmusicplayer.base.TwoLineItemViewHolder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

final class PlaylistListAdapter extends BaseRecyclerAdapter
        <ProtoPlaybackData.Media, TwoLineItemViewHolder> {

    PlaylistListAdapter(@NonNull final Context context) {
        super(context);
    }

    private void onTrackClick(final int position) {
        // TODO
    }

    @Override
    public TwoLineItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final TwoLineItemViewHolder vh = new TwoLineItemViewHolder(
                getLayoutInflater().inflate(R.layout.list_item_two_line, parent, false));
        vh.itemView.setOnClickListener(v -> onTrackClick(vh.getAdapterPosition()));
        return vh;
    }

    @Override
    public void onBindViewHolder(final TwoLineItemViewHolder holder, final int position) {
        final ProtoPlaybackData.Media item = getItem(position);
        holder.text1.setText(item.title);
        holder.text2.setText(item.artist);
    }
}
