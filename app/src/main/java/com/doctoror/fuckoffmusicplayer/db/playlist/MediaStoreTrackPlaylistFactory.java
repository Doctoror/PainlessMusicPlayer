package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreVolumeNames;
import com.doctoror.fuckoffmusicplayer.db.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.SelectionUtils;
import com.doctoror.fuckoffmusicplayer.util.SqlUtils;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */

public final class MediaStoreTrackPlaylistFactory implements TrackPlaylistFactory {

    @NonNull
    private final ContentResolver mContentResolver;

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public MediaStoreTrackPlaylistFactory(
            @NonNull final ContentResolver contentResolver,
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        mContentResolver = contentResolver;
        mMediaProvider = mediaProvider;
    }

    @Nullable
    @Override
    public List<Media> forTracks(@NonNull final long[] trackIds, @Nullable final String sortOrder) {
        return mMediaProvider
                .load(SelectionUtils.inSelectionLong(MediaStore.Audio.Media._ID, trackIds),
                        null,
                        sortOrder,
                        null);
    }

    @Nullable
    @Override
    public List<Media> fromTracksSearch(@Nullable final String query) {
        final List<Long> ids = new ArrayList<>(15);

        final StringBuilder sel = new StringBuilder(256);
        sel.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            final String likeQuery = " LIKE " + SqlUtils.escapeAndWrapForLikeArgument(query);
            sel.append(" AND (").append(MediaStore.Audio.Media.TITLE).append(likeQuery);
            sel.append(" OR ").append(MediaStore.Audio.Media.ARTIST).append(likeQuery);
            sel.append(" OR ").append(MediaStore.Audio.Media.ALBUM).append(likeQuery);
            sel.append(')');
        }

        final List<Media> playlist = mMediaProvider.load(sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK,
                PlaylistConfig.MAX_PLAYLIST_SIZE);

        for (final Media media : playlist) {
            ids.add(media.getId());
        }

        if (!TextUtils.isEmpty(query) && playlist.size() < PlaylistConfig.MAX_PLAYLIST_SIZE) {
            // Search in genres for tracks with media ids that do not match found ids
            Cursor c = mContentResolver.query(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
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
                playlist.addAll(mMediaProvider.load(
                        MediaStore.Audio.Genres.Members
                                .getContentUri(MediaStoreVolumeNames.EXTERNAL, genreId),
                        SelectionUtils.notInSelection(MediaStore.Audio.Media._ID, ids),
                        null,
                        "RANDOM()",
                        PlaylistConfig.MAX_PLAYLIST_SIZE - ids.size()));
            }
        }

        return playlist;
    }
}
