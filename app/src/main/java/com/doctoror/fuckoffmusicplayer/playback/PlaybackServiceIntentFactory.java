/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    static Intent intentPlay(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PLAY);
    }

    @NonNull
    public static Intent intentPlayAnything(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PLAY_ANYTHING);
    }

    @NonNull
    static Intent intentPause(@NonNull final Context context) {
        return intentAction(context, PlaybackService.ACTION_PAUSE);
    }

    @NonNull
    static Intent intentStop(@NonNull final Context context) {
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
    static Intent intentResendState() {
        return new Intent(PlaybackService.ACTION_RESEND_STATE);
    }

    @NonNull
    static Intent intentStopWithError(@NonNull final Context context,
            @NonNull final CharSequence errorMessage) {
        final Intent intent = intentAction(context, PlaybackService.ACTION_STOP_WITH_ERROR);
        intent.putExtra(PlaybackService.EXTRA_ERROR_MESSAGE, errorMessage);
        return intent;
    }

    @NonNull
    static Intent intentSeek(@NonNull final Context context,
            final float positionPercent) {
        final Intent intent = intentAction(context, PlaybackService.ACTION_SEEK);
        intent.putExtra(PlaybackService.EXTRA_POSITION_PERCENT, positionPercent);
        return intent;
    }

    @NonNull
    static Intent intentPlayMediaFromQueue(@NonNull final Context context,
            final long mediaId) {
        final Intent intent = intentAction(context, PlaybackService.ACTION_PLAY_MEDIA_FROM_QUEUE);
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
