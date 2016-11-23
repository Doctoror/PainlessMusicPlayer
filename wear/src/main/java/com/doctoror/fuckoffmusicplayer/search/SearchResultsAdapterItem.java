package com.doctoror.fuckoffmusicplayer.search;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

abstract class SearchResultsAdapterItem {

    private CharSequence mTitle;

    SearchResultsAdapterItem(final CharSequence title) {
        mTitle = title;
    }

    public CharSequence getTitle() {
        return mTitle;
    }

}
