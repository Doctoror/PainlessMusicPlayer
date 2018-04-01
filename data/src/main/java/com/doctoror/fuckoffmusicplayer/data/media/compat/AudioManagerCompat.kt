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

import android.annotation.TargetApi
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import com.doctoror.fuckoffmusicplayer.data.media.compat.AudioManagerCompat.Impl.ImplLegacy
import com.doctoror.fuckoffmusicplayer.data.media.compat.AudioManagerCompat.Impl.ImplOreo

class AudioManagerCompat(private val audioManager: AudioManager) {

    private val impl: Impl

    init {
        impl = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ImplOreo()
        } else {
            ImplLegacy()
        }
    }

    fun requestAudioFocus(request: AudioFocusRequestCompat) =
            impl.requestAudioFocus(audioManager, request)

    fun abandonAudioFocus() = impl.abandonAudioFocus(audioManager)

    private sealed class Impl {

        abstract fun requestAudioFocus(
                audioManager: AudioManager,
                request: AudioFocusRequestCompat): Int

        abstract fun abandonAudioFocus(audioManager: AudioManager): Int

        @TargetApi(Build.VERSION_CODES.O)
        class ImplOreo : Impl() {

            private var request: AudioFocusRequest? = null

            override fun requestAudioFocus(
                    audioManager: AudioManager,
                    request: AudioFocusRequestCompat): Int {
                val realRequest = toAudioFocusRequest(request)
                this.request = realRequest
                return audioManager.requestAudioFocus(realRequest)
            }

            override fun abandonAudioFocus(
                    audioManager: AudioManager): Int {
                var returnValue = AudioManager.AUDIOFOCUS_REQUEST_FAILED
                request?.let {
                    returnValue = audioManager.abandonAudioFocusRequest(it)
                }
                request = null
                return returnValue
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

        class ImplLegacy : Impl() {

            private var listener: AudioManager.OnAudioFocusChangeListener? = null

            override fun requestAudioFocus(
                    audioManager: AudioManager,
                    request: AudioFocusRequestCompat): Int {
                val listener = AudioFocusChangeListenerLegacyWrapper(
                        request.onAudioFocusChangeListener)

                this.listener = listener

                @Suppress("DEPRECATION")
                return audioManager.requestAudioFocus(
                        listener,
                        request.audioAttributes.legacyStreamType,
                        AudioManager.AUDIOFOCUS_GAIN)
            }

            override fun abandonAudioFocus(audioManager: AudioManager): Int {
                var returnValue = AudioManager.AUDIOFOCUS_REQUEST_FAILED
                listener?.let {
                    @Suppress("DEPRECATION")
                    returnValue = audioManager.abandonAudioFocus(it)
                }
                listener = null
                return returnValue
            }

            private class AudioFocusChangeListenerLegacyWrapper(
                    private val wrapped: (Int) -> Unit) : AudioManager.OnAudioFocusChangeListener {

                override fun onAudioFocusChange(focusChange: Int) {
                    wrapped.invoke(focusChange)
                }
            }
        }
    }
}
