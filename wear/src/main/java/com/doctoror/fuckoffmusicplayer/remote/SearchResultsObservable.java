package com.doctoror.fuckoffmusicplayer.remote;

import com.doctoror.commons.wear.nano.WearSearchData;

import android.support.annotation.NonNull;

import java.util.Observable;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

public final class SearchResultsObservable extends Observable {

    private static final SearchResultsObservable INSTANCE = new SearchResultsObservable();

    @NonNull
    public static SearchResultsObservable getInstance() {
        return INSTANCE;
    }

    private SearchResultsObservable() {

    }

    public void onSearchResultsReceived(@NonNull final WearSearchData.Results results) {
        setChanged();
        notifyObservers(results);
    }
}
