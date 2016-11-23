package com.doctoror.fuckoffmusicplayer.search;

import com.doctoror.commons.wear.nano.WearSearchData;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.base.BaseRecyclerAdapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

final class SearchResultsAdapter
        extends BaseRecyclerAdapter<SearchResultsAdapterItem, SearchResultsViewHolder> {

    private static final int VIEW_TYPE_ALBUM = 0;
    private static final int VIEW_TYPE_ARTIST = 1;
    private static final int VIEW_TYPE_TRACK = 2;

    private final Drawable mIconAlbum;
    private final Drawable mIconArtist;
    private final Drawable mIconTrack;

    private WearSearchData.Results mResults;

    SearchResultsAdapter(@NonNull final Context context,
            @Nullable final WearSearchData.Results results) {
        super(context);

        mIconAlbum = context.getDrawable(R.drawable.list_item_icon_album_40dp);
        mIconArtist = context.getDrawable(R.drawable.list_item_icon_artist_40dp);
        mIconTrack = context.getDrawable(R.drawable.list_item_icon_track_40dp);

        setResults(results);
    }

    void setResults(@Nullable final WearSearchData.Results results) {
        if (mResults != results) {
            mResults = results;
            flattenResults(results);
        }
    }

    @MainThread
    @SuppressWarnings("ForLoopReplaceableByForEach") // Faster with manual for loop
    private void flattenResults(@Nullable final WearSearchData.Results results) {
        final List<SearchResultsAdapterItem> items = getMutableItems();
        items.clear();
        if (results != null) {
            final WearSearchData.Album[] albums = results.albums;
            if (albums != null) {
                for (int i = 0; i < albums.length; i++) {
                    items.add(new SearchResultsAdapterItemAlbum(albums[i]));
                }
            }

            final WearSearchData.Artist[] artists = results.artists;
            if (artists != null) {
                for (int i = 0; i < artists.length; i++) {
                    items.add(new SearchResultsAdapterItemArtist(artists[i]));
                }
            }

            final WearSearchData.Track[] tracks = results.tracks;
            if (tracks != null) {
                for (int i = 0; i < tracks.length; i++) {
                    items.add(new SearchResultsAdapterItemTrack(tracks[i]));
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(final int position) {
        final SearchResultsAdapterItem item = getItem(position);
        if (SearchResultsAdapterItemAlbum.class.equals(item.getClass())) {
            return VIEW_TYPE_ALBUM;
        }
        if (SearchResultsAdapterItemArtist.class.equals(item.getClass())) {
            return VIEW_TYPE_ARTIST;
        }
        if (SearchResultsAdapterItemTrack.class.equals(item.getClass())) {
            return VIEW_TYPE_TRACK;
        }
        throw new IllegalArgumentException("Unexpected item class: " + item.getClass());
    }

    @Override
    public SearchResultsViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new SearchResultsViewHolder(getLayoutInflater()
                .inflate(R.layout.list_item_search_result, parent, false));
    }

    @Override
    public void onBindViewHolder(final SearchResultsViewHolder holder, final int position) {
        holder.textView.setText(getItem(position).getTitle());
        final Drawable drawableStart = drawableStart(getItemViewType(position));
        if (holder.textView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, drawableStart, null);
        } else {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(
                    drawableStart, null, null, null);
        }
    }

    @Nullable
    private Drawable drawableStart(final int viewType) {
        switch (viewType) {
            case VIEW_TYPE_ALBUM:
                return mIconAlbum;

            case VIEW_TYPE_ARTIST:
                return mIconArtist;

            case VIEW_TYPE_TRACK:
                return mIconTrack;

            default:
                return null;
        }
    }
}
