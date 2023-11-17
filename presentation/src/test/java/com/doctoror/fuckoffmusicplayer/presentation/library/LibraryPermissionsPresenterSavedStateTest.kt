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
package com.doctoror.fuckoffmusicplayer.presentation.library

import android.os.Bundle
import com.doctoror.commons.reactivex.TestSchedulersProvider
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.parcelable.reCreateFromParcel
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class LibraryPermissionsPresenterSavedStateTest {

    private val libraryPermissionChecker: LibraryPermissionsChecker = mock()
    private val libraryPermissionProvider: LibraryPermissionsRequester = mock()
    private val runtimePermissions: RuntimePermissions = mock()
    private val schedulersProvider = TestSchedulersProvider()

    @Test
    fun savesAndRestoresInstanceState() {
        // Given
        val underTestSource = LibraryPermissionsPresenterTest.Impl(
            libraryPermissionChecker,
            libraryPermissionProvider,
            runtimePermissions,
            schedulersProvider
        )

        underTestSource.permissionRequested = true

        // When
        val savedState = Bundle()
        underTestSource.onSaveInstanceState(savedState)

        val savedStateFromParcel = reCreateFromParcel(savedState)

        val restored = LibraryPermissionsPresenterTest.Impl(
            libraryPermissionChecker,
            libraryPermissionProvider,
            runtimePermissions,
            schedulersProvider
        )

        restored.restoreInstanceState(savedStateFromParcel)

        // Then
        assertTrue(restored.permissionRequested)
    }
}
