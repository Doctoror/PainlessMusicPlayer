package com.doctoror.fuckoffmusicplayer.db.albums;

import android.database.Cursor;
import android.support.annotation.Nullable;

import rx.Observable;
import rx.Single;

/**
 * "Albums" provider
 */
public interface AlbumsProvider {

    int COLUMN_ID = 0;
    int COLUMN_ALBUM = 1;
    int COLUMN_ALBUM_ART = 2;

    Observable<Cursor> load(@Nullable String searchFilter);

    Single<Cursor> loadRecentlyPlayedAlbumsOnce();

}
