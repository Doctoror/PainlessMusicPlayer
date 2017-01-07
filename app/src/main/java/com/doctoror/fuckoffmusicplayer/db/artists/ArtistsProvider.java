package com.doctoror.fuckoffmusicplayer.db.artists;

import android.database.Cursor;
import android.support.annotation.Nullable;

import rx.Observable;

/**
 * "Artists" provider
 */
public interface ArtistsProvider {

    int COLUMN_ID = 0;
    int COLUMN_NUMBER_OF_ALBUMS = 1;
    int COLUMN_ARTIST = 2;

    Observable<Cursor> load(@Nullable String searchFilter);
    Observable<Cursor> load(@Nullable String searchFilter, @Nullable Integer limit);
}
