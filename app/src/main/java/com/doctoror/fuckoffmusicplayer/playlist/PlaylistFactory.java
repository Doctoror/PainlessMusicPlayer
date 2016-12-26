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
package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksQuery;
import com.doctoror.fuckoffmusicplayer.util.SelectionUtils;
import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Playlist factory
 */
public final class PlaylistFactory {

    private static final String TAG = "PlaylistFactory";

    private static final int MAX_PLAYLIST_SIZE = 99;

    private static final String EXTERNAL = "external";

    private PlaylistFactory() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromArtist(@NonNull final ContentResolver resolver,
            final long artistId) {
        final List<Media> playlist = new ArrayList<>(25);
        final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                TracksQuery.SELECTION_NON_HIDDEN_MUSIC + " AND "
                        + MediaStore.Audio.Media.ARTIST_ID + '=' + artistId,
                null,
                MediaStore.Audio.Media.ALBUM_ID + ',' + MediaStore.Audio.Media.TRACK);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }
        return playlist;
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromAlbumSearch(@NonNull final ContentResolver resolver,
            @Nullable final String query) {
        final StringBuilder sel = new StringBuilder(256);
        sel.append(TracksQuery.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            sel.append(" AND ").append(MediaStore.Audio.Media.ALBUM).append(" LIKE ")
                    .append(SqlUtils.escapeAndWrapForLikeArgument(query));
        }

        final List<Media> playlist = new ArrayList<>(15);
        final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK + " LIMIT " +
                        MAX_PLAYLIST_SIZE);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }
        return playlist;
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromArtistSearch(@NonNull final ContentResolver resolver,
            @Nullable final String query) {
        final StringBuilder sel = new StringBuilder(256);
        sel.append(TracksQuery.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            sel.append(" AND ").append(MediaStore.Audio.Media.ARTIST).append(" LIKE ")
                    .append(SqlUtils.escapeAndWrapForLikeArgument(query));
        }

        final List<Media> playlist = new ArrayList<>(15);
        final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK + " LIMIT " +
                        MAX_PLAYLIST_SIZE);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }
        return playlist;
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromTracksSearch(@NonNull final ContentResolver resolver,
            @Nullable final String query) {
        final List<Media> playlist = new ArrayList<>(15);
        final List<Long> ids = new ArrayList<>(15);

        final StringBuilder sel = new StringBuilder(256);
        sel.append(TracksQuery.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            final String likeQuery = " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(query);
            sel.append(" AND (").append(MediaStore.Audio.Media.TITLE).append(likeQuery);
            sel.append(" OR ").append(MediaStore.Audio.Media.ARTIST).append(likeQuery);
            sel.append(" OR ").append(MediaStore.Audio.Media.ALBUM).append(likeQuery);
            sel.append(')');
        }

        Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK + " LIMIT " +
                        MAX_PLAYLIST_SIZE);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
                ids.add(c.getLong(MediaQuery.COLUMN_ID));
            }
            c.close();
        }

        if (!TextUtils.isEmpty(query) && playlist.size() < MAX_PLAYLIST_SIZE) {
            // Search in genres for tracks with media ids that do not match found ids
            c = resolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    new String[]{BaseColumns._ID},
                    MediaStore.Audio.Genres.NAME + "=?",
                    new String[]{StringUtils.capWords(query)},
                    null);

            Long genreId = null;
            if (c != null) {
                try {
                    if (c.moveToFirst()) {
                        genreId = c.getLong(0);
                    }
                } finally {
                    c.close();
                }
            }

            if (genreId != null) {
                c = resolver
                        .query(MediaStore.Audio.Genres.Members.getContentUri(EXTERNAL, genreId),
                                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                                SelectionUtils.notInSelection(MediaStore.Audio.Media._ID, ids),
                                null,
                                "RANDOM() LIMIT " + (MAX_PLAYLIST_SIZE - ids.size()));
                if (c != null) {
                    for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                        playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
                    }
                    c.close();
                }
            }

        }

        return playlist;
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromAlbum(@NonNull final ContentResolver resolver,
            final long albumId) {
        return fromAlbums(resolver, new long[]{albumId}, null);
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromAlbums(@NonNull final ContentResolver resolver,
            @NonNull final long[] albumIds,
            @Nullable final Long forArtist) {
        final List<Media> playlist = new ArrayList<>(15 * albumIds.length);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < albumIds.length; i++) {
            final long albumId = albumIds[i];
            final StringBuilder selection = new StringBuilder(256);
            selection.append(TracksQuery.SELECTION_NON_HIDDEN_MUSIC).append(" AND ");
            selection.append(MediaStore.Audio.Media.ALBUM_ID).append('=').append(albumId);
            if (forArtist != null) {
                selection.append(" AND ")
                        .append(MediaStore.Audio.Media.ARTIST_ID).append('=').append(forArtist);
            }
            final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                    MediaQuery.PROJECTION_WITH_ALBUM_ART,
                    selection.toString(),
                    null,
                    MediaStore.Audio.Media.TRACK);
            if (c != null) {
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
                }
                c.close();
            }
        }
        return playlist;
    }

    @Nullable
    public static List<Media> forTracks(@NonNull final ContentResolver resolver,
            @NonNull final long[] trackIds,
            @Nullable final String sortOrder) {
        final List<Media> playlist = new ArrayList<>(trackIds.length);
        final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                SelectionUtils.inSelectionLong(MediaStore.Audio.Media._ID, trackIds),
                null,
                sortOrder);
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }
        return playlist;
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromGenre(@NonNull final ContentResolver resolver,
            final long genreId) {
        final List<Media> playlist = new ArrayList<>(MAX_PLAYLIST_SIZE);
        final Cursor c = resolver.query(MediaStore.Audio.Genres.Members.getContentUri(EXTERNAL,
                genreId),
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                TracksQuery.SELECTION_NON_HIDDEN_MUSIC,
                null,
                "RANDOM()");
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }
        return playlist;
    }

    @Nullable
    @WorkerThread
    public static List<Media> fromSelection(@NonNull final ContentResolver resolver,
            @Nullable final String selection,
            @Nullable final String[] selectionArgs,
            @Nullable final String orderBy,
            @Nullable final Integer limit) {
        //noinspection UnnecessaryUnboxing
        final List<Media> playlist = new ArrayList<>(limit == null ? 50 : limit.intValue());
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
        final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                selection,
                selectionArgs,
                order.length() == 0 ? null : order.toString());
        if (c != null) {
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }
        return playlist;
    }

    @NonNull
    public static List<Media> forFile(@NonNull final ContentResolver resolver,
            @NonNull final Uri uri) throws Exception {
        final List<Media> playlist = new ArrayList<>(1);
        final Cursor c = resolver.query(MediaQuery.CONTENT_URI,
                MediaQuery.PROJECTION_WITH_ALBUM_ART,
                MediaStore.Audio.Media.DATA.concat("=?"),
                new String[]{uri.getPath()},
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                playlist.add(mediaFromCursor(c, c.getString(MediaQuery.COLUMN_ALBUM_ART)));
            }
            c.close();
        }

        if (playlist.isEmpty()) {
            // Not found in MediaStore. Create Media from file data
            playlist.add(mediaFromFile(uri));
        }

        return playlist;
    }

    @NonNull
    private static Media mediaFromFile(@NonNull final Uri uri) throws Exception {
        final MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(uri.getPath());

            final Media media = new Media();
            media.title = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            media.artist = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            media.album = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            media.data = uri;

            final String duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (!TextUtils.isEmpty(duration)) {
                try {
                    media.duration = Long.parseLong(duration);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mediaFromFile() duration is not a number: " + duration);
                }
            }

            final String trackNumber = r
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            if (!TextUtils.isEmpty(trackNumber)) {
                try {
                    media.track = Integer.parseInt(duration);
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mediaFromFile() track number is not a number: " + duration);
                }
            }

            return media;
        } finally {
            r.release();
        }
    }

    @NonNull
    private static Media mediaFromCursor(@NonNull final Cursor c,
            @Nullable final String albumArt) {
        final Media media = new Media();
        media.id = c.getLong(MediaQuery.COLUMN_ID);
        media.track = c.getInt(MediaQuery.COLUMN_TRACK);
        media.title = c.getString(MediaQuery.COLUMN_TITLE);
        media.artist = c.getString(MediaQuery.COLUMN_ARTIST);
        media.album = c.getString(MediaQuery.COLUMN_ALBUM);
        media.albumId = c.getLong(MediaQuery.COLUMN_ALBUM_ID);
        media.albumArt = albumArt;
        media.duration = c.getLong(MediaQuery.COLUMN_DURATION);
        final String path = c.getString(MediaQuery.COLUMN_DATA);
        if (!TextUtils.isEmpty(path)) {
            media.data = Uri.parse(new File(path).toURI().toString());
        }
        return media;
    }

}
