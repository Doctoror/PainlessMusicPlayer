package com.doctoror.fuckoffmusicplayer.library.playlists;

import com.doctoror.fuckoffmusicplayer.R;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * "Live Playlist" ViewHolder
 */
final class LivePlaylistViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.icon) public ImageView icon;
    @BindView(R.id.text) public TextView text;
    @BindView(R.id.progress) public View progress;

    LivePlaylistViewHolder(@NonNull final View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
