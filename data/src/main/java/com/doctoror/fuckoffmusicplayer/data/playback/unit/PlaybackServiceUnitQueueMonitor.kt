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
import com.doctoror.fuckoffmusicplayer.data.playback.controller.PlaybackControllerProvider
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import io.reactivex.disposables.CompositeDisposable

/**
 * Monitors position in queue and handles it's changes.
 */
class PlaybackServiceUnitQueueMonitor(
    private val albumThumbHolder: AlbumThumbHolder,
    private val playbackControllerProvider: PlaybackControllerProvider,
    private val playbackData: PlaybackData,
    private val stopAction: Runnable
) : ServiceLifecycleObserver {

    private val disposables = CompositeDisposable()

    override fun onCreate() {
        disposables.add(playbackData.queueObservable().subscribe(this::onQueueChanged))
    }

    override fun onDestroy() {
        disposables.clear()
    }

    private fun onQueueChanged(q: List<Media>) {
        if (q.isEmpty()) {
            albumThumbHolder.albumThumb = null
            stopAction.run()
        } else {
            val playbackController = playbackControllerProvider.obtain()
            playbackController.setQueue(q)
        }
    }
}
