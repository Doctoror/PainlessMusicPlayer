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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.annotation.VisibleForTesting
import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleObserver

class PlaybackServiceUnitAudioNoisyManagement(
        private val context: Context,
        stopAction: Runnable) : ServiceLifecycleObserver {

    @VisibleForTesting
    val receiver = AudioBecomingNoisyReceiver(stopAction)

    override fun onCreate() {
        context.registerReceiver(receiver, receiver.intentFilter)
    }

    override fun onDestroy() {
        context.unregisterReceiver(receiver)
    }

    @VisibleForTesting
    class AudioBecomingNoisyReceiver(private val stopAction: Runnable) : BroadcastReceiver() {

        internal val intentFilter = IntentFilter(
                AudioManager.ACTION_AUDIO_BECOMING_NOISY)

        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == action) {
                stopAction.run()
            }
        }
    }
}
