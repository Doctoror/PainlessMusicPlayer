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
package com.doctoror.fuckoffmusicplayer.domain.queue.usecase

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer
import com.doctoror.fuckoffmusicplayer.domain.queue.Media

class RemoveMediasFromCurrentQueueUseCase(
    private val playbackData: PlaybackData,
    private val playbackInitializer: PlaybackInitializer
) {

    fun removeMediasFromCurrentQueue(vararg mediaIds: Long) {
        val queue = playbackData.queue?.toMutableList()
        val currentMediaPosition = playbackData.queuePosition
        val currentMedia = if (queue != null && currentMediaPosition < queue.size) {
            queue[currentMediaPosition]
        } else {
            null
        }

        var modified = false
        if (queue != null) {
            for (id in mediaIds) {
                modified = modified or removeFromQueue(queue, id)
            }
        }

        if (modified) {
            playbackData.setPlayQueue(queue)
            if (currentMedia != null) {
                val newMediaPosition = queue!!.indexOf(currentMedia)
                if (newMediaPosition == -1) {
                    if (playbackData.playbackState == PlaybackState.STATE_PLAYING) {
                        playbackInitializer.setQueueAndPlay(queue, 0)
                    }
                } else if (newMediaPosition != currentMediaPosition) {
                    playbackData.setPlayQueuePosition(newMediaPosition)
                }
            }
        }
    }

    private fun removeFromQueue(queue: MutableList<Media>, id: Long): Boolean {
        val i = queue.iterator()
        while (i.hasNext()) {
            if (i.next().id == id) {
                i.remove()
                return true
            }
        }
        return false
    }
}
