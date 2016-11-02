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
package com.doctoror.fuckoffmusicplayer.library.albums.conditional;

import com.doctoror.rxcursorloader.RxCursorLoader;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class ConditionalAlbumListQuery {

    private ConditionalAlbumListQuery() {
        throw new UnsupportedOperationException();
    }

    static final int COLUMN_ID = 0;
    static final int COLUMN_FIRST_YEAR = 1;
    static final int COLUMN_ALBUM = 2;
    static final int COLUMN_ALBUM_ART = 3;

    /**
     * Constricts params for albums search.
     *
     * @param contentUri content uri
     * @param selection selection
     * @return params
     */
    @NonNull
    public static RxCursorLoader.Query newParams(@NonNull final Uri contentUri,
            @Nullable final String selection) {
        return new RxCursorLoader.Query.Builder()
                .setContentUri(contentUri)
                .setProjection(new String[]{
                        MediaStore.Audio.Albums._ID,
                        MediaStore.Audio.Albums.FIRST_YEAR,
                        MediaStore.Audio.Albums.ALBUM,
                        MediaStore.Audio.Albums.ALBUM_ART,
                })
                .setSortOrder(MediaStore.Audio.Albums.FIRST_YEAR)
                .setSelection(selection)
                .create();
    }
}
