package com.doctoror.fuckoffmusicplayer.search;

import com.doctoror.commons.wear.nano.WearSearchData;

import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

final class SearchResultsAdapterItemArtist extends SearchResultsAdapterItem {

    @NonNull
    private final WearSearchData.Artist mArtist;

    public SearchResultsAdapterItemArtist(@NonNull final WearSearchData.Artist data) {
        super(data.title);
        mArtist = data;
    }

    @NonNull
    public WearSearchData.Artist getArtist() {
        return mArtist;
    }
}