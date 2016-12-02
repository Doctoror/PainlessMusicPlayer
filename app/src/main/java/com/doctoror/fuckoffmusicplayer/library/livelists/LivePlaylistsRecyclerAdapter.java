package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.util.ThemeUtils;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 08.11.16.
 */
final class LivePlaylistsRecyclerAdapter extends BaseRecyclerAdapter<LivePlaylist,
        LivePlaylistViewHolder> {

    interface OnPlaylistClickListener {
        void onPlaylistClick(int position, @NonNull LivePlaylist livePlaylist);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @Nullable
    private final Drawable mIcon;

    private int mLoadingPosition = -1;

    private OnPlaylistClickListener mOnPlaylistClickListener;

    LivePlaylistsRecyclerAdapter(@NonNull final Context context,
            @Nullable final List<LivePlaylist> items) {
        super(context, items, true);
        mLayoutInflater = LayoutInflater.from(context);
        mIcon = DrawableUtils.getTintedDrawable(context, R.drawable.ic_queue_music_black_40dp,
                ThemeUtils.getColorStateList(context.getTheme(), android.R.attr.textColorPrimary));
    }

    void setOnPlaylistClickListener(
            @Nullable final OnPlaylistClickListener onPlaylistClickListener) {
        mOnPlaylistClickListener = onPlaylistClickListener;
    }

    void clearLoadingFlag() {
        if (mLoadingPosition != -1) {
            final int prevValue = mLoadingPosition;
            mLoadingPosition = -1;
            notifyItemChanged(prevValue);
        }
    }

    private void onPlaylistClick(final int position, @NonNull final LivePlaylist livePlaylist) {
        if (mOnPlaylistClickListener != null) {
            mLoadingPosition = position;
            notifyItemChanged(position);
            mOnPlaylistClickListener.onPlaylistClick(position, livePlaylist);
        }
    }

    @Override
    public LivePlaylistViewHolder onCreateViewHolder(final ViewGroup parent,
            final int viewType) {
        final LivePlaylistViewHolder vh = new LivePlaylistViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_live_playlist, parent, false));
        vh.icon.setImageDrawable(mIcon);
        vh.itemView.setOnClickListener(v -> {
            final int position = vh.getAdapterPosition();
            onPlaylistClick(position, getItem(position));
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(final LivePlaylistViewHolder holder,
            final int position) {
        holder.text.setText(getItem(position).getTitle());
        holder.progress.setVisibility(mLoadingPosition == position ? View.VISIBLE : View.GONE);
    }
}
