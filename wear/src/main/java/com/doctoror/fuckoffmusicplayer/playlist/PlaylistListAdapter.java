package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.commons.view.BaseRecyclerAdapter;
import com.doctoror.commons.view.TwoLineItemViewHolder;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

final class PlaylistListAdapter extends BaseRecyclerAdapter
        <ProtoPlaybackData.Media, TwoLineItemViewHolder> {

    public PlaylistListAdapter(@NonNull final Context context) {
        super(context);
    }

    @Override
    public TwoLineItemViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(final TwoLineItemViewHolder holder, final int position) {

    }
}
