package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;
import com.doctoror.fuckoffmusicplayer.widget.viewholder.AlbumViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

final class RecentActivityRecyclerAdapter
        extends BaseRecyclerAdapter<Object, RecyclerView.ViewHolder> {

    static final int VIEW_TYPE_HEADER = 0;
    static final int VIEW_TYPE_ALBUM = 1;

    interface OnAlbumClickListener {

        void onAlbumClick(int position, long id, @Nullable String album);
    }

    private final RequestOptions requestOptions = new RequestOptions()
            .error(R.drawable.album_art_placeholder)
            .diskCacheStrategy(DiskCacheStrategy.NONE);

    private final RequestManager requestManager;
    private OnAlbumClickListener onAlbumClickListener;

    RecentActivityRecyclerAdapter(@NonNull final Context context) {
        super(context);
        requestManager = Glide.with(context);
    }

    void setOnAlbumClickListener(@NonNull final OnAlbumClickListener l) {
        onAlbumClickListener = l;
    }

    private void onItemClick(final int position) {
        final Object item = getItem(position);
        if (item instanceof AlbumItem) {
            onAlbumClick(position,
                    ((AlbumItem) item).id,
                    ((AlbumItem) item).title);
        }

    }

    private void onAlbumClick(final int position, final long id,
                              @Nullable final String album) {
        if (onAlbumClickListener != null) {
            onAlbumClickListener.onAlbumClick(position, id, album);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        final Object item = getItem(position);
        if (item instanceof RecentActivityHeader) {
            return VIEW_TYPE_HEADER;
        }
        if (item instanceof AlbumItem) {
            return VIEW_TYPE_ALBUM;
        }
        throw new IllegalArgumentException("Unexpected item " + item + " for position " + position);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                return onCreateViewHolderHeader(parent);

            case VIEW_TYPE_ALBUM:
                return onCreateViewHolderAlbum(parent);

            default:
                throw new IllegalArgumentException("Unexpected viewType: " + viewType);
        }
    }

    @NonNull
    private AlbumViewHolder onCreateViewHolderAlbum(final ViewGroup parent) {
        final AlbumViewHolder vh = new AlbumViewHolder(
                getLayoutInflater().inflate(R.layout.recycler_item_album, parent, false));
        vh.itemView.setOnClickListener(v -> onItemClick(vh.getAdapterPosition()));
        return vh;
    }

    @NonNull
    private HeaderViewHolder onCreateViewHolderHeader(final ViewGroup parent) {
        return new HeaderViewHolder(
                getLayoutInflater().inflate(R.layout.recycler_item_header, parent, false));
    }

    @Override
    public void onBindViewHolder(
            @NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_HEADER:
                onBindViewHolderHeader((HeaderViewHolder) holder, position);
                break;

            case VIEW_TYPE_ALBUM:
                onBindViewHolderAlbum((AlbumViewHolder) holder, position);
                break;
        }
    }

    private void onBindViewHolderHeader(final HeaderViewHolder holder, final int position) {
        final RecentActivityHeader header = (RecentActivityHeader) getItem(position);
        holder.title.setText(header.getTitle());
    }

    private void onBindViewHolderAlbum(final AlbumViewHolder holder, final int position) {
        final AlbumItem item = (AlbumItem) getItem(position);
        holder.text1.setText(item.title);
        final String artLocation = item.albumArt;
        if (TextUtils.isEmpty(artLocation)) {
            requestManager.clear(holder.image);
            holder.image.setImageResource(R.drawable.album_art_placeholder);
        } else {
            requestManager.load(artLocation)
                    .apply(requestOptions)
                    .into(holder.image);
        }
    }

    static final class HeaderViewHolder extends RecyclerView.ViewHolder {

        @BindView(android.R.id.text1)
        TextView title;

        HeaderViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
