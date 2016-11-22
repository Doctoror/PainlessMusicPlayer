package com.doctoror.fuckoffmusicplayer.search;

import com.doctoror.commons.wear.nano.WearSearchData;

import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

final class SearchResultsAdapterItemTrack extends SearchResultsAdapterItem {

    @NonNull
    private final WearSearchData.Track mTrack;

    public SearchResultsAdapterItemTrack(@NonNull final WearSearchData.Track album) {
        super(album.title);
        mTrack = album;
    }

    @NonNull
    public WearSearchData.Track getTrack() {
        return mTrack;
    }
}