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
    int COLUMN_FIRST_YEAR = 3;

    Observable<Cursor> load(@Nullable String searchFilter);
    Observable<Cursor> load(@Nullable String searchFilter, @Nullable Integer limit);

    Observable<Cursor> loadForArtist(long artistId);
    Observable<Cursor> loadForGenre(long genreId);
    Observable<Cursor> loadRecentlyPlayedAlbums();

    Single<Cursor> loadRecentlyPlayedAlbumsOnce();

}
