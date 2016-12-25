package com.doctoror.fuckoffmusicplayer.playback;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Intent facory for playback service
 */
public final class PlaybackServiceIntentFactory {

    private PlaybackServiceIntentFactory() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static Intent intentPlayPause(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PLAY_PAUSE);
    }

    @NonNull
    public static Intent intentPlay(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PLAY);
    }

    @NonNull
    public static Intent intentPlayAnything(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PLAY_ANYTHING);
    }

    @NonNull
    public static Intent intentPause(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PAUSE);
    }

    @NonNull
    public static Intent intentStop(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_STOP);
    }

    @NonNull
    public static Intent intentPrev(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PREV);
    }

    @NonNull
    public static Intent intentNext(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_NEXT);
    }

    @NonNull
    public static Intent intentResendState(@NonNull final Context context) {
        return new Intent(PlaybackService.ACTION_RESEND_STATE);
    }

    @NonNull
    public static Intent intentStopWithError(@NonNull final Context context,
            @NonNull final String errorMessage) {
        final Intent intent = intentAction(context, PlaybackService.ACTION_STOP_WITH_ERROR);
        intent.putExtra(PlaybackService.EXTRA_ERROR_MESSAGE, errorMessage);
        return intent;
    }

    @NonNull
    public static Intent intentSeek(@NonNull final Context context,
            final float positionPercent) {
        final Intent intent = intentAction(context, PlaybackService.ACTION_SEEK);
        intent.putExtra(PlaybackService.EXTRA_POSITION_PERCENT, positionPercent);
        return intent;
    }

    @NonNull
    public static Intent intentPlayMediaFromPlaylist(@NonNull final Context context,
            final long mediaId) {
        final Intent intent = intentAction(context, PlaybackService.ACTION_PLAY_MEDIA_FROM_PLAYLIST);
        intent.putExtra(PlaybackService.EXTRA_MEDIA_ID, mediaId);
        return intent;
    }

    @NonNull
    private static Intent intentAction(@NonNull final Context context,
            @NonNull final String action) {
        final Intent intent = new Intent(context, PlaybackService.class);
        intent.setAction(action);
        return intent;
    }

}
