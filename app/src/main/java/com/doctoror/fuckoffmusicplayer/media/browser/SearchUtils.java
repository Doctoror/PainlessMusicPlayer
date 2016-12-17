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
package com.doctoror.fuckoffmusicplayer.media.browser;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.livelists.LivePlaylistRandom;
import com.doctoror.fuckoffmusicplayer.library.livelists.LivePlaylistRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistFactory;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

/**
 * Search utils
 */
public final class SearchUtils {

    private static final String TAG = "SearchUtils";

    private SearchUtils() {
        throw new UnsupportedOperationException();
    }

    public static void onPlayFromMediaId(@NonNull final Context context,
            @NonNull final String mediaId) {
        if (MediaBrowserImpl.MEDIA_ID_RANDOM.equals(mediaId)) {
            final List<Media> playlist = new LivePlaylistRandom(context.getResources())
                    .create(context);
            play(context, playlist, 0);
        } else if (MediaBrowserImpl.MEDIA_ID_RECENT.equals(mediaId)) {
            final List<Media> playlist = new LivePlaylistRecentlyScanned(context.getResources())
                    .create(context);
            play(context, playlist, 0);
        } else if (mediaId.startsWith(MediaBrowserImpl.MEDIA_ID_PREFIX_ALBUM)) {
            onPlayFromAlbumId(context, mediaId);
        } else if (mediaId.startsWith(MediaBrowserImpl.MEDIA_ID_PREFIX_GENRE)) {
            onPlayFromGenreId(context, mediaId);
        } else {
            long id = -1;
            try {
                id = Long.parseLong(mediaId);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Media id is not a number", e);
            }
            if (id != -1) {
                onPlayFromMediaId(context, id);
            }
        }
    }

    private static void onPlayFromAlbumId(@NonNull final Context context,
            @NonNull final String mediaId) {
        final String albumId = mediaId
                .substring(MediaBrowserImpl.MEDIA_ID_PREFIX_ALBUM.length());
        long id = -1;
        try {
            id = Long.parseLong(albumId);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Album id is not a number " + albumId, e);
        }
        if (id != -1) {
            final List<Media> playlist = PlaylistFactory.fromAlbum(context.getContentResolver(),
                    id);
            play(context, playlist, 0);
        }
    }

    private static void onPlayFromGenreId(@NonNull final Context context,
            @NonNull final String mediaId) {
        final String genreId = mediaId
                .substring(MediaBrowserImpl.MEDIA_ID_PREFIX_GENRE.length());
        long id = -1;
        try {
            id = Long.parseLong(genreId);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Genre id is not a number " + genreId, e);
        }
        if (id != -1) {
            final List<Media> playlist = PlaylistFactory.fromGenre(context.getContentResolver(),
                    id);
            play(context, playlist, 0);
        }
    }

    private static void onPlayFromMediaId(@NonNull final Context context,
            final long mediaId) {
        int position = -1;
        List<Media> playlist = CurrentPlaylist.getInstance(context).getPlaylist();
        if (playlist != null && !playlist.isEmpty()) {
            final int size = playlist.size();
            for (int i = 0; i < size; i++) {
                if (playlist.get(i).getId() == mediaId) {
                    position = i;
                    break;
                }
            }
        }

        // If this media is not found in current playlist
        if (position == -1) {
            position = 0;
            playlist = PlaylistFactory.fromSelection(context.getContentResolver(),
                    MediaStore.Audio.Media._ID + '=' + mediaId,
                    null,
                    null,
                    null);
        }

        play(context, playlist, position);
    }

    public static void onPlayFromSearch(@NonNull final Context context,
            @Nullable final String query,
            @Nullable final Bundle extras) {
        if (TextUtils.isEmpty(query)) {
            PlaybackService.playAnything(context);
            return;
        }

        boolean isArtistFocus = false;
        boolean isAlbumFocus = false;

        String artist = null;
        String album = null;

        String mediaFocus = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
        if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
            isArtistFocus = true;
            artist = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
        } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
            isAlbumFocus = true;
            album = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
        }

        final ContentResolver resolver = context.getContentResolver();
        List<Media> playlist = null;
        if (isArtistFocus) {
            playlist = PlaylistFactory.fromArtistSearch(resolver, TextUtils.isEmpty(artist)
                    ? query : artist);
        } else if (isAlbumFocus) {
            playlist = PlaylistFactory.fromAlbumSearch(resolver, TextUtils.isEmpty(album)
                    ? query : album);
        }

        if (playlist == null || playlist.isEmpty()) {
            // No focus found, search by query for song title
            playlist = PlaylistFactory.fromTracksSearch(resolver, query);
        }

        playFromSearch(context, playlist, query);
    }

    private static void playFromSearch(@NonNull final Context context,
            @Nullable final List<Media> playlist,
            @Nullable final String query) {
        if (playlist != null && !playlist.isEmpty()) {
            PlaylistUtils.play(context, playlist);
        } else {
            final String message = TextUtils.isEmpty(query)
                    ? context.getString(R.string.No_media_found)
                    : context.getString(R.string.No_media_found_for_s, query);

            PlaybackService.stopWithError(context, message);
        }
    }

    private static void play(@NonNull final Context context,
            @Nullable final List<Media> playlist,
            final int position) {
        if (playlist != null && !playlist.isEmpty()) {
            PlaylistUtils.play(context, playlist, position);
        } else {
            PlaybackService.stopWithError(context, context.getString(R.string.No_media_found));
        }
    }

}
