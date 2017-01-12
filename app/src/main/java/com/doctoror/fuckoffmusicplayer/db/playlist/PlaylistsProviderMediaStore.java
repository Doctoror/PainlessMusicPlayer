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
package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreVolumeNames;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.util.SelectionUtils;
import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import rx.Observable;

/**
 * MediaStore implementation for {@link PlaylistsProvider}
 */
public final class PlaylistsProviderMediaStore implements PlaylistsProvider {

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public PlaylistsProviderMediaStore(@NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        mContentResolver = contentResolver;
        mMediaProvider = mediaProvider;
    }

    @Override
    public Observable<Cursor> load(@Nullable final String filter) {
        return RxCursorLoader.create(mContentResolver, newQuery(filter));
    }

    @Override
    public Observable<List<Media>> loadQueue(final long playlistId) {
        return loadMediaIdsCursor(playlistId)
                .map(this::mediaIdsFromCursor)
                .map(this::loadMediasForIds);
    }

    @NonNull
    private Observable<Cursor> loadMediaIdsCursor(final long playlistId) {
        final RxCursorLoader.Query query = new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Playlists.Members.getContentUri(
                        MediaStoreVolumeNames.EXTERNAL, playlistId))
                .setProjection(new String[]{MediaStore.Audio.Playlists.Members.AUDIO_ID})
                .setSortOrder(MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER)
                .create();

        return RxCursorLoader.create(mContentResolver, query);
    }

    @NonNull
    private long[] mediaIdsFromCursor(@NonNull final Cursor medaIdsCursor) {
        final long[] ids = new long[medaIdsCursor.getCount()];
        int i = 0;
        for (medaIdsCursor.moveToFirst(); !medaIdsCursor.isAfterLast();
                medaIdsCursor.moveToNext(), i++) {
            ids[i] = medaIdsCursor.getLong(0);
        }
        medaIdsCursor.close();
        return ids;
    }

    @NonNull
    private List<Media> loadMediasForIds(@NonNull final long[] mediaIds) {
        return mMediaProvider.load(
                SelectionUtils.inSelectionLong(MediaStore.Audio.Media._ID, mediaIds),
                null,
                SelectionUtils.orderByLongField(MediaStore.Audio.Media._ID, mediaIds),
                null);
    }

    @NonNull
    private static RxCursorLoader.Query newQuery(@Nullable final String filter) {
        return new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Playlists._ID,
                        MediaStore.Audio.Playlists.NAME
                })
                .setSelection(TextUtils.isEmpty(filter) ? null : MediaStore.Audio.Albums.ALBUM
                        + " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(filter))
                .setSortOrder(MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER)
                .create();
    }
}
