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
package com.doctoror.fuckoffmusicplayer.data.playback.usecase

import android.support.v4.media.session.MediaSessionCompat
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitReporter
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_ERROR
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test

class PlaybackServiceUnitReporterTest {

    private val mediaSession: MediaSessionCompat = mock()
    private val playbackReporter: PlaybackReporter = mock()

    private val currentMediaProvider: CurrentMediaProvider = mock()
    private val mediaSessionHolder: MediaSessionHolder = mock()
    private val playbackReporterFactory: PlaybackReporterFactory = mock()

    private val underTest = PlaybackServiceUnitReporter(
            currentMediaProvider, mediaSessionHolder, playbackReporterFactory)

    @Before
    fun setup() {
        whenever(mediaSessionHolder.mediaSession)
                .thenReturn(mediaSession)

        whenever(playbackReporterFactory.newUniversalReporter(mediaSession))
                .thenReturn(playbackReporter)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsWhenNoMediaSession() {
        // Given
        whenever(mediaSessionHolder.mediaSession).thenReturn(null)

        // When
        underTest.onCreate()

        // Then Exception is expected
    }

    @Test
    fun reportsCurrentMedia() {
        // Given
        val media = Media()
        whenever(currentMediaProvider.currentMedia).thenReturn(media)

        underTest.onCreate()

        // When
        underTest.reportCurrentMedia()

        // Then
        verify(playbackReporter).reportTrackChanged(media)
    }

    @Test
    fun doesNotReportCurrentMediaWhenNotCreated() {
        // When
        underTest.reportCurrentMedia()

        // Then
        verify(playbackReporter, never()).reportTrackChanged(any())
    }

    @Test
    fun reportCurrentMediaSkipsWhenMediaIsNull() {
        // Given
        underTest.onCreate()

        // When
        underTest.reportCurrentMedia()

        // Then
        verify(playbackReporter, never()).reportTrackChanged(any())
    }

    @Test
    fun reportsPlaybackState() {
        // Given
        val state = STATE_ERROR
        val errorMessage = "SHIT"
        underTest.onCreate()

        // When
        underTest.reportPlaybackState(state, errorMessage)

        // Then
        verify(playbackReporter).reportPlaybackStateChanged(state, errorMessage)
    }

    @Test
    fun doesNotReportPlaybackStateWhenNotCreated() {
        // When
        underTest.reportPlaybackState(STATE_ERROR, "SHIT")

        // Then
        verify(playbackReporter, never()).reportPlaybackStateChanged(any(), any())
    }
}
