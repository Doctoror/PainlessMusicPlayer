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
package com.doctoror.fuckoffmusicplayer.domain.playback

import com.doctoror.fuckoffmusicplayer.domain.queue.Media

/**
 * Playback service control
 */
interface PlaybackServiceControl {

    fun resendState()

    fun playPause()

    fun play(queue: List<Media>, position: Int)

    fun playAnything()

    fun pause()

    fun stop()

    fun stopWithError(errorMessage: CharSequence)

    fun prev()

    fun next()

    fun seek(positionPercent: Float)
}
