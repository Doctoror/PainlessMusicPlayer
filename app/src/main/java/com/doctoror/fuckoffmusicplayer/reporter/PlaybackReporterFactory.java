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
                new LastFmPlaybackReporter(context, currentMedia));
    }

    @NonNull
    public static PlaybackReporter newMediaSessionReporter(@NonNull final Context context,
            @NonNull final MediaSessionCompat mediaSession) {
        return new MediaSessionPlaybackReporter(context, mediaSession);
    }


}
