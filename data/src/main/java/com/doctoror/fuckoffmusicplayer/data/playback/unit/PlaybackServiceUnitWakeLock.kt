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

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager
import android.support.annotation.VisibleForTesting
import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleObserver

class PlaybackServiceUnitWakeLock(private val context: Context) : ServiceLifecycleObserver {

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        acquireWakeLock()
    }

    override fun onDestroy() {
        releaseWakeLock()
    }

    @SuppressLint("WakelockTimeout") // Should be held until released.
    private fun acquireWakeLock() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
        if (powerManager != null) {
            val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG)
            wakeLock.acquire()

            this.wakeLock = wakeLock
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.apply {
            if (isHeld) {
                release()
            }
        }
        wakeLock = null
    }

    companion object {

        @VisibleForTesting
        const val TAG = "WakelockAcquirer"
    }
}
