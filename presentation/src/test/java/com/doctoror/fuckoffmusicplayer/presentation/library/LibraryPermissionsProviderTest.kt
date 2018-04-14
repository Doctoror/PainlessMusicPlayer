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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.presentation.rxpermissions.RxPermissionsProvider
import com.nhaarman.mockito_kotlin.*
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class LibraryPermissionsProviderTest {

    private val context: Context = mock()
    private val rxPermissions: RxPermissions = mock()
    private val rxPermissionsProvider: RxPermissionsProvider = mock {
        on(it.provideRxPermissions()).doReturn(rxPermissions)
    }

    private val underTest = LibraryPermissionsProvider(context, rxPermissionsProvider)

    private fun givenPermissionsDenied() {
        whenever(rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE))
                .thenReturn(Observable.just(false))
    }

    private fun givenPermissionsGranted() {
        whenever(rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE))
                .thenReturn(Observable.just(true))
    }

    @Test
    fun detectsPermissionDenied() {
        // Given
        whenever(context.checkPermission(eq(Manifest.permission.READ_EXTERNAL_STORAGE), any(), any()))
                .thenReturn(PackageManager.PERMISSION_DENIED)

        // When
        val result = underTest.permissionsGranted()

        // Then
        assertFalse(result)
    }

    @Test
    fun detectsPermissionGranted() {
        // Given
        whenever(context.checkPermission(eq(Manifest.permission.READ_EXTERNAL_STORAGE), any(), any()))
                .thenReturn(PackageManager.PERMISSION_GRANTED)

        // When
        val result = underTest.permissionsGranted()

        // Then
        assertTrue(result)
    }

    @Test
    fun setsPermissionRequestedUponRequest() {
        // Given
        givenPermissionsDenied()

        // When
        underTest.requestPermission().test()

        // Then
        assertTrue(RuntimePermissions.arePermissionsRequested())
    }

    @Test
    fun deliversPermissionDenied() {
        // Given
        givenPermissionsDenied()

        // When
        val o = underTest.requestPermission().test()

        // Then
        o.assertResult(false)
    }

    @Test
    fun deliversPermissionGranted() {
        // Given
        givenPermissionsGranted()

        // When
        val o = underTest.requestPermission().test()

        // Then
        o.assertResult(true)
    }
}
