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

import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackController
import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackControllerProvider
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test

class PlaybackServiceUnitQueueMonitorTest {

    private val queueSubject = PublishSubject.create<List<Media>>()
    private val queuePositionSubject = PublishSubject.create<Int>()

    private val playbackController: PlaybackController = mock()

    private val albumThumbHolder: AlbumThumbHolder = mock()
    private val currentMediaProvider: CurrentMediaProvider = mock()
    private val playbackControllerProvider: PlaybackControllerProvider = mock()
    private val playbackData: PlaybackData = mock()
    private val restartAction: Runnable = mock()
    private val stopAction: Runnable = mock()

    private val underTest = PlaybackServiceUnitQueueMonitor(
            albumThumbHolder,
            currentMediaProvider,
            playbackControllerProvider,
            playbackData,
            restartAction,
            stopAction)

    @Before
    fun setup() {
        whenever(playbackControllerProvider.obtain()).thenReturn(playbackController)
        whenever(playbackData.queueObservable()).thenReturn(queueSubject)
        whenever(playbackData.queuePositionObservable()).thenReturn(queuePositionSubject)
    }

    @After
    fun tearDown() {
        underTest.onDestroy()
    }

    @Test
    fun invokesStopActionAndRemovesAlbumThumbOnEmptyQueue() {
        // Given
        underTest.onCreate()

        // When
        queueSubject.onNext(emptyList())

        // Then
        verify(albumThumbHolder).albumThumb = null
        verify(stopAction).run()
    }

    @Test
    fun setsPlaybackControllerQueueOnEmit() {
        // Given
        underTest.onCreate()

        val queue = listOf(Media())

        // When
        queueSubject.onNext(queue)

        // Then
        verify(playbackController).setQueue(queue)
    }

    @Test
    fun restartsWhenTrackIsNoLongerInTheQueue() {
        // Given
        val currentMedia = Media()

        whenever(playbackData.playbackState).thenReturn(PlaybackState.STATE_PLAYING)
        whenever(currentMediaProvider.currentMedia).thenReturn(currentMedia)
        underTest.onCreate()

        // When

        // Emit some queue and position
        queueSubject.onNext(listOf(currentMedia))
        queuePositionSubject.onNext(0)

        // Emit queue where the current media no longer exists
        queueSubject.onNext(listOf(Media(id = 1)))

        // Then
        verify(restartAction).run()
    }
}
