package com.doctoror.fuckoffmusicplayer.db.genres;

import android.database.Cursor;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.Single;

/**
 * "Genres" provider
 */
public interface GenresProvider {

    int COLUMN_ID = 0;
    int COLUMN_NAME = 1;

    Observable<Cursor> load(@Nullable String searchFilter);
    Single<Cursor> loadOnce();
}
