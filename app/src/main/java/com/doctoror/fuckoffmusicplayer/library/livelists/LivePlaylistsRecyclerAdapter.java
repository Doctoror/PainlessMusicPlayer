package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.util.DrawableUtils;
import com.doctoror.fuckoffmusicplayer.util.ThemeUtils;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;
import com.doctoror.fuckoffmusicplayer.widget.SingleLineWithIconItemViewHolder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 08.11.16.
 */
final class LivePlaylistsRecyclerAdapter extends BaseRecyclerAdapter<LivePlaylist,
        SingleLineWithIconItemViewHolder> {

    interface OnPlaylistClickListener {
        void onPlaylistClick(@NonNull LivePlaylist livePlaylist);
    }

    @NonNull
    private final LayoutInflater mLayoutInflater;

    @Nullable
    private final Drawable mIcon;

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

    private void onPlaylistClick(@NonNull final LivePlaylist livePlaylist) {
        if (mOnPlaylistClickListener != null) {
            mOnPlaylistClickListener.onPlaylistClick(livePlaylist);
        }
    }

    @Override
    public SingleLineWithIconItemViewHolder onCreateViewHolder(final ViewGroup parent,
            final int viewType) {
        final SingleLineWithIconItemViewHolder vh = new SingleLineWithIconItemViewHolder(
                mLayoutInflater.inflate(R.layout.list_item_single_line_icon, parent, false));
        vh.icon.setImageDrawable(mIcon);
        vh.itemView.setOnClickListener(v -> onPlaylistClick(getItem(vh.getAdapterPosition())));
        return vh;
    }

    @Override
    public void onBindViewHolder(final SingleLineWithIconItemViewHolder holder,
            final int position) {
        holder.text.setText(getItem(position).getTitle());
    }
}
