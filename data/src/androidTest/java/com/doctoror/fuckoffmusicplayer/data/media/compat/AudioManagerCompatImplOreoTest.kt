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

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * Cannot be run with Robolectric. It cannot mock [AudioFocusRequest].
 */
class AudioManagerCompatImplOreoTest {

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

    private val audioManager: AudioManager = mock(AudioManager::class.java)

    private val underTest = AudioManagerCompat.Impl.ImplOreo()

    @Test
    fun requestsAudioFocus() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)

        // Then
        val captor = ArgumentCaptor.forClass(AudioFocusRequest::class.java)
        verify(audioManager).requestAudioFocus(captor.capture())

        val expectedAudioFocusRequest = toAudioFocusRequest(audioFocusRequest)
        assertTrue(audioFocusRequestsEqual(expectedAudioFocusRequest, captor.value))
    }

    @Test
    fun abandonsRequestedAudioFocus() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)
        underTest.abandonAudioFocus(audioManager)

        // Then
        val captor = ArgumentCaptor.forClass(AudioFocusRequest::class.java)
        verify(audioManager).abandonAudioFocusRequest(captor.capture())

        val expectedAudioFocusRequest = toAudioFocusRequest(audioFocusRequest)
        assertTrue(audioFocusRequestsEqual(expectedAudioFocusRequest, captor.value))
    }

    @Test
    fun doesNotAbandonRequestedAudioFocusTwice() {
        // When
        underTest.requestAudioFocus(audioManager, audioFocusRequest)
        underTest.abandonAudioFocus(audioManager)
        underTest.abandonAudioFocus(audioManager)

        // Then
        verify(audioManager, times(1)).abandonAudioFocusRequest(any())
    }

    @Test
    fun doesNothingWhenAbandonRequestedWithoutPriorRequest() {
        // When
        underTest.abandonAudioFocus(audioManager)

        // Then
        verify(audioManager, never()).abandonAudioFocusRequest(any())
    }

    private fun audioFocusRequestsEqual(
            a: AudioFocusRequest,
            b: AudioFocusRequest): Boolean {
        return (a.acceptsDelayedFocusGain() == b.acceptsDelayedFocusGain()) and
                audioAttributesEqual(a.audioAttributes, b.audioAttributes) and
                (a.willPauseWhenDucked() == b.willPauseWhenDucked())
    }

    private fun audioAttributesEqual(
            a: AudioAttributes,
            b: AudioAttributes): Boolean {
        return (a.contentType == b.contentType) and
                (a.flags == b.flags) and
                (a.usage == b.usage) and
                (a.volumeControlStream == b.volumeControlStream)
    }

    private fun toAudioAttributes(attributes: AudioAttributesCompat) = AudioAttributes
            .Builder()
            .setContentType(attributes.contentType)
            .setLegacyStreamType(attributes.legacyStreamType)
            .setUsage(attributes.usage)
            .build()

    private fun toAudioFocusRequest(request: AudioFocusRequestCompat) = AudioFocusRequest
            .Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(toAudioAttributes(request.audioAttributes))
            .setAcceptsDelayedFocusGain(request.acceptsDelayedFocusGain)
            .setOnAudioFocusChangeListener(request.onAudioFocusChangeListener)
            .setWillPauseWhenDucked(request.willPauseWhenDucked)
            .build()
}
