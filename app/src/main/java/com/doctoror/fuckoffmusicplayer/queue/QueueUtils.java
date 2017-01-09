/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.queue;

import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Playlist utils
 */
public final class QueueUtils {

    private QueueUtils() {
        throw new UnsupportedOperationException();
    }

    public static void play(@NonNull final Context context,
            @NonNull final PlaybackData playbackData,
            @NonNull final List<Media> mediaList) {
        if (mediaList.isEmpty()) {
            throw new IllegalArgumentException("Will not play empty playlist");
        }
        play(context, playbackData, mediaList, 0);
    }

    public static void play(@NonNull final Context context,
            @NonNull final PlaybackData playbackData,
            @NonNull final List<Media> mediaList,
            final int position) {
        playbackData.setPlayQueue(mediaList);
        playbackData.setPlayQueuePosition(position);
        playbackData.setMediaPosition(0);
        playbackData.persistAsync();

        PlaybackServiceControl.play(context);
    }

}
