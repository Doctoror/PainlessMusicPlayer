/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    Single<Cursor> loadRecentlyPlayedAlbumsOnce(@Nullable Integer limit);

    Single<Cursor> loadRecentlyScannedAlbumsOnce(int limit);

}
