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
package com.doctoror.fuckoffmusicplayer.presentation.library.playlists;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A "Live Playlist"
 */
final class LivePlaylist {

    static final int TYPE_RECENTLY_PLAYED_ALBUMS = -1;
    static final int TYPE_RECENTLY_SCANNED = -2;
    static final int TYPE_RANDOM_PLAYLIST = -3;

    @IntDef({
            TYPE_RECENTLY_PLAYED_ALBUMS,
            TYPE_RECENTLY_SCANNED,
            TYPE_RANDOM_PLAYLIST
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface Type {

    }

    @Type
    private final int mType;

    @NonNull
    private final CharSequence mTitle;

    LivePlaylist(@Type final int type, @NonNull final CharSequence title) {
        mType = type;
        mTitle = title;
    }

    @Type
    public int getType() {
        return mType;
    }

    @NonNull
    public CharSequence getTitle() {
        return mTitle;
    }
}
