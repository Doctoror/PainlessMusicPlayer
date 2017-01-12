package com.doctoror.fuckoffmusicplayer.home;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.widget.viewholder.AlbumViewHolder;
import com.doctoror.fuckoffmusicplayer.widget.BaseRecyclerAdapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Yaroslav Mytkalyk on 11.01.17.
 */
final class RecentActivityRecyclerAdapter
        extends BaseRecyclerAdapter<Object, RecyclerView.ViewHolder> {

    static final int VIEW_TYPE_HEADER = 0;
    static final int VIEW_TYPE_ALBUM = 1;

    interface OnAlbumClickListener {

        void onAlbumClick(View albumArtView, long id, String album);
    }

    private final RequestManager mGlide;
    private OnAlbumClickListener mOnAlbumClickListener;

    RecentActivityRecyclerAdapter(@NonNull final Context context) {
        super(context);
        mGlide = Glide.with(context);
    }

    void setOnAlbumClickListener(@NonNull final OnAlbumClickListener l) {
        mOnAlbumClickListener = l;
    }

    private void onAlbumClick(@NonNull final View view, final long id,
            @NonNull final String album) {
        if (mOnAlbumClickListener != null) {
            mOnAlbumClickListener.onAlbumClick(view, id, album);
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

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
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
        vh.itemView.setOnClickListener(v -> {
            final Object item = getItem(vh.getAdapterPosition());
            if (item instanceof AlbumItem) {
                onAlbumClick(vh.image,
                        ((AlbumItem) item).id,
                        ((AlbumItem) item).title);
            }
        });

        return vh;
    }

    @NonNull
    private HeaderViewHolder onCreateViewHolderHeader(final ViewGroup parent) {
        return new HeaderViewHolder(
                getLayoutInflater().inflate(R.layout.recycler_item_header, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
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
            Glide.clear(holder.image);
            holder.image.setImageResource(R.drawable.album_art_placeholder);
        } else {
            mGlide.load(artLocation)
                    .placeholder(R.drawable.album_art_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontTransform()
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
