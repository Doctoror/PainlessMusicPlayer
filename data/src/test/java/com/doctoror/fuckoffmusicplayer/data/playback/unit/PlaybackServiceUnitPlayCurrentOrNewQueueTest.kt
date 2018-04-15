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

import com.doctoror.commons.reactivex.TestSchedulersProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import org.junit.Test

class PlaybackServiceUnitPlayCurrentOrNewQueueTest {

    private val playbackData: PlaybackData = mock()
    private val playbackInitializer: PlaybackInitializer = mock()
    private val psUnitPlayMediaFromQueue: PlaybackServiceUnitPlayMediaFromQueue = mock()
    private val queueProviderRecentlyScanned: QueueProviderRecentlyScanned = mock()

    private val underTest = PlaybackServiceUnitPlayCurrentOrNewQueue(
            playbackData,
            playbackInitializer,
            psUnitPlayMediaFromQueue,
            queueProviderRecentlyScanned,
            TestSchedulersProvider())

    @Test
    fun playsMediaFromQueueWhenQueueIsSet() {
        // Given
        val queue = listOf(Media(), Media())
        val queuePosition = 1
        whenever(playbackData.queue).thenReturn(queue)
        whenever(playbackData.queuePosition).thenReturn(queuePosition)

        // When
        underTest.playCurrentOrNewQueue()

        // Then
        verify(psUnitPlayMediaFromQueue).play(queue, queuePosition)
    }

    @Test
    fun playsRecentlyScannedQueueWhenQueueNotSet() {
        // Given
        val queue = listOf(Media(), Media())
        whenever(queueProviderRecentlyScanned.recentlyScannedQueue())
                .thenReturn(Observable.just(queue))

        // When
        underTest.playCurrentOrNewQueue()

        // Then
        verify(playbackInitializer).setQueueAndPlay(queue, 0)
    }

    @Test
    fun doesNotPlayEmptyRecentlyScannedQueue() {
        // Given
        val queue = emptyList<Media>()
        whenever(queueProviderRecentlyScanned.recentlyScannedQueue())
                .thenReturn(Observable.just(queue))

        // When
        underTest.playCurrentOrNewQueue()

        // Then
        verify(playbackInitializer, never()).setQueueAndPlay(any(), any())
    }
}
