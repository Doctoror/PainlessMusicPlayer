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
package com.doctoror.fuckoffmusicplayer.library.tracks;

import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Used for querying tracks
 */
public final class TracksQuery {

    private TracksQuery() {
        throw new UnsupportedOperationException();
    }

    // Avoids non-music and hidden files
    public static final String SELECTION_NON_HIDDEN_MUSIC = MediaStore.Audio.Media.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE '%/.%'";

    static final int COLUMN_ID = 0;
    static final int COLUMN_TITLE = 1;
    static final int COLUMN_ARTIST = 2;

    static final String SORT_ORDER = MediaStore.Audio.Media.TITLE;

    @NonNull
    static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        final String wrapped = TextUtils.isEmpty(searchFilter) ? null
                : SqlUtils.escapeAndWrapForLikeArgument(searchFilter);
        final StringBuilder selection = new StringBuilder(256);
        selection.append(TracksQuery.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(wrapped)) {
            selection.append(" AND ")

                    .append(MediaStore.Audio.Media.TITLE)
                    .append(" LIKE ").append(wrapped).append(" OR ")

                    .append(MediaStore.Audio.Media.ARTIST)
                    .append(" LIKE ").append(wrapped);
        }
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST
                })
                .setSortOrder(SORT_ORDER)
                .setSelection(selection.toString())
                .create();
    }

}
