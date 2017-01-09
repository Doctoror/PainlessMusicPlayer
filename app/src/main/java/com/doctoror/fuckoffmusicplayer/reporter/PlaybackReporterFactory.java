package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.wear.WearableMediaPlaybackReporter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * {@link PlaybackReporter} factory
 */
public final class PlaybackReporterFactory {

    private PlaybackReporterFactory() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static PlaybackReporter newUniversalReporter(
            @NonNull final Context context,
            @NonNull final MediaSessionCompat mediaSession,
            @Nullable final Media currentMedia) {
        return new PlaybackReporterSet(
                new MediaSessionPlaybackReporter(context, mediaSession),
                new WearableMediaPlaybackReporter(context),
                new ScrobbleDroidPlaybackReporter(context, currentMedia));
    }

    @NonNull
    public static PlaybackReporter newMediaSessionReporter(@NonNull final Context context,
            @NonNull final MediaSessionCompat mediaSession) {
        return new MediaSessionPlaybackReporter(context, mediaSession);
    }


}
