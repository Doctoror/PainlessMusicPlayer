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
package com.doctoror.fuckoffmusicplayer.library.genres;

import com.doctoror.rxcursorloader.RxCursorLoader;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */

public final class GenresQuery {

    private GenresQuery() {
        throw new UnsupportedOperationException();
    }

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NAME = 1;

    @NonNull
    public static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Genres._ID,
                        MediaStore.Audio.Genres.NAME
                })
                .setSortOrder(MediaStore.Audio.Genres.NAME)
                .setSelection(TextUtils.isEmpty(searchFilter) ? null
                        : MediaStore.Audio.Genres.NAME + " LIKE '%"
                                + StringUtils.sqlEscape(searchFilter) + "%'")
                .create();
    }

}
