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
package com.doctoror.fuckoffmusicplayer.presentation.settings

import com.doctoror.fuckoffmusicplayer.domain.settings.Settings
import com.doctoror.fuckoffmusicplayer.domain.settings.Theme
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsPresenterTest {

    private val dayNightModeMapper: DayNightModeMapper = mock()
    private val dayNightPermissionProvider: DayNightPermissionProvider = mock()
    private val viewModel = SettingsViewModel()
    private val settings: Settings = mock()
    private val themeToButtonIdMapper: ThemeToButtonIdMapper = mock()

    private val underTest = SettingsPresenter(
            dayNightModeMapper,
            dayNightPermissionProvider,
            viewModel,
            settings,
            themeToButtonIdMapper)

    @Test
    fun updatesThemeModelFromSettingsOnCreate() {
        // Given
        val theme = Theme.NIGHT
        val buttonId = 1
        whenever(settings.theme).thenReturn(theme)
        whenever(themeToButtonIdMapper.themeToButtonId(theme)).thenReturn(buttonId)

        // When
        underTest.onCreate()

        // Then
        assertEquals(buttonId, viewModel.themeCheckedItem.get())
    }

    @Test
    fun updatesScrobbleEnabledFromSettingsOnCreate() {
        // Given
        val scrobbleEnabled = true
        whenever(settings.isScrobbleEnabled).thenReturn(scrobbleEnabled)

        // When
        underTest.onCreate()

        // Then
        assertEquals(scrobbleEnabled, viewModel.isScrobbleEnabled.get())
    }

    @Test
    fun showsDayNightAccuracyDialogIfThemeIsDayNightAndNotSuppressed() {
        // Given
        whenever(settings.theme).thenReturn(Theme.DAYNIGHT)

        // When
        underTest.onCreate()

        // Then
        assertTrue(viewModel.dayNightAccuracyDialogShown.get())
    }

    @Test
    fun doesNotShowDayNightAccuracyDialogIfThemeIsDayNightButSuppressed() {
        // Given
        whenever(settings.theme).thenReturn(Theme.DAYNIGHT)
        viewModel.suppressDayNightWarnings = true

        // When
        underTest.onCreate()

        // Then
        assertFalse(viewModel.dayNightAccuracyDialogShown.get())
    }

    @Test
    fun doesNotShowDayNightAccuracyDialogIfThemeIsNotDayNight() {
        // Given
        whenever(settings.theme).thenReturn(Theme.DAY)

        // When
        underTest.onCreate()

        // Then
        assertFalse(viewModel.dayNightAccuracyDialogShown.get())
    }

    @Test
    fun doesNotShowDayNightAccuracyDialogIfHasPermission() {
        // Given
        whenever(settings.theme).thenReturn(Theme.DAYNIGHT)
        whenever(dayNightPermissionProvider.hasPermission()).thenReturn(true)

        // When
        underTest.onCreate()

        // Then
        assertFalse(viewModel.dayNightAccuracyDialogShown.get())
    }

    @Test
    fun updatesScrobbleEnabledSetting() {
        // Given
        val value = true

        // When
        underTest.onScrobbleEnabled(value)

        // Then
        verify(settings).isScrobbleEnabled = value
    }

    @Test
    fun doesNotUpdateScrobbleEnabledSettingWhenTheSameValuePassed() {
        // Given
        val value = false

        // When
        underTest.onScrobbleEnabled(value)

        // Then
        verify(settings, never()).isScrobbleEnabled = value
    }

    @Test
    fun updatesThemeSettingAndRestarts() {
        // Given
        val buttonId = 1
        val theme = Theme.DAY
        whenever(themeToButtonIdMapper.buttonIdToTheme(buttonId)).thenReturn(theme)

        // When
        underTest.onThemeClick(buttonId)

        // Then
        verify(settings).theme = theme
        assertTrue(viewModel.restart.get())
    }

    @Test
    fun doesNotUpdateThemeWhenTheSameValuePassed() {
        // Given
        val buttonId = 1
        val theme = Theme.NIGHT
        whenever(settings.theme).thenReturn(Theme.NIGHT)
        whenever(themeToButtonIdMapper.buttonIdToTheme(buttonId)).thenReturn(theme)

        // When
        underTest.onThemeClick(buttonId)

        // Then
        verify(settings, never()).theme = theme
        assertFalse(viewModel.restart.get())
    }
}
