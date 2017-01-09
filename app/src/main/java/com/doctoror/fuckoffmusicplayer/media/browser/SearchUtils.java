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
import com.doctoror.fuckoffmusicplayer.db.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbums;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderArtists;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderGenres;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRandom;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderTracks;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueUtils;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Search utils
 */
public final class SearchUtils {

    private static final String TAG = "SearchUtils";

    @NonNull
    private final Context mContext;

    @Inject
    MediaProvider mediaProvider;

    @Inject
    PlaybackData mPlaybackData;

    @Inject
    PlaylistProviderArtists artistPlaylistFactory;

    @Inject
    PlaylistProviderAlbums albumPlaylistFactory;

    @Inject
    PlaylistProviderGenres genrePlaylistFactory;

    @Inject
    PlaylistProviderTracks trackPlaylistFactory;

    @Inject
    PlaylistProviderRecentlyScanned recentlyScannedPlaylistFactory;

    @Inject
    PlaylistProviderRandom randomPlaylistFactory;

    public SearchUtils(@NonNull final Context context) {
        mContext = context;
        DaggerHolder.getInstance(context).mainComponent().inject(this);
    }

    public void onPlayFromMediaId(@NonNull final String mediaId) {
        if (MediaBrowserImpl.MEDIA_ID_RANDOM.equals(mediaId)) {
            final List<Media> queue = randomPlaylistFactory.randomPlaylist();
            play(mContext, queue, 0);
        } else if (MediaBrowserImpl.MEDIA_ID_RECENT.equals(mediaId)) {
            final List<Media> queue = recentlyScannedPlaylistFactory.recentlyScannedPlaylist();
            play(mContext, queue, 0);
        } else if (mediaId.startsWith(MediaBrowserImpl.MEDIA_ID_PREFIX_ALBUM)) {
            onPlayFromAlbumId(mediaId);
        } else if (mediaId.startsWith(MediaBrowserImpl.MEDIA_ID_PREFIX_GENRE)) {
            onPlayFromGenreId(mediaId);
        } else {
            long id = -1;
            try {
                id = Long.parseLong(mediaId);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Media id is not a number", e);
            }
            if (id != -1) {
                onPlayFromMediaId(id);
            }
        }
    }

    private void onPlayFromAlbumId(@NonNull final String mediaId) {
        final String albumId = mediaId
                .substring(MediaBrowserImpl.MEDIA_ID_PREFIX_ALBUM.length());
        long id = -1;
        try {
            id = Long.parseLong(albumId);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Album id is not a number " + albumId, e);
        }
        if (id != -1) {
            final List<Media> queue = albumPlaylistFactory.fromAlbum(id);
            play(mContext, queue, 0);
        }
    }

    private void onPlayFromGenreId(@NonNull final String mediaId) {
        final String genreId = mediaId
                .substring(MediaBrowserImpl.MEDIA_ID_PREFIX_GENRE.length());
        long id = -1;
        try {
            id = Long.parseLong(genreId);
        } catch (NumberFormatException e) {
            Log.w(TAG, "Genre id is not a number " + genreId, e);
        }
        if (id != -1) {
            final List<Media> queue = genrePlaylistFactory.fromGenre(id);
            play(mContext, queue, 0);
        }
    }

    private void onPlayFromMediaId(final long mediaId) {
        int position = -1;
        List<Media> queue = mPlaybackData.getQueue();
        if (queue != null && !queue.isEmpty()) {
            final int size = queue.size();
            for (int i = 0; i < size; i++) {
                if (queue.get(i).getId() == mediaId) {
                    position = i;
                    break;
                }
            }
        }

        // If this media is not found in current playlist
        if (position == -1) {
            position = 0;
            queue = mediaProvider.load(mediaId);
        }

        play(mContext, queue, position);
    }

    public void onPlayFromSearch(@Nullable final String query,
            @Nullable final Bundle extras) {
        if (TextUtils.isEmpty(query)) {
            PlaybackServiceControl.playAnything(mContext);
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

        List<Media> queue = null;
        if (isArtistFocus) {
            queue = artistPlaylistFactory.fromArtistSearch(TextUtils.isEmpty(artist)
                    ? query : artist);
        } else if (isAlbumFocus) {
            queue = albumPlaylistFactory.fromAlbumSearch(TextUtils.isEmpty(album)
                    ? query : album);
        }

        if (queue == null || queue.isEmpty()) {
            // No focus found, search by query for song title
            queue = trackPlaylistFactory.fromTracksSearch(query);
        }

        playFromSearch(queue, query);
    }

    private void playFromSearch(@Nullable final List<Media> queue,
            @Nullable final String query) {
        if (queue != null && !queue.isEmpty()) {
            QueueUtils.play(mContext, mPlaybackData, queue);
        } else {
            final String message = TextUtils.isEmpty(query)
                    ? mContext.getString(R.string.No_media_found)
                    : mContext.getString(R.string.No_media_found_for_s, query);

            PlaybackServiceControl.stopWithError(mContext, message);
        }
    }

    private void play(@NonNull final Context context,
            @Nullable final List<Media> queue,
            final int position) {
        if (queue != null && !queue.isEmpty()) {
            QueueUtils.play(context, mPlaybackData, queue, position);
        } else {
            PlaybackServiceControl
                    .stopWithError(context, context.getString(R.string.No_media_found));
        }
    }

}
