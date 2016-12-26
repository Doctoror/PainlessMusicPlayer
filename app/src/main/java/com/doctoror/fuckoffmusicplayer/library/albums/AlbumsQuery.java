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
package com.doctoror.fuckoffmusicplayer.library.albums;

import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class AlbumsQuery {

    private AlbumsQuery() {
        throw new UnsupportedOperationException();
    }

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_ALBUM = 1;
    public static final int COLUMN_ALBUM_ART = 2;

    /**
     * Constructs params for albums search.
     *
     * @param searchFilter the user input filter string. May be null if no filtering needed
     * @return params
     */
    @NonNull
    public static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        return newParamsBuilder().setSelection(TextUtils.isEmpty(searchFilter) ? null :
                MediaStore.Audio.Albums.ALBUM + " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(
                        searchFilter)).create();
    }

    /**
     * Constructs params Builder for albums search.
     *
     * @return params Builder
     */
    @NonNull
    public static RxCursorLoader.Query.Builder newParamsBuilder() {
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Albums.ALBUM_ART
                })
                .setSortOrder(MediaStore.Audio.Albums.ALBUM);
    }

}
