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

import android.util.SparseIntArray
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitPlayMediaFromQueue
import com.doctoror.fuckoffmusicplayer.data.util.RandomHolder
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import java.util.*

class PlaybackControllerShuffle(
        playbackParams: PlaybackParams,
        playMediaFromQueueUseCase: PlaybackServiceUnitPlayMediaFromQueue,
        stopAction: Runnable) : PlaybackControllerNormal(
        playbackParams, playMediaFromQueueUseCase, stopAction) {

    private val shuffledPositions = SparseIntArray()

    override fun setQueue(queue: List<Media>?) {
        synchronized(lock) {
            rebuildShuffledPositions(queue?.size ?: 0)
        }
        super.setQueue(queue)
    }

    override fun play(list: List<Media>?, position: Int) {
        var shuffledPosition = 0
        synchronized(lock) {
            shuffledPosition = shuffledPositions.get(position)
        }
        super.play(list, shuffledPosition)
    }

    private fun rebuildShuffledPositions(size: Int) {
        shuffledPositions.clear()
        if (size != 0) {
            val positions = ArrayList<Int>(size)
            for (i in 0 until size) {
                positions.add(i)
            }

            positions.shuffle(RandomHolder.getInstance().random)

            for (i in 0 until size) {
                shuffledPositions.put(i, positions[i])
            }
        }
    }
}
