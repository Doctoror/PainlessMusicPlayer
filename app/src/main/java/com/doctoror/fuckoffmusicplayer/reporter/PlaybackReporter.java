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

import com.doctoror.fuckoffmusicplayer.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Media Reporter
 */
public interface PlaybackReporter {

    void onDestroy();

    @WorkerThread
    void reportTrackChanged(@NonNull Media media, int positionInQueue);

    @WorkerThread
    void reportPlaybackStateChanged(
            @PlaybackState.State int state,
            @Nullable CharSequence errorMessage);

    @WorkerThread
    void reportPositionChanged(long mediaId, long position);

    @WorkerThread
    void reportQueueChanged(@Nullable List<Media> queue);
}
