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
package com.doctoror.fuckoffmusicplayer.data.media;

import com.doctoror.fuckoffmusicplayer.domain.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

/**
 * MedaiStore {@link MediaProvider}
 */
public final class MediaStoreMediaProvider implements MediaProvider {

    @NonNull
    private final ContentResolver mContentResolver;

    public MediaStoreMediaProvider(@NonNull final ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }

    @Override
    @NonNull
    public Observable<List<Media>> load(@Nullable final String selection,
            @Nullable final String[] selectionArgs,
            @Nullable final String orderBy,
            @Nullable final Integer limit) {
        return load(MediaQuery.CONTENT_URI, selection, selectionArgs, orderBy, limit);
    }

    @NonNull
    @Override
    public Observable<List<Media>> load(final long id) {
        return load(MediaQuery.CONTENT_URI,
                MediaStore.Audio.Media._ID + '=' + id,
                null,
                null,
                null);
    }

    @NonNull
    public Observable<List<Media>> load(@NonNull final Uri contentUri,
            @Nullable final String selection,
            @Nullable final String[] selectionArgs,
            @Nullable final String orderBy,
            @Nullable final Integer limit) {
        return Observable.fromCallable(
                () -> loadMediaList(contentUri, selection, selectionArgs, orderBy, limit));
    }

    @NonNull
    @WorkerThread
    private List<Media> loadMediaList(@NonNull final Uri contentUri,
            @Nullable final String selection,
            @Nullable final String[] selectionArgs,
            @Nullable final String orderBy,
            @Nullable final Integer limit) {
        List<Media> result = null;

        final StringBuilder order = new StringBuilder(128);
        if (orderBy != null) {
            order.append(orderBy);
        }

        if (limit != null) {
            if (order.length() == 0) {
                throw new IllegalArgumentException("Cannot use LIMIT without ORDER BY");
            }
            order.append(" LIMIT ").append(limit);
        }

        final Cursor c = mContentResolver.query(contentUri,
                MediaQuery.PROJECTION,
                selection,
                selectionArgs,
                order.length() == 0 ? null : order.toString());
        if (c != null) {
            result = new ArrayList<>(c.getCount());
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                result.add(mediaFromCursor(c));
            }
            c.close();
        }
        return result == null ? new ArrayList<>() : result;
    }

    @NonNull
    private static Media mediaFromCursor(@NonNull final Cursor c) {
        final Media media = new Media();
        media.setId(c.getLong(MediaQuery.COLUMN_ID));
        media.setTrack(c.getInt(MediaQuery.COLUMN_TRACK));
        media.setTitle(c.getString(MediaQuery.COLUMN_TITLE));
        media.setArtist(c.getString(MediaQuery.COLUMN_ARTIST));
        media.setAlbum(c.getString(MediaQuery.COLUMN_ALBUM));
        media.setAlbumId(c.getLong(MediaQuery.COLUMN_ALBUM_ID));
        media.setAlbumArt(c.getString(MediaQuery.COLUMN_ALBUM_ART));
        media.setDuration(c.getLong(MediaQuery.COLUMN_DURATION));
        final String path = c.getString(MediaQuery.COLUMN_DATA);
        if (!TextUtils.isEmpty(path)) {
            media.setData(Uri.parse(new File(path).toURI().toString()));
        }
        return media;
    }
}
