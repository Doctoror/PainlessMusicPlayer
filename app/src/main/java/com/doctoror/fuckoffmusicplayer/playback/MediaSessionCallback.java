package com.doctoror.fuckoffmusicplayer.playback;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;

import java.util.List;

/**
 * {@link MediaSessionCompat.Callback} implementation
 */
final class MediaSessionCallback extends MediaSessionCompat.Callback {

    @NonNull
    private final Context mContext;

    MediaSessionCallback(@NonNull final Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void onPlay() {
        PlaybackService.play(mContext);
    }

    @Override
    public void onStop() {
        PlaybackService.stop(mContext);
    }

    @Override
    public void onPause() {
        PlaybackService.pause(mContext);
    }

    @Override
    public void onSkipToPrevious() {
        PlaybackService.prev(mContext);
    }

    @Override
    public void onSkipToNext() {
        PlaybackService.next(mContext);
    }

    @Override
    public void onPlayFromSearch(final String query, final Bundle extras) {
        if (TextUtils.isEmpty(query)) {
            PlaybackService.playAnything(mContext);
            return;
        }

        boolean isArtistFocus = false;
        boolean isAlbumFocus = false;

        String artist = null;
        String album = null;

        String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
        if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
            isArtistFocus = true;
            artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
        } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
            isAlbumFocus = true;
            album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
        }

        final ContentResolver resolver = mContext.getContentResolver();
        List<Media> playlist = null;
        if (isArtistFocus) {
            playlist = PlaylistUtils.fromArtistSearch(resolver, TextUtils.isEmpty(artist)
                    ? query : artist);
        } else if (isAlbumFocus) {
            playlist = PlaylistUtils.fromAlbumSearch(resolver, TextUtils.isEmpty(album)
                    ? query : album);
        }

        if (playlist == null || playlist.isEmpty()) {
            // No focus found, search by query for song title
            playlist = PlaylistUtils.fromTracksSearch(resolver, query);
        }

        if (playlist != null && !playlist.isEmpty()) {
            // Start playing from the beginning of the search results
            PlaylistUtils.play(mContext, playlist);
        } else {
            // Stop if nothing found
            PlaybackService.stop(mContext);
        }
    }
}
