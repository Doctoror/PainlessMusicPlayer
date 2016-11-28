/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.search;

import com.doctoror.commons.wear.nano.WearSearchData;

import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 22.11.16.
 */

final class SearchResultsAdapterItemAlbum extends SearchResultsAdapterItem {

    @NonNull
    private final WearSearchData.Album mAlbum;

    SearchResultsAdapterItemAlbum(@NonNull final WearSearchData.Album album) {
        super(album.title);
        mAlbum = album;
    }

    @NonNull
    WearSearchData.Album getAlbum() {
        return mAlbum;
    }
}
