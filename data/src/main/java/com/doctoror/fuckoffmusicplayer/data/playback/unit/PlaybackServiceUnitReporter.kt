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

import android.support.annotation.WorkerThread
import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleObserver
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory

class PlaybackServiceUnitReporter(
        private val currentMediaProvider: CurrentMediaProvider,
        private val mediaSessionHolder: MediaSessionHolder,
        private val playbackReporterFactory: PlaybackReporterFactory) : ServiceLifecycleObserver {

    private var playbackReporter: PlaybackReporter? = null

    override fun onCreate() {
        val mediaSession = mediaSessionHolder.mediaSession
                ?: throw IllegalStateException("MediaSession is null")

        playbackReporter = playbackReporterFactory.newUniversalReporter(mediaSession)
    }

    @WorkerThread
    fun reportCurrentMedia() {
        val media = currentMediaProvider.currentMedia
        if (media != null) {
            playbackReporter?.reportTrackChanged(media)
        }
    }

    @WorkerThread
    fun reportPlaybackState(
            state: PlaybackState,
            errorMessage: CharSequence?) {
        playbackReporter?.reportPlaybackStateChanged(state, errorMessage)
    }

    override fun onDestroy() {
        playbackReporter?.onDestroy()
    }
}
