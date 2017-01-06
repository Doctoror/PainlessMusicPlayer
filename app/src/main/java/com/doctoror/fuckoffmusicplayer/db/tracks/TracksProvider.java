package com.doctoror.fuckoffmusicplayer.db.tracks;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import rx.Observable;

/**
 * "Tracks" provider
 */
public interface TracksProvider {

    // Avoids non-music and hidden files
    String SELECTION_NON_HIDDEN_MUSIC = MediaStore.Audio.Media.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE '%/.%'";

    int COLUMN_ID = 0;
    int COLUMN_TITLE = 1;
    int COLUMN_ARTIST = 2;

    String SORT_ORDER = MediaStore.Audio.Media.TITLE;

    Observable<Cursor> load(@Nullable String searchFilter);
}
