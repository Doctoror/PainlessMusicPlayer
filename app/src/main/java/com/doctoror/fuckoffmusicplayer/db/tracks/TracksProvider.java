package com.doctoror.fuckoffmusicplayer.db.tracks;

import android.database.Cursor;
import android.support.annotation.Nullable;

import rx.Observable;

/**
 * "Tracks" provider
 */
public interface TracksProvider {

    int COLUMN_ID = 0;
    int COLUMN_TITLE = 1;
    int COLUMN_ARTIST = 2;

    Observable<Cursor> load(@Nullable String searchFilter);

    Observable<Cursor> load(
            @Nullable String searchFilter,
            @Nullable Integer limit,
            boolean includeSearchByArtist);
}
