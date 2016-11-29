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

import com.doctoror.rxcursorloader.RxCursorLoader;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Used for querying tracks
 */
final class TracksQuery {

    private TracksQuery() {
        throw new UnsupportedOperationException();
    }

    static final int COLUMN_ID = 0;
    static final int COLUMN_TITLE = 1;
    static final int COLUMN_ARTIST = 2;

    static final String SORT_ORDER = MediaStore.Audio.Media.TITLE;

    @NonNull
    static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        final String ef = TextUtils.isEmpty(searchFilter) ? null
                : StringUtils.sqlEscape(searchFilter);
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST
                })
                .setSortOrder(SORT_ORDER)
                .setSelection(TextUtils.isEmpty(ef) ? null
                        : MediaStore.Audio.Media.TITLE + " LIKE '%" + ef + "%'" + " OR "
                                + MediaStore.Audio.Media.ARTIST + " LIKE '%" + ef + "%'")
                .create();
    }

}
