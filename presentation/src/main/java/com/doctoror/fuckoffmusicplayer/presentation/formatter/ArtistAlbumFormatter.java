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
package com.doctoror.fuckoffmusicplayer.presentation.formatter;

import android.content.res.Resources;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.R;

public final class ArtistAlbumFormatter {

    @NonNull
    public String formatArtistAndAlbum(@NonNull final Resources res,
            @Nullable String artist,
            @Nullable String album) {
        if (TextUtils.isEmpty(artist)) {
            artist = res.getString(R.string.Unknown_artist);
        }
        if (TextUtils.isEmpty(album)) {
            album = res.getString(R.string.Unknown_album);
        }
        return artist + res.getString(R.string.artist_album_separator) + album;
    }
}


