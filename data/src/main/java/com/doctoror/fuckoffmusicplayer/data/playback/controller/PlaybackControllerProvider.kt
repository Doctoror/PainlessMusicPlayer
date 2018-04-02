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
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams

class PlaybackControllerProvider(
        private val playbackData: PlaybackData,
        private val playbackParams: PlaybackParams,
        private val psUnitPlayMediaFromQueue: PlaybackServiceUnitPlayMediaFromQueue,
        private val stopAction: Runnable) {

    private var playbackController: PlaybackController? = null

    /**
     * Returns Playback controller based on [PlaybackParams].
     *
     * Users should obtain a controller before any operation to ensure the correct controller is used based on
     * current [PlaybackParams] state.
     *
     * This value is cached and refreshed each time a new invocation is called for different [PlaybackParams].
     */
    fun obtain(): PlaybackController {
        var created = false
        var returnValue: PlaybackController? = playbackController
        if (returnValue == null) {
            returnValue = if (playbackParams.isShuffleEnabled)
                newPlaybackControllerShuffle()
            else
                newPlaybackControllerNormal()
            created = true
        } else if (playbackParams.isShuffleEnabled) {
            if (PlaybackControllerShuffle::class.java != returnValue.javaClass) {
                returnValue = newPlaybackControllerShuffle()
                created = true
            }
        } else {
            if (PlaybackControllerNormal::class.java != returnValue.javaClass) {
                returnValue = newPlaybackControllerNormal()
                created = true
            }
        }
        if (created) {
            returnValue.setQueue(playbackData.queue)
            returnValue.setPositionInQueue(playbackData.queuePosition)
        }

        playbackController = returnValue
        return returnValue
    }

    private fun newPlaybackControllerNormal(): PlaybackController = PlaybackControllerNormal(
            playbackParams,
            psUnitPlayMediaFromQueue,
            stopAction)

    private fun newPlaybackControllerShuffle(): PlaybackController = PlaybackControllerShuffle(
            playbackParams,
            psUnitPlayMediaFromQueue,
            stopAction)
}
