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
package com.doctoror.fuckoffmusicplayer.data.playback.unit

import com.doctoror.commons.reactivex.SchedulersProvider
import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned

class PlaybackServiceUnitPlayCurrentOrNewQueue(
    private val playbackData: PlaybackData,
    private val playbackInitializer: PlaybackInitializer,
    private val psUnitPlayMediaFromQueue: PlaybackServiceUnitPlayMediaFromQueue,
    private val queueProviderRecentlyScanned: QueueProviderRecentlyScanned,
    private val schedulersProvider: SchedulersProvider
) {

    private val tag = "PlayCurrentOrNewQueueUseCase"

    fun playCurrentOrNewQueue() {
        val queue = playbackData.queue
        if (queue != null && !queue.isEmpty()) {
            psUnitPlayMediaFromQueue.play(queue, playbackData.queuePosition)
        } else {
            queueProviderRecentlyScanned.recentlyScannedQueue()
                .take(1)
                .subscribeOn(schedulersProvider.io())
                .observeOn(schedulersProvider.mainThread())
                .subscribe(
                    { q ->
                        if (q.isEmpty()) {
                            Log.w(tag, "Recently scanned queue is empty")
                        } else {
                            playbackInitializer.setQueueAndPlay(q, 0)
                        }
                    },
                    { t -> Log.w(tag, "Failed to load recently scanned", t) })
        }
    }
}
