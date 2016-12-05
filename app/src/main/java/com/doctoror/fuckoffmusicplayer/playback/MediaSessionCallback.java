package com.doctoror.fuckoffmusicplayer.playback;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

/**
 * Created by Yaroslav Mytkalyk on 08.11.16.
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
        } else {
            // Build a queue based on songs that match "query" or "extras" param
            String mediaFocus = extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
            if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                isArtistFocus = true;
                artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
            } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                isAlbumFocus = true;
                album = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
            }

            // Implement additional "extras" param filtering
        }

        // Implement your logic to retrieve the queue
        if (isArtistFocus) {
            result = searchMusicByArtist(artist);
        } else if (isAlbumFocus) {
            result = searchMusicByAlbum(album);
        }

        if (result == null) {
            // No focus found, search by query for song title
            result = searchMusicBySongTitle(query);
        }

        if (result != null && !result.isEmpty()) {
            // Immediately start playing from the beginning of the search results
            // Implement your logic to start playing music
            playMusic(result);
        } else {
            // Handle no queue found. Stop playing if the app
            // is currently playing a song
        }
    }
}
