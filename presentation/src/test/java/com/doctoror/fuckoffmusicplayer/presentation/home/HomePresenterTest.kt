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
package com.doctoror.fuckoffmusicplayer.presentation.home

import android.view.View
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationItem
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationViewModel
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test

class HomePresenterTest {

    private val playbackData: PlaybackData = mock()
    private val viewModel = HomeViewModel(NavigationViewModel())

    private val underTest = HomePresenter(playbackData, viewModel)

    @Test
    fun showsStatusCardWhenHasQueueItemAndQueuePositionValid() {
        // Given
        whenever(playbackData.queuePositionObservable()).thenReturn(Observable.just(0))
        whenever(playbackData.queue).thenReturn(listOf(Media()))

        // When
        underTest.onStart()

        // Then
        assertEquals(View.VISIBLE, viewModel.playbackStatusCardVisibility.get())
    }

    @Test
    fun hidesStatusCardWhenHasQueueItemButQueuePositionInvalid() {
        // Given
        whenever(playbackData.queuePositionObservable()).thenReturn(Observable.just(1))
        whenever(playbackData.queue).thenReturn(listOf(Media()))

        // When
        underTest.onStart()

        // Then
        assertEquals(View.GONE, viewModel.playbackStatusCardVisibility.get())
    }

    @Test
    fun hidesStatusCardWhenQueueEmpty() {
        // Given
        whenever(playbackData.queuePositionObservable()).thenReturn(Observable.just(0))
        whenever(playbackData.queue).thenReturn(emptyList())

        // When
        underTest.onStart()

        // Then
        assertEquals(View.GONE, viewModel.playbackStatusCardVisibility.get())
    }

    @Test
    fun hidesStatusCardWhenQueueIsNull() {
        // Given
        whenever(playbackData.queuePositionObservable()).thenReturn(Observable.just(0))

        // When
        underTest.onStart()

        // Then
        assertEquals(View.GONE, viewModel.playbackStatusCardVisibility.get())
    }

    @Test
    fun hidesStatusCardWhenQueuePositionIsNotSet() {
        // Given
        whenever(playbackData.queuePositionObservable()).thenReturn(Observable.empty())

        // When
        underTest.onStart()

        // Then
        assertEquals(View.GONE, viewModel.playbackStatusCardVisibility.get())
    }

    @Test
    fun navigatesToNormalNavigationItem() {
        // Given
        val item = NavigationItem.RECENT_ACTIVITY

        // When
        underTest.navigateTo(item)

        // Then
        assertEquals(item.title, item.title)
        assertEquals(item, viewModel.navigationModel.navigationItem.get())
    }

    @Test
    fun navigatesToSettingsButDoesNotUpdateTitle() {
        // Given
        val item = NavigationItem.SETTINGS

        // When
        underTest.navigateTo(item)

        // Then
        assertEquals(0, viewModel.title.get())
        assertEquals(item, viewModel.navigationModel.navigationItem.get())
    }
}
