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

import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.settings.Theme
import junit.framework.Assert.assertEquals
import org.junit.Test

class ThemeToButtonIdMapperTest {

    private val underTest = ThemeToButtonIdMapper()

    @Test
    fun returnsRadioDayForThemeDay() {
        assertEquals(R.id.radioDay, underTest.themeToButtonId(Theme.DAY))
    }

    @Test
    fun returnsRadioNightForThemeNight() {
        assertEquals(R.id.radioNight, underTest.themeToButtonId(Theme.NIGHT))
    }

    @Test
    fun returnsRadioDayNightForThemeDayNight() {
        assertEquals(R.id.radioDayNight, underTest.themeToButtonId(Theme.DAYNIGHT))
    }

    @Test
    fun returnsThemeDayForRadioDay() {
        assertEquals(Theme.DAY, underTest.buttonIdToTheme(R.id.radioDay))
    }

    @Test
    fun returnsThemeNightForRadioNight() {
        assertEquals(Theme.NIGHT, underTest.buttonIdToTheme(R.id.radioNight))
    }

    @Test
    fun returnsThemeDayNightForRadioDayNight() {
        assertEquals(Theme.DAYNIGHT, underTest.buttonIdToTheme(R.id.radioDayNight))
    }

    @Test(expected = IllegalArgumentException::class)
    fun throwsIllegalArgumentExceptionForInvalidButtonId() {
        underTest.buttonIdToTheme(0)
    }
}
