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

import io.reactivex.Observable

/**
 * Current playback data
 */
interface PlaybackData {

    val queue: List<Media>?

    val queuePosition: Int

    val mediaPosition: Long

    var playbackState: PlaybackState

    fun queueObservable(): Observable<List<Media>>

    fun queuePositionObservable(): Observable<Int>

    fun mediaPositionObservable(): Observable<Long>

    fun playbackStateObservable(): Observable<PlaybackState>

    fun setPlayQueue(queue: List<Media>?)

    fun setPlayQueuePosition(position: Int)

    fun setMediaPosition(position: Long)

    fun persistAsync()

}
