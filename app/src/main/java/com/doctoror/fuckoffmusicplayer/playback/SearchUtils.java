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
package com.doctoror.fuckoffmusicplayer.playback;

import com.doctoror.fuckoffmusicplayer.playlist.Media;
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

    private SearchUtils() {
        throw new UnsupportedOperationException();
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
        boolean isGenreFocus = false;

        String artist = null;
        String album = null;
        String genre = null;

        String mediaFocus = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
        if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
            isArtistFocus = true;
            artist = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
        } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
            isAlbumFocus = true;
            album = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
        } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Genres.ENTRY_CONTENT_TYPE)) {
            isGenreFocus = true;
            genre = extras == null ? null : extras.getString("android.intent.extra.genre");
        }

        final ContentResolver resolver = context.getContentResolver();
        List<Media> playlist = null;
        if (isArtistFocus) {
            playlist = PlaylistUtils.fromArtistSearch(resolver, TextUtils.isEmpty(artist)
                    ? query : artist);
        } else if (isAlbumFocus) {
            playlist = PlaylistUtils.fromAlbumSearch(resolver, TextUtils.isEmpty(album)
                    ? query : album);
        } else if (isGenreFocus) {
            // TODO check if working
            playlist = PlaylistUtils.fromGenreSearch(resolver, TextUtils.isEmpty(genre)
                    ? query : genre);
        }

        if (playlist == null || playlist.isEmpty()) {
            // No focus found, search by query for song title
            playlist = PlaylistUtils.fromTracksSearch(resolver, query);
        }

        if (playlist != null && !playlist.isEmpty()) {
            // Start playing from the beginning of the search results
            PlaylistUtils.play(context, playlist);
        } else {
            // Stop if nothing found
            PlaybackService.stop(context);
        }
    }

}
