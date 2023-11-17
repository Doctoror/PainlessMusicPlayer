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

import android.graphics.Bitmap
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class SingleRowAppWidgetPresenterAppearanceTest {

    private val albumThumbHolder: AlbumThumbHolder = mock()
    private val currentMediaProvider: CurrentMediaProvider = mock()
    private val viewModel = SingleRowAppWidgetViewModel()

    private val underTest = SingleRowAppWidgetPresenter(
            albumThumbHolder, currentMediaProvider, viewModel)

    @Test
    fun correctPlayPauseIconIsSetForStateNotPlaying() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(R.drawable.ic_play_arrow_white_24dp, viewModel.playPauseResId)
    }

    @Test
    fun correctPlayPauseIconIsSetForStatePlaying() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_PLAYING)

        // Then
        assertEquals(R.drawable.ic_pause_white_24dp, viewModel.playPauseResId)
    }

    @Test
    fun unknownArtistIsSetWhenNoMedia() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(
                RuntimeEnvironment.application.getText(R.string.Unknown_artist),
                viewModel.artistText)
    }

    @Test
    fun unknownArtistIsSetWhenMediaHasNoArtist() {
        // Given
        whenever(currentMediaProvider.currentMedia).thenReturn(Media())

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(
                RuntimeEnvironment.application.getText(R.string.Unknown_artist),
                viewModel.artistText)
    }

    @Test
    fun artistIsSetFromMedia() {
        // Given
        val artist = "Artist"
        whenever(currentMediaProvider.currentMedia).thenReturn(Media(artist = artist))

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(artist, viewModel.artistText)
    }

    @Test
    fun untitledTitleWhenNoMedia() {
        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(
                RuntimeEnvironment.application.getText(R.string.Untitled),
                viewModel.titleText)
    }

    @Test
    fun untitledTitleWhenMediaHasNoTitle() {
        // Given
        whenever(currentMediaProvider.currentMedia).thenReturn(Media())

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(
                RuntimeEnvironment.application.getText(R.string.Untitled),
                viewModel.titleText)
    }

    @Test
    fun titleIsSetFromMedia() {
        // Given
        val title = "Title"
        whenever(currentMediaProvider.currentMedia).thenReturn(Media(title = title))

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(title, viewModel.titleText)
    }

    @Test
    fun albumThumbIsSet() {
        // Given
        val thumb: Bitmap = mock()
        whenever(albumThumbHolder.albumThumb).thenReturn(thumb)

        // When
        underTest.bindState(RuntimeEnvironment.application, PlaybackState.STATE_IDLE)

        // Then
        assertEquals(thumb, viewModel.albumThumb)
    }
}
