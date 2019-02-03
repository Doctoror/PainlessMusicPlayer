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
package com.doctoror.fuckoffmusicplayer.data.media.compat

import android.media.AudioManager
import com.doctoror.fuckoffmusicplayer.data.media.compat.AudioManagerCompat.Impl.ImplLegacy.AudioFocusChangeListenerLegacyWrapper
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class AudioManagerCompatImplLegacyTest {

    private val onAudioFocusChangeListener = { _: Int -> }

    private val audioAttributes = AudioAttributesCompat(
            usage = USAGE_MEDIA,
            contentType = CONTENT_TYPE_MUSIC,
            legacyStreamType = AudioManager.STREAM_MUSIC)

    private val audioFocusRequest = AudioFocusRequestCompat(
            audioAttributes = audioAttributes,
            acceptsDelayedFocusGain = false,
            onAudioFocusChangeListener = onAudioFocusChangeListener,
            willPauseWhenDucked = false)

    private val audioManager: AudioManager = mock()

    private val underTest = AudioManagerCompat.Impl.ImplLegacy()

    @Test
    fun requestsAudioFocus() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)

        // Then
        val captor = ArgumentCaptor.forClass(AudioManager.OnAudioFocusChangeListener::class.java)

        @Suppress("DEPRECATION")
        verify(audioManager).requestAudioFocus(
                captor.capture(),
                eq(audioAttributes.legacyStreamType),
                eq(AudioManager.AUDIOFOCUS_GAIN)
        )

        val listenerArgument = captor.value as AudioFocusChangeListenerLegacyWrapper
        assertEquals(onAudioFocusChangeListener, listenerArgument.wrapped)
    }

    @Test
    fun abandonsRequestedAudioFocus() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)
        underTest.abandonAudioFocus(audioManager)

        // Then
        val captor = ArgumentCaptor.forClass(AudioManager.OnAudioFocusChangeListener::class.java)

        @Suppress("DEPRECATION")
        verify(audioManager).abandonAudioFocus(captor.capture())

        val listenerArgument = captor.value as AudioFocusChangeListenerLegacyWrapper
        assertEquals(onAudioFocusChangeListener, listenerArgument.wrapped)
    }

    @Test
    fun doesNotAbandonsRequestedAudioFocusTwice() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)
        underTest.abandonAudioFocus(audioManager)
        underTest.abandonAudioFocus(audioManager)

        // Then
        @Suppress("DEPRECATION")
        verify(audioManager, times(1)).abandonAudioFocus(any())
    }

    @Test
    fun doesNothingWhenAbandonRequestedWithoutPriorRequest() {
        // When
        underTest.abandonAudioFocus(audioManager)

        // Then
        @Suppress("DEPRECATION")
        verify(audioManager, never()).abandonAudioFocus(any())
    }
}
