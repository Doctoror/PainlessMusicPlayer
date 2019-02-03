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

import android.net.Uri
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import java.lang.IllegalArgumentException

class PlaybackServiceUnitPlayMediaFromQueueTest {

    private val currentMediaProvider: CurrentMediaProvider = mock()
    private val mediaPlayer: MediaPlayer = mock()
    private val playbackData: PlaybackData = mock()
    private val unitAudioFocus: PlaybackServiceUnitAudioFocus = mock()
    private val unitReporter: PlaybackServiceUnitReporter = mock()

    private val underTest = PlaybackServiceUnitPlayMediaFromQueue(
            currentMediaProvider, mediaPlayer, playbackData, unitAudioFocus, unitReporter)

    private fun givenPlayRequestedForCurrentMedia(media: Media = Media()) {
        val queue = listOf(media)
        val queuePosition = 0

        whenever(currentMediaProvider.currentMedia).thenReturn(media)
        whenever(playbackData.queue).thenReturn(queue)
        whenever(playbackData.queuePosition).thenReturn(queuePosition)

        underTest.play(queue, queuePosition)
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwsForNullQueue() {
        // When
        underTest.play(null, 0)

        // Then IllegalArgumentException is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwsForEmptyQueue() {
        // When
        underTest.play(emptyList(), 0)

        // Then IllegalArgumentException is thrown
    }

    @Test
    fun continuesPlaybackForTheSameMediaWhenPausedAndFocusGranted() {
        // Given
        whenever(unitAudioFocus.focusGranted).thenReturn(true)
        whenever(playbackData.playbackState).thenReturn(PlaybackState.STATE_PAUSED)
        givenPlayRequestedForCurrentMedia()

        // Then
        verify(unitAudioFocus).requestAudioFocus()
        verify(mediaPlayer).play()
        verify(unitReporter).reportCurrentMedia()
    }

    @Test
    fun requestsFocusWhenSameMediaIsPaused() {
        // Given
        whenever(unitAudioFocus.focusGranted).thenReturn(false)
        whenever(playbackData.playbackState).thenReturn(PlaybackState.STATE_PAUSED)
        givenPlayRequestedForCurrentMedia()

        // Then
        verify(unitAudioFocus).requestAudioFocus()

        verify(mediaPlayer, never()).play()
        verify(unitReporter, never()).reportCurrentMedia()
    }

    @Test
    fun changesMediaAndRequestsFocus() {
        // Given
        val targetMedia = Media()
        val queue = listOf(targetMedia)
        val queuePosition = 0

        // When
        underTest.play(queue, queuePosition)

        // Then
        verify(mediaPlayer).stop()
        verify(playbackData).setPlayQueue(queue)
        verify(playbackData).setPlayQueuePosition(queuePosition)
        verify(playbackData).setMediaPosition(0L)
        verify(playbackData).persistAsync()
        verify(unitAudioFocus).requestAudioFocus()
    }

    @Test
    fun changesMediaAndPlaysWhenFocusGranted() {
        // Given
        whenever(unitAudioFocus.focusGranted).thenReturn(true)

        val mediaUri: Uri = mock()
        val targetMedia = Media(data = mediaUri)

        // When
        underTest.play(listOf(targetMedia), 0)

        // Then
        verify(mediaPlayer).load(mediaUri)
        verify(mediaPlayer).play()
        verify(unitReporter).reportCurrentMedia()
    }

    @Test
    fun continuesWhereStopped() {
        // Given
        val mediaPosition = 128L
        whenever(playbackData.playbackState).thenReturn(PlaybackState.STATE_IDLE)
        whenever(playbackData.mediaPosition).thenReturn(mediaPosition)
        whenever(unitAudioFocus.focusGranted).thenReturn(true)

        givenPlayRequestedForCurrentMedia(Media(
                duration = mediaPosition + underTest.remainingDurationForPlayWhereStopped,
                data = mock()))

        // Then
        verify(mediaPlayer).seekTo(mediaPosition)
        verify(mediaPlayer).play()
    }

    @Test
    fun doesNotContinueWhereStoppedWhenNotEnoughRemainingDuration() {
        // Given
        val mediaPosition = 128L
        whenever(playbackData.playbackState).thenReturn(PlaybackState.STATE_IDLE)
        whenever(playbackData.mediaPosition).thenReturn(mediaPosition)
        whenever(unitAudioFocus.focusGranted).thenReturn(true)

        givenPlayRequestedForCurrentMedia(Media(
                duration = mediaPosition + underTest.remainingDurationForPlayWhereStopped - 1,
                data = mock()))

        // Then
        verify(mediaPlayer, never()).seekTo(any())
        verify(mediaPlayer).play()
    }
}
