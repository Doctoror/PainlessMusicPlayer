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
package com.doctoror.fuckoffmusicplayer.data.reporter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;

/**
 * Reports to multiple {@link PlaybackReporter}s
 */
final class PlaybackReporterSet implements PlaybackReporter {

    @NonNull
    private final PlaybackReporter[] mReporters;

    PlaybackReporterSet(@NonNull final PlaybackReporter... reporters) {
        mReporters = reporters;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportTrackChanged(media);
        }
    }

    @Override
    public void reportPlaybackStateChanged(@NonNull final PlaybackState state,
                                           @Nullable final CharSequence errorMessage) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportPlaybackStateChanged(state, errorMessage);
        }
    }
}
