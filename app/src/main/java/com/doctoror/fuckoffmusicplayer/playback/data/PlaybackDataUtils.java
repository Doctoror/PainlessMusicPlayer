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
package com.doctoror.fuckoffmusicplayer.playback.data;

import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.util.CollectionUtils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * {@link PlaybackData} utils
 */
public final class PlaybackDataUtils {

    private PlaybackDataUtils() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static Media getCurrentMedia(@NonNull final PlaybackData playbackData) {
        return CollectionUtils
                .getItemSafe(playbackData.getQueue(), playbackData.getQueuePosition());
    }
}
