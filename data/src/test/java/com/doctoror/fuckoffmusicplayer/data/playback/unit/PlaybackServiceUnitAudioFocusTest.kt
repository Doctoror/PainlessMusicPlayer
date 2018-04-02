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
package com.doctoror.fuckoffmusicplayer.data.media.playback.usecase

import android.content.Context
import android.media.AudioAttributes.CONTENT_TYPE_MUSIC
import android.media.AudioAttributes.USAGE_MEDIA
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.STREAM_MUSIC
import android.os.Build
import com.doctoror.fuckoffmusicplayer.data.playback.unit.AudioFocusListener
import com.doctoror.fuckoffmusicplayer.data.playback.unit.AudioFocusRequester
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.O_MR1])
@RunWith(RobolectricTestRunner::class)
class PlaybackServiceUnitAudioFocusTest {

    private val audioManager: AudioManager = mock()

    private val context: Context = mock()
    private val listener: AudioFocusListener = mock()

    private val underTest = AudioFocusRequester(context, listener)

    @Before
    fun setup() {
        whenever(context.getSystemService(Context.AUDIO_SERVICE)).thenReturn(audioManager)
    }

    @Test
    fun requestsFocusWithCorrectParams() {
        // When
        underTest.onCreate()
        underTest.requestAudioFocus()

        // Then
        val captor = ArgumentCaptor.forClass(AudioFocusRequest::class.java)
        verify(audioManager).requestAudioFocus(captor.capture())

        val focusRequest = captor.value
        assertEquals(CONTENT_TYPE_MUSIC, focusRequest.audioAttributes.contentType)
        assertEquals(USAGE_MEDIA, focusRequest.audioAttributes.usage)
        assertEquals(STREAM_MUSIC, focusRequest.audioAttributes.volumeControlStream)
        assertFalse(focusRequest.acceptsDelayedFocusGain())
        assertFalse(focusRequest.willPauseWhenDucked())
    }

    @Test
    fun requestsFocusOnce() {
        // When
        underTest.onCreate()
        underTest.requestAudioFocus()
        underTest.requestAudioFocus()

        // Then
        verify(audioManager, times(1)).requestAudioFocus(any())
    }

    @Test
    fun abandonsFocusOnDestroy() {
        // When
        underTest.onCreate()
        underTest.requestAudioFocus()
        underTest.onDestroy()

        // Then
        verify(audioManager).abandonAudioFocusRequest(any())
    }

    @Test
    fun doesNothingOnDestroyWhenFocusNotRequested() {
        // When
        underTest.onDestroy()

        // Then
        verify(audioManager, never()).abandonAudioFocusRequest(any())
    }
}
