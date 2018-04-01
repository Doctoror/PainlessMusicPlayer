/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.domain.reporter

import android.support.annotation.WorkerThread
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.queue.Media

/**
 * Media Reporter
 */
interface PlaybackReporter {

    @WorkerThread
    fun reportTrackChanged(media: Media, positionInQueue: Int)

    @WorkerThread
    fun reportPlaybackStateChanged(
            state: PlaybackState,
            errorMessage: CharSequence?)

    @WorkerThread
    fun reportPositionChanged(mediaId: Long, position: Long)

    @WorkerThread
    fun reportQueueChanged(queue: List<Media>?)

    fun onDestroy()
}
