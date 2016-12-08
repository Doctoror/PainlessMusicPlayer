package com.doctoror.fuckoffmusicplayer.playback;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

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
        SearchUtils.onPlayFromSearch(mContext, query, extras);
    }
}
