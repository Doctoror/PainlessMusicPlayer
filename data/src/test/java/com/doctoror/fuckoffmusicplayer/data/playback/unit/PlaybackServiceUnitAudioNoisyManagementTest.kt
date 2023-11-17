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
package com.doctoror.fuckoffmusicplayer.data.playback.usecase

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.doctoror.fuckoffmusicplayer.data.playback.unit.PlaybackServiceUnitAudioNoisyManagement
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.junit.Test

class PlaybackServiceUnitAudioNoisyManagementTest {

    private val context: Context = mock()
    private val stopAction: Runnable = mock()

    private val underTest = PlaybackServiceUnitAudioNoisyManagement(context, stopAction)

    private fun mockIntentWithAction(action: String?): Intent {
        val intent: Intent = mock()
        whenever(intent.action).thenReturn(action)
        return intent
    }

    @Test
    fun registersReceiverOnCreate() {
        // When
        underTest.onCreate()

        // Then
        verify(context).registerReceiver(underTest.receiver, underTest.receiver.intentFilter)
    }

    @Test
    fun unregistersReceiverOnDestroy() {
        // When
        underTest.onDestroy()

        // Then
        verify(context).unregisterReceiver(underTest.receiver)
    }

    @Test
    fun runsStopActionWhenReceivedAudioBecomingNoisy() {
        // When
        underTest.receiver.onReceive(context,
                mockIntentWithAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY))

        // When
        verify(stopAction).run()
    }

    @Test
    fun doesNotRunStopActionWhenArbitraryIntentReceived() {
        // When
        underTest.receiver.onReceive(context, mockIntentWithAction(null))

        // When
        verify(stopAction, never()).run()
    }
}
