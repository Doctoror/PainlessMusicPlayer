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

    private final SearchUtils mSearchUtils;

    MediaSessionCallback(@NonNull final Context context) {
        mContext = context.getApplicationContext();
        mSearchUtils = new SearchUtils(context);
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
        Observable.create(s -> mSearchUtils.onPlayFromSearch(query, extras))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }

    @Override
    public void onPlayFromMediaId(final String mediaId, final Bundle extras) {
        Observable.create(s -> mSearchUtils.onPlayFromMediaId(mediaId))
                .subscribeOn(Schedulers.computation())
                .subscribe();
    }
}
