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

import com.doctoror.commons.reactivex.SchedulersProvider
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackServiceUnitStopTimeoutTest {

    private val schedulersProvider: SchedulersProvider = mock()
    private val stopAction: Runnable = mock()

    private val underTest = PlaybackServiceUnitStopTimeout(stopAction, schedulersProvider)

    private fun givenSchedulerComputationAndLargeTimeout() {
        underTest.timeout = Long.MAX_VALUE
        whenever(schedulersProvider.computation()).thenReturn(Schedulers.computation())
    }

    private fun givenSchedulerTrampolineAndNoTimeout() {
        underTest.timeout = 0
        whenever(schedulersProvider.computation()).thenReturn(Schedulers.trampoline())
    }

    @Test
    fun disposesPreviousStopTimerOnInitialize() {
        // Given
        givenSchedulerComputationAndLargeTimeout()
        underTest.initializeStopTimer()

        val disposable = underTest.disposable

        // When
        underTest.initializeStopTimer()

        // Then
        assertTrue(disposable!!.isDisposed)
    }

    @Test
    fun initializesStopTimer() {
        // Given
        givenSchedulerComputationAndLargeTimeout()

        // When
        underTest.initializeStopTimer()

        // Then
        assertNotNull(underTest.disposable)
        assertFalse(underTest.disposable!!.isDisposed)
    }

    @Test
    fun disposesStopTimer() {
        // Given
        givenSchedulerComputationAndLargeTimeout()
        underTest.initializeStopTimer()

        // When
        val disposable = underTest.disposable!!
        underTest.abortStopTimer()

        // Then
        assertTrue(disposable.isDisposed)
        assertNull(underTest.disposable)
    }

    @Test
    fun invokesStopAction() {
        // Given
        givenSchedulerTrampolineAndNoTimeout()

        // When
        underTest.initializeStopTimer()

        // Then
        verify(stopAction).run()
    }
}
