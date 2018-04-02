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
package com.doctoror.fuckoffmusicplayer.data.playback.controller

import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitPlayMediaFromQueue
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams
import com.doctoror.fuckoffmusicplayer.domain.playback.RepeatMode
import com.doctoror.fuckoffmusicplayer.domain.queue.Media

open class PlaybackControllerNormal(
        private val playbackParams: PlaybackParams,
        private val playMediaFromQueueUseCase: PlaybackServiceUnitPlayMediaFromQueue,
        private val stopAction: Runnable) : PlaybackController {

    protected val lock = Any()

    private var queue: List<Media>? = null
    private var position: Int = 0

    override fun playPrev() {
        synchronized(lock) {
            val localQueue = queue
            if (localQueue != null && !localQueue.isEmpty()) {
                when (playbackParams.repeatMode) {
                    RepeatMode.NONE -> onPlay(localQueue, prevPos(localQueue, position))
                    RepeatMode.QUEUE -> onPlay(localQueue, prevPos(localQueue, position))
                    RepeatMode.TRACK -> onPlay(localQueue, position)
                }
            }
        }
    }

    override fun playNext(isUserAction: Boolean) {
        synchronized(lock) {
            val localQueue = queue
            if (localQueue != null && !localQueue.isEmpty()) {
                when (playbackParams.repeatMode) {
                    RepeatMode.NONE -> if (!isUserAction && position == localQueue.size - 1) {
                        stopAction.run()
                    } else {
                        onPlay(localQueue, nextPos(localQueue, position))
                    }

                    RepeatMode.QUEUE -> onPlay(localQueue, nextPos(localQueue, position))

                    RepeatMode.TRACK -> if (isUserAction) {
                        onPlay(localQueue, nextPos(localQueue, position))
                    } else {
                        onPlay(localQueue, position)
                    }
                }
            }
        }
    }

    private fun onPlay(list: List<Media>?, position: Int) {
        this.position = position
        play(list, position)
    }

    protected open fun play(list: List<Media>?, position: Int) {
        this.position = playMediaFromQueueUseCase.play(list, position, false)
    }

    override fun setQueue(queue: List<Media>?) {
        synchronized(lock) {
            this.queue = queue
        }
    }

    override fun setPositionInQueue(position: Int) {
        synchronized(lock) {
            this.position = position
        }
    }

    private fun prevPos(queue: List<Media>, position: Int) = if (position - 1 < 0) {
        queue.size - 1
    } else {
        position - 1
    }

    private fun nextPos(queue: List<Media>, position: Int) = if (position + 1 >= queue.size) {
        0
    } else {
        position + 1
    }
}
