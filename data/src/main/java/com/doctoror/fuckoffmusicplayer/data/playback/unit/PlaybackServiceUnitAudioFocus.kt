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
import android.media.AudioManager
import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleObserver
import com.doctoror.fuckoffmusicplayer.data.media.compat.*

class PlaybackServiceUnitAudioFocus(private val context: Context) : ServiceLifecycleObserver {

    var listener: AudioFocusListener? = null

    private val onAudioFocusChangeListener = { focusChange: Int ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                focusGranted = true
                listener?.onFocusGranted()
            }

            else -> {
                focusGranted = false
                listener?.onFocusDenied()
            }
        }
        Unit
    }

    private val audioAttributes = AudioAttributesCompat(
            usage = USAGE_MEDIA,
            contentType = CONTENT_TYPE_MUSIC,
            legacyStreamType = AudioManager.STREAM_MUSIC)

    private val audioFocusRequest = AudioFocusRequestCompat(
            audioAttributes = audioAttributes,
            acceptsDelayedFocusGain = false,
            onAudioFocusChangeListener = onAudioFocusChangeListener,
            willPauseWhenDucked = false)

    private var audioManager: AudioManagerCompat? = null

    private var audioFocusRequested = false

    var focusGranted = false

    override fun onCreate() {
        val audioManagerSource = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        this.audioManager = if (audioManagerSource != null) {
            AudioManagerCompat(audioManagerSource)
        } else {
            null
        }
    }

    override fun onDestroy() {
        audioManager?.abandonAudioFocus()
        audioFocusRequested = false
    }

    fun requestAudioFocus() {
        if (!audioFocusRequested) {
            audioFocusRequested = true
            val result = audioManager?.requestAudioFocus(audioFocusRequest)
                    ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
            focusGranted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    fun abandonAudioFocus() {
        audioFocusRequested = false
        audioManager?.abandonAudioFocus()
    }
}

interface AudioFocusListener {

    fun onFocusGranted()
    fun onFocusDenied()
}
