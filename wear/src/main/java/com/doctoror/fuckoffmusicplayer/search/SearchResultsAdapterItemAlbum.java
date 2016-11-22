package com.doctoror.fuckoffmusicplayer.search;

import com.doctoror.commons.wear.nano.WearSearchData;

import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

final class SearchResultsAdapterItemAlbum extends SearchResultsAdapterItem {

    @NonNull
    private final WearSearchData.Album mAlbum;

    public SearchResultsAdapterItemAlbum(@NonNull final WearSearchData.Album album) {
        super(album.title);
        mAlbum = album;
    }

    @NonNull
    public WearSearchData.Album getAlbum() {
        return mAlbum;
    }
}
