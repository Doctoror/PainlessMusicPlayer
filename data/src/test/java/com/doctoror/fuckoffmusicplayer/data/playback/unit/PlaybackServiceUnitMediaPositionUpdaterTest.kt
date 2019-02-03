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
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer
import com.nhaarman.mockitokotlin2.*
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.Test

class PlaybackServiceUnitMediaPositionUpdaterTest {

    private val mediaPlayer: MediaPlayer = mock()
    private val playbackData: PlaybackData = mock()
    private val schedulersProvider = object : TestSchedulersProvider() {

        /*
          * Observable.interval causes trampoline to loop forever, thus computation is used.
          */
        override fun computation() = Schedulers.computation()
    }

    private val underTest = PlaybackServiceUnitMediaPositionUpdater(
            mediaPlayer, playbackData, schedulersProvider)

    @Test
    fun subscribesOnInitialize() {
        // Given
        assertNull(underTest.positionUpdater)

        // When
        underTest.initializeMediaPositionUpdater()

        // Then
        assertNotNull(underTest.positionUpdater)
        assertFalse(underTest.positionUpdater!!.isDisposed)
    }

    @Test
    fun disposesOnDispose() {
        // Given
        underTest.initializeMediaPositionUpdater()

        // When
        val positionUpdater = underTest.positionUpdater
        underTest.disposeMediaPositionUpdater()

        // Then
        assertTrue(positionUpdater!!.isDisposed)
        assertNull(underTest.positionUpdater)
    }

    @Test
    fun disposesOnDestroy() {
        // Given
        underTest.initializeMediaPositionUpdater()

        // When
        val positionUpdater = underTest.positionUpdater
        underTest.onDestroy()

        // Then
        assertTrue(positionUpdater!!.isDisposed)
        assertNull(underTest.positionUpdater)
    }

    // Cannot test Interval with Trampoline, thus direct side-effect must be tested.

    @Test
    fun doesNotUpdateMediaPositionWhenNotPlaying() {
        // When
        underTest.updateMediaPosition()

        // Then
        verify(playbackData, never()).setMediaPosition(any())
    }

    @Test
    fun updatesMediaPositionWhenPlaying() {
        // Given
        whenever(playbackData.playbackState).thenReturn(PlaybackState.STATE_PLAYING)

        val position = 666L
        whenever(mediaPlayer.getCurrentPosition()).thenReturn(position)

        // When
        underTest.updateMediaPosition()

        // Then
        verify(playbackData).setMediaPosition(position)
    }
}
