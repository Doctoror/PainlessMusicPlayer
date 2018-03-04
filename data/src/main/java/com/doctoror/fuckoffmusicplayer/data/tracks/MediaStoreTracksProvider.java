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
package com.doctoror.fuckoffmusicplayer.data.tracks;

import com.doctoror.fuckoffmusicplayer.domain.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.data.util.SqlUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import io.reactivex.Observable;

public final class MediaStoreTracksProvider implements TracksProvider {

    // Avoids non-music and hidden files
    public static final String SELECTION_NON_HIDDEN_MUSIC = MediaStore.Audio.Media.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.Media.DATA + " NOT LIKE '%/.%'";

    public static final String SORT_ORDER = MediaStore.Audio.Media.TITLE;

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaStoreTracksProvider(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter) {
        return load(searchFilter, null, true);
    }

    @Override
    public Observable<Cursor> load(@Nullable final String searchFilter,
            @Nullable final Integer limit,
            final boolean includeSearchByArtist) {
        return RxCursorLoader
                .create(mContentResolver, newParams(searchFilter, limit, includeSearchByArtist));
    }

    @NonNull
    private static RxCursorLoader.Query newParams(
            @Nullable final String searchFilter,
            @Nullable final Integer limit,
            final boolean includeSearchByArtist) {
        final String wrapped = TextUtils.isEmpty(searchFilter) ? null
                : SqlUtils.escapeAndWrapForLikeArgument(searchFilter);
        final StringBuilder selection = new StringBuilder(256);
        selection.append(SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(wrapped)) {
            selection.append(" AND ")

                    .append(MediaStore.Audio.Media.TITLE)
                    .append(" LIKE ").append(wrapped);

            if (includeSearchByArtist) {
                selection
                        .append(" OR ")
                        .append(MediaStore.Audio.Media.ARTIST)
                        .append(" LIKE ").append(wrapped);
            }
        }
        final RxCursorLoader.Query.Builder b = new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST
                })
                .setSelection(selection.toString());

        if (limit == null) {
            b.setSortOrder(SORT_ORDER);
        } else {
            b.setSortOrder(SORT_ORDER + " LIMIT " + limit);
        }

        return b.create();
    }

}
