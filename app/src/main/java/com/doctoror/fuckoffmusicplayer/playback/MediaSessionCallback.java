package com.doctoror.fuckoffmusicplayer.playback;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

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
}
