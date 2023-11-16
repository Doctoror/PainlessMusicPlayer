/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.domain.albums

import android.database.Cursor

import io.reactivex.Observable
import io.reactivex.Scheduler

const val COLUMN_ID = 0
const val COLUMN_ALBUM = 1
const val COLUMN_FIRST_YEAR = 2
const val COLUMN_ALBUM_ART = 3

interface AlbumsProvider {

    fun load(searchFilter: String?, scheduler: Scheduler): Observable<Cursor>

    fun loadForArtist(artistId: Long): Observable<Cursor>
    fun loadForGenre(genreId: Long): Observable<Cursor>

    fun loadRecentlyPlayedAlbums(): Observable<Cursor>
    fun loadRecentlyPlayedAlbums(limit: Int?): Observable<Cursor>

    fun loadRecentlyScannedAlbums(limit: Int?): Observable<Cursor>
}
