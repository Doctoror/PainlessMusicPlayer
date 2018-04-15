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
package com.doctoror.fuckoffmusicplayer.presentation.navigation;

import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;

public enum NavigationItem {

    RECENT_ACTIVITY(R.id.navigationRecentActivity, R.string.Recent_Activity),
    PLAYLISTS(R.id.navigationPlaylists, R.string.Playlists),
    ARTISTS(R.id.navigationArtists, R.string.Artists),
    ALBUMS(R.id.navigationAlbums, R.string.Albums),
    GENRES(R.id.navigationGenres, R.string.Genres),
    TRACKS(R.id.navigationTracks, R.string.Tracks),
    SETTINGS(R.id.navigationSettings, R.string.Settings);

    @Nullable
    public static NavigationItem fromId(@IdRes final int id) {
        for (final NavigationItem item : values()) {
            if (item.id == id) {
                return item;
            }
        }
        Log.w("NavigationItem", "Cannot resolve NavigationItem for id = " + id);
        return null;
    }

    @IdRes
    public final int id;

    @StringRes
    public final int title;

    NavigationItem(
            @IdRes final int id,
            @StringRes final int title) {
        this.id = id;
        this.title = title;
    }
}
