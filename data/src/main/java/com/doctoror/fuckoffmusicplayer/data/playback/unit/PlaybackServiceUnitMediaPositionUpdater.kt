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

import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleObserver
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState.STATE_PLAYING
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

class PlaybackServiceUnitMediaPositionUpdater(
        private val mediaPlayer: MediaPlayer,
        private val playbackData: PlaybackData) : ServiceLifecycleObserver {

    private var positionUpdater: Disposable? = null

    override fun onCreate() {
        // Not handled
    }

    override fun onDestroy() {
        disposeMediaPositionUpdater()
    }

    fun initializeMediaPositionUpdater() {
        positionUpdater?.dispose()
        positionUpdater = Observable
                .interval(POSITION_UPDATE_INTERVAL, TimeUnit.SECONDS)
                .subscribe { _ -> updateMediaPosition() }
    }

    fun disposeMediaPositionUpdater() {
        positionUpdater?.dispose()
        positionUpdater = null
    }

    private fun updateMediaPosition() {
        if (playbackData.playbackState == STATE_PLAYING) {
            playbackData.setMediaPosition(mediaPlayer.getCurrentPosition())
        }
    }

    private companion object {

        /**
         * The interval for updating media position, in seconds.
         */
        private const val POSITION_UPDATE_INTERVAL = 1L
    }
}