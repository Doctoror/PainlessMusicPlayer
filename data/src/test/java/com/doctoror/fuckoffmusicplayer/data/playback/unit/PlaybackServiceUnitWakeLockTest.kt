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

import android.content.Context
import android.os.PowerManager
import android.os.PowerManager.WakeLock
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlaybackServiceUnitWakeLockTest {

    private val powerManager: PowerManager = mock()
    private val wakeLock: WakeLock = mock()
    private val context: Context = mock()

    private val underTest = PlaybackServiceUnitWakeLock(context)

    @Before
    fun setup() {
        whenever(powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG))
                .thenReturn(wakeLock)

        whenever(context.getSystemService(Context.POWER_SERVICE))
                .thenReturn(powerManager)
    }

    @Test
    fun acquiresWakeLockOnCreate() {
        // When
        underTest.onCreate()

        // Then
        verify(wakeLock).acquire()
    }

    @Test
    fun releasesAcquiredWakeLockOnDestroy() {
        // Given
        whenever(wakeLock.isHeld).thenReturn(true)

        // When
        underTest.onCreate()
        underTest.onDestroy()

        // Then
        verify(wakeLock).release()
    }

    @Test
    fun doesNotReleaseNonHeldAcquiredWakeLockOnDestroy() {
        // Given
        whenever(wakeLock.isHeld).thenReturn(false)

        // When
        underTest.onCreate()
        underTest.onDestroy()

        // Then
        verify(wakeLock, never()).release()
    }

    @Test
    fun doesNotReleaseWakeLockWhenNotAquired() {
        // When
        underTest.onDestroy()

        // Then
        verify(wakeLock, never()).release()
    }
}
