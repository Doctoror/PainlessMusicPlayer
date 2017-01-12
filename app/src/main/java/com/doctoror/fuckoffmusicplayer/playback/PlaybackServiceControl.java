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
