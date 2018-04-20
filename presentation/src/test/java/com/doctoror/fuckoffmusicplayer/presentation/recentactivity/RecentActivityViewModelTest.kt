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
package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import org.junit.Assert.assertEquals
import org.junit.Test

class RecentActivityViewModelTest {

    private val underTest = RecentActivityViewModel()

    @Test
    fun showsViewProgress() {
        // When
        underTest.showViewProgress()
        
        // Then
        assertEquals(underTest.animatorChildProgress, underTest.displayedChild.get())
    }

    @Test
    fun showsViewPermissionDenied() {
        // When
        underTest.showViewPermissionDenied()

        // Then
        assertEquals(underTest.animatorChildPermissionDenied, underTest.displayedChild.get())
    }

    @Test
    fun showViewEmpty() {
        // When
        underTest.showViewEmpty()

        // Then
        assertEquals(underTest.animatorChildEmpty, underTest.displayedChild.get())
    }

    @Test
    fun showViewError() {
        // When
        underTest.showViewError()

        // Then
        assertEquals(underTest.animatorChildError, underTest.displayedChild.get())
    }

    @Test
    fun showViewContent() {
        // When
        underTest.showViewContent()

        // Then
        assertEquals(underTest.animatorChildContent, underTest.displayedChild.get())
    }
}
