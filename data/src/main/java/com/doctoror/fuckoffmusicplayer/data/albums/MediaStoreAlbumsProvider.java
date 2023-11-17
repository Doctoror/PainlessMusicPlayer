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
package com.doctoror.fuckoffmusicplayer.data.albums;

import static com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProviderKt.COLUMN_ALBUM_ART;
import static com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProviderKt.COLUMN_ID;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreVolumeNames;
import com.doctoror.fuckoffmusicplayer.data.util.SelectionUtils;
import com.doctoror.fuckoffmusicplayer.data.util.SqlUtils;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;
import com.doctoror.rxcursorloader.RxCursorLoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * MediaStore {@link AlbumsProvider}
 */
public final class MediaStoreAlbumsProvider implements AlbumsProvider {

    private static final String SORT_ORDER = MediaStore.Audio.Albums.ALBUM;

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final RecentActivityManager mRecentActivityManager;

    public MediaStoreAlbumsProvider(
            @NonNull final ContentResolver contentResolver,
            @NonNull final RecentActivityManager recentActivityManager) {
        mContentResolver = contentResolver;
        mRecentActivityManager = recentActivityManager;
    }

    @Override
    public Observable<Cursor> load(
            @Nullable final String searchFilter,
            @NonNull final Scheduler scheduler) {
        final RxCursorLoader.Query query = newParams(searchFilter);
        return RxCursorLoader
                .observable(mContentResolver, query, scheduler)
                .map(transformForLegacyAlbumArtHack());
    }

    @Override
    public Observable<Cursor> loadForArtist(final long artistId) {
        final RxCursorLoader.Query query = newParamsBuilder(null)
                .setContentUri(MediaStore.Audio.Artists.Albums.getContentUri(
                        MediaStoreVolumeNames.EXTERNAL, artistId))
                .setSortOrder(MediaStore.Audio.Albums.FIRST_YEAR)
                .create();

        return RxCursorLoader
                .observable(mContentResolver, query, Schedulers.io())
                .map(transformForLegacyAlbumArtHack());
    }

    @NonNull
    @Override
    public Observable<Cursor> loadForGenre(final long genreId) {
        return trackIdsForGenre(genreId)
                .switchMap(trackIds -> albumIdsForTrackIds(trackIds))
                .switchMap(albumIds -> loadAlbumsOrderedByIds(albumIds));
    }

    private ObservableTransformer<Cursor, long[]> trackIdsTransformer() {
        return upstream -> upstream.map((Function<Cursor, long[]>) cursor -> {
                    final long[] tracks = new long[cursor.getCount()];
                    int counter = 0;
                    for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                        tracks[counter++] = cursor.getLong(0);
                    }
                    return tracks;
                }
        );
    }

    private Observable<long[]> trackIdsForGenre(final long genreId) {
        return RxCursorLoader.create(mContentResolver,
                new RxCursorLoader
                        .Query
                        .Builder()
                        .setContentUri(
                                MediaStore.Audio.Genres.Members.getContentUri(
                                        MediaStoreVolumeNames.EXTERNAL,
                                        genreId
                                )
                        )
                        .setProjection(new String[]{MediaStore.Audio.Genres.Members.AUDIO_ID})
                        .create()
        ).compose(trackIdsTransformer());
    }

    @NonNull
    private Observable<Set<Long>> albumIdsForTrackIds(final long[] trackIds) {
        return RxCursorLoader.create(mContentResolver,
                new RxCursorLoader
                        .Query
                        .Builder()
                        .setContentUri(Media.EXTERNAL_CONTENT_URI)
                        .setProjection(new String[]{MediaStore.Audio.Genres.Members.ALBUM_ID})
                        .setSelection(SelectionUtils.inSelectionLong(Media._ID, trackIds))
                        .create()
        ).map((Function<Cursor, Set<Long>>) cursor -> {
            final Set<Long> ids = new HashSet<>();
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                ids.add(cursor.getLong(0));
            }
            return ids;
        });
    }

    @Override
    public Observable<Cursor> loadRecentlyPlayedAlbums() {
        return loadRecentlyPlayedAlbums(null);
    }

    @Override
    public Observable<Cursor> loadRecentlyPlayedAlbums(@Nullable final Integer limit) {
        final RxCursorLoader.Query query = newRecentlyPlayedAlbumsQuery(limit);
        return RxCursorLoader
                .observable(mContentResolver, query, Schedulers.io())
                .map(transformForLegacyAlbumArtHack());
    }

    @NonNull
    private RxCursorLoader.Query newRecentlyPlayedAlbumsQuery(@Nullable final Integer limit) {
        final long[] recentlyPlayedAlbums = mRecentActivityManager.getRecentlyPlayedAlbums();
        final String sortOrder = SelectionUtils.orderByLongField(MediaStore.Audio.Albums._ID,
                recentlyPlayedAlbums);

        final RxCursorLoader.Query.Builder query = newParamsBuilder(limit);
        query
                .setSelection(SelectionUtils.inSelectionLong(MediaStore.Audio.Albums._ID,
                        recentlyPlayedAlbums))

                .setSortOrder(sortOrder);
        return query.create();
    }

    @Override
    public Observable<Cursor> loadRecentlyScannedAlbums(@Nullable final Integer limit) {
        final RxCursorLoader.Query recentTracksQuery = new RxCursorLoader.Query.Builder()
                .setContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                .setProjection(new String[]{
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.DATE_ADDED
                })
                .setSortOrder(MediaStore.Audio.Media.DATE_ADDED + " DESC")
                .create();

        return
                RxCursorLoader
                        .observable(mContentResolver, recentTracksQuery, Schedulers.io())
                        .map(c -> albumIds(c, limit != null ? limit : Integer.MAX_VALUE))
                        .flatMap(this::loadAlbumsOrderedByIds);
    }

    @NonNull
    private Collection<Long> albumIds(@NonNull final Cursor c, final int limit) {
        try {
            final Set<Long> ids = new LinkedHashSet<>(limit);
            if (limit == 0) {
                return ids;
            }
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                final long id = c.getLong(0);
                if (id > 0) {
                    ids.add(id);
                    if (ids.size() == limit) {
                        return ids;
                    }
                }
            }
            return ids;
        } finally {
            c.close();
        }
    }

    private Observable<Cursor> loadAlbumsOrderedByIds(@NonNull final Collection<Long> ids) {
        final RxCursorLoader.Query query = newParamsBuilder(null)
                .setSelection(SelectionUtils.inSelection(MediaStore.Audio.Albums._ID, ids))
                .setSortOrder(SelectionUtils.orderByField(MediaStore.Audio.Albums._ID, ids))
                .create();

        return RxCursorLoader
                .observable(mContentResolver, query, Schedulers.io())
                .map(transformForLegacyAlbumArtHack());
    }

    private Function<Cursor, Cursor> transformForLegacyAlbumArtHack() {
        return new Function<Cursor, Cursor>() {

            @Override
            public Cursor apply(Cursor cursor) {
                return new CursorWrapper(cursor) {

                    @Override
                    public String getString(int columnIndex) {
                        if (columnIndex == COLUMN_ALBUM_ART) {
                            return ContentUris.withAppendedId(
                                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                    cursor.getLong(COLUMN_ID)
                            ).toString();
                        }

                        return super.getString(columnIndex);
                    }
                };
            }
        };
    }

    /**
     * Constructs params for albums search.
     *
     * @param searchFilter the user input filter string. May be null if no filtering needed
     * @return params
     */
    @NonNull
    private static RxCursorLoader.Query newParams(@Nullable final String searchFilter) {
        return newParamsBuilder(null)
                .setSelection(searchFilterToSelection(searchFilter))
                .create();
    }

    @Nullable
    private static String searchFilterToSelection(@Nullable final String searchFilter) {
        return TextUtils.isEmpty(searchFilter)
                ? null : MediaStore.Audio.Albums.ALBUM + " LIKE " + SqlUtils
                .escapeAndWrapForLikeArgument(searchFilter);
    }

    /**
     * Constructs params Builder for albums search.
     *
     * @return params Builder
     */
    @NonNull
    private static RxCursorLoader.Query.Builder newParamsBuilder(@Nullable final Integer limit) {
        Uri contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        if (limit != null) {
            contentUri = contentUri
                    .buildUpon()
                    .appendQueryParameter("LIMIT", limit.toString())
                    .build();
        }

        final String projection[];
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection = new String[]{
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.FIRST_YEAR
            };
        } else {
            projection = new String[]{
                    MediaStore.Audio.Albums._ID,
                    MediaStore.Audio.Albums.ALBUM,
                    MediaStore.Audio.Albums.FIRST_YEAR,
                    MediaStore.Audio.Albums.ALBUM_ART
            };
        }

        return new RxCursorLoader.Query.Builder()
                .setContentUri(contentUri)
                .setProjection(projection)
                .setSortOrder(SORT_ORDER);
    }
}
