package com.doctoror.fuckoffmusicplayer.eventbus;

import com.doctoror.commons.wear.nano.WearSearchData;

import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 28.11.16.
 */

public final class EventSearchResults {

    @Nullable
    public final WearSearchData.Results results;

    public EventSearchResults(@Nullable final WearSearchData.Results results) {
        this.results = results;
    }
}
