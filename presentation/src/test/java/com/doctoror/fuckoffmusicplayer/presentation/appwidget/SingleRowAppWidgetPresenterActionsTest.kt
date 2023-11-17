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
package com.doctoror.fuckoffmusicplayer.presentation.appwidget

import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.presentation.home.HomeActivity
import com.doctoror.fuckoffmusicplayer.presentation.nowplaying.NowPlayingActivity
import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackAndroidService
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class SingleRowAppWidgetPresenterActionsTest {

    private val albumThumbHolder: AlbumThumbHolder = mock()
    private val currentMediaProvider: CurrentMediaProvider = mock()
    private val viewModel = SingleRowAppWidgetViewModel()

    private val underTest = SingleRowAppWidgetPresenter(
            albumThumbHolder, currentMediaProvider, viewModel)

    @Test
    fun coverActionLeadsToHomeWhenMediaNotSet() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.coverAction)
        assertEquals(HomeActivity::class.java.name, shadow.savedIntent.component!!.className)
    }

    @Test
    fun coverActionLeadsToNowPlayingWhenMediaSet() {
        // Given
        whenever(currentMediaProvider.currentMedia).thenReturn(Media())

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.coverAction)
        assertEquals(NowPlayingActivity::class.java.name, shadow.savedIntent.component!!.className)
    }

    @Test
    fun playPauseActionLeadsToPlayAnythingWhenMediaNotSet() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.playPauseAction)
        assertEquals(PlaybackAndroidService.ACTION_PLAY_ANYTHING, shadow.savedIntent.action)
    }

    @Test
    fun playPauseActionLeadsToPlayPauseWhenMediaSet() {
        // Given
        whenever(currentMediaProvider.currentMedia).thenReturn(Media())

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.playPauseAction)
        assertEquals(PlaybackAndroidService.ACTION_PLAY_PAUSE, shadow.savedIntent.action)
    }

    @Test
    fun nextActionLeadsToPlayAnythingWhenMediaNotSet() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.nextAction)
        assertEquals(PlaybackAndroidService.ACTION_PLAY_ANYTHING, shadow.savedIntent.action)
    }

    @Test
    fun nextActionLeadsToNextWhenMediaSet() {
        // Given
        whenever(currentMediaProvider.currentMedia).thenReturn(Media())

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.nextAction)
        assertEquals(PlaybackAndroidService.ACTION_NEXT, shadow.savedIntent.action)
    }

    @Test
    fun prevActionLeadsToPlayAnythingWhenMediaNotSet() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.prevAction)
        assertEquals(PlaybackAndroidService.ACTION_PLAY_ANYTHING, shadow.savedIntent.action)
    }

    @Test
    fun prevActionLeadsToPrevWhenMediaSet() {
        // Given
        whenever(currentMediaProvider.currentMedia).thenReturn(Media())

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        val shadow = shadowOf(viewModel.prevAction)
        assertEquals(PlaybackAndroidService.ACTION_PREV, shadow.savedIntent.action)
    }
}
