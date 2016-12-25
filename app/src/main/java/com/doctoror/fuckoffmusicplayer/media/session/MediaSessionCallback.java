package com.doctoror.fuckoffmusicplayer.media.session;

import com.doctoror.fuckoffmusicplayer.media.browser.SearchUtils;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

import rx.Observable;
import rx.schedulers.Schedulers;

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
        PlaybackServiceControl.play(mContext);
    }

    @Override
    public void onStop() {
        PlaybackServiceControl.stop(mContext);
    }

    @Override
    public void onPause() {
        PlaybackServiceControl.pause(mContext);
    }

    @Override
    public void onSkipToPrevious() {
        PlaybackServiceControl.prev(mContext);
    }

    @Override
    public void onSkipToNext() {
        PlaybackServiceControl.next(mContext);
    }

    @Override
    public void onPlayFromSearch(final String query, final Bundle extras) {
        Observable.create(s -> SearchUtils.onPlayFromSearch(mContext, query, extras))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void onPlayFromMediaId(final String mediaId, final Bundle extras) {
        Observable.create(s -> SearchUtils.onPlayFromMediaId(mContext, mediaId))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }
}
