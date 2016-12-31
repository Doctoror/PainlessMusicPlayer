package com.doctoror.fuckoffmusicplayer.reporter;

import com.google.android.gms.common.api.GoogleApiClient;

import com.bumptech.glide.RequestManager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * {@link PlaybackReporter} factory
 */
public final class PlaybackReporterFactory {

    private PlaybackReporterFactory() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static PlaybackReporter newUniversalReporter(@NonNull final Context context,
            @NonNull final GoogleApiClient wearApiClient,
            @NonNull final MediaSessionCompat mediaSession,
            @NonNull final RequestManager glide) {
        return new PlaybackReporterSet(
                new MediaSessionPlaybackReporter(context, mediaSession, glide),
                new WearableMediaPlaybackReporter(context, wearApiClient, glide),
                new ScrobbleDroidPlaybackReporter(context));
    }

    @NonNull
    public static PlaybackReporter newMediaSessionReporter(@NonNull final Context context,
            @NonNull final MediaSessionCompat mediaSession,
            @NonNull final RequestManager glide) {
        return new MediaSessionPlaybackReporter(context, mediaSession, glide);
    }
}
