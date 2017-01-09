package com.doctoror.fuckoffmusicplayer.playback;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Playback service control
 */
public final class PlaybackServiceControl {

    private PlaybackServiceControl() {
        throw new UnsupportedOperationException();
    }

    public static void resendState(@NonNull final Context context) {
        context.sendBroadcast(PlaybackServiceIntentFactory.intentResendState());
    }

    public static void playPause(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentPlayPause(context));
    }

    public static void play(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentPlay(context));
    }

    public static void playAnything(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentPlayAnything(context));
    }

    public static void pause(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentPause(context));
    }

    public static void stop(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentStop(context));
    }

    public static void stopWithError(@NonNull final Context context,
            @NonNull final String errorMessage) {
        context.startService(
                PlaybackServiceIntentFactory.intentStopWithError(context, errorMessage));
    }

    public static void prev(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentPrev(context));
    }

    public static void next(@NonNull final Context context) {
        context.startService(PlaybackServiceIntentFactory.intentNext(context));
    }

    public static void seek(@NonNull final Context context,
            final float positionPercent) {
        context.startService(PlaybackServiceIntentFactory.intentSeek(context, positionPercent));
    }

    public static void playMediaFromQueue(@NonNull final Context context,
            final long mediaId) {
        context.startService(PlaybackServiceIntentFactory
                .intentPlayMediaFromQueue(context, mediaId));
    }
}
