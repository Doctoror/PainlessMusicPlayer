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
package com.doctoror.fuckoffmusicplayer.data.playback.usecase

import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackController
import com.doctoror.fuckoffmusicplayer.data.util.Log
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned
import io.reactivex.schedulers.Schedulers

class PlayCurrentOrNewQueueUseCase(
        private val playbackControllerSource: () -> PlaybackController,
        private val playbackData: PlaybackData,
        private val playbackInitializer: PlaybackInitializer,
        private val playMedaFromQueueUseCase: PlayMediaFromQueueUseCase,
        private val queueProviderRecentlyScanned: QueueProviderRecentlyScanned) {

    fun playCurrentOrNewQueue() {
        val queue = playbackData.queue
        if (queue != null && !queue.isEmpty()) {
            val position = playMedaFromQueueUseCase.play(
                    queue,
                    playbackData.queuePosition,
                    true)
            playbackControllerSource().setPositionInQueue(position)
        } else {
            queueProviderRecentlyScanned.recentlyScannedQueue()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            { q -> playbackInitializer.setQueueAndPlay(q, 0) },
                            { t -> Log.w(TAG, "Failed to load recently scanned", t) })
        }
    }

    companion object {
        private const val TAG = "PlayCurrentOrNewQueueUseCase"
    }
}
