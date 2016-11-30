package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Yaroslav Mytkalyk on 30.11.16.
 */

final class LivePlaylistViewHolder extends RecyclerView.ViewHolder {

    public final ImageView icon;
    public final TextView text;
    public final View progress;

    public LivePlaylistViewHolder(@NonNull final View itemView) {
        super(itemView);
        icon = (ImageView) itemView.findViewById(R.id.icon);
        text = (TextView) itemView.findViewById(R.id.text);
        progress = itemView.findViewById(R.id.progress);
    }
}
