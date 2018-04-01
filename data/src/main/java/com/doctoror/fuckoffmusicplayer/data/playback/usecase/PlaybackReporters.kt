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

import android.support.annotation.WorkerThread
import com.doctoror.fuckoffmusicplayer.data.lifecycle.ServiceLifecycleObserver
import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayer
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory

class PlaybackReporters(
        private val currentMediaProvider: CurrentMediaProvider,
        private val mediaSessionHolder: MediaSessionHolder,
        private val playbackData: PlaybackData,
        private val playbackReporterFactory: PlaybackReporterFactory) : ServiceLifecycleObserver {

    private var playbackReporter: PlaybackReporter? = null

    override fun onCreate() {
        val mediaSession = mediaSessionHolder.mediaSession
                ?: throw IllegalStateException("MediaSession is null")

        playbackReporter = playbackReporterFactory.newUniversalReporter(mediaSession)
    }

    @WorkerThread
    fun reportCurrentMedia() {
        val pos = playbackData.queuePosition
        val media = CollectionUtils.getItemSafe<Media>(playbackData.queue, pos)
        if (media != null) {
            playbackReporter?.reportTrackChanged(media, pos)
        }
    }

    @WorkerThread
    fun reportPlaybackState(
            state: PlaybackState,
            errorMessage: CharSequence?) {
        playbackReporter?.reportPlaybackStateChanged(state, errorMessage)
    }

    @WorkerThread
    fun reportCurrentPlaybackPosition(mediaPlayer: MediaPlayer?) {
        val media = currentMediaProvider.currentMedia
        if (media == null) {
            playbackReporter?.reportPositionChanged(0, 0)
        } else {
            val mediaUri = media.data
            if (mediaUri != null && mediaUri == mediaPlayer?.getLoadedMediaUri()) {
                playbackReporter?.reportPositionChanged(
                        media.id, mediaPlayer.getCurrentPosition())
            }
        }
    }

    @WorkerThread
    fun reportCurrentQueue() {
        val queue = playbackData.queue
        if (queue != null) {
            playbackReporter?.reportQueueChanged(queue)
        }
    }

    override fun onDestroy() {
        playbackReporter?.onDestroy()
        playbackReporter = null
    }
}
