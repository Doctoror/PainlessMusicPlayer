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
package com.doctoror.fuckoffmusicplayer.presentation.library.tracks

import android.os.Bundle
import android.widget.Toast
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueConfig
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderTracks
import com.doctoror.fuckoffmusicplayer.domain.tracks.TracksProvider
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment2
import com.doctoror.fuckoffmusicplayer.presentation.nowplaying.NowPlayingActivity
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * "Tracks" list fragment.
 */
class TracksFragment : LibraryListFragment2() {

    @Inject
    lateinit var playbackInitializer: PlaybackInitializer

    @Inject
    lateinit var queueProvider: QueueProviderTracks

    @Inject
    lateinit var tracksProvider: TracksProvider

    private lateinit var adapter: TracksRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun obtainConfig() = Config(
            canShowEmptyView = true,
            dataSource = { tracksProvider.load(it) },
            emptyMessage = getText(R.string.No_tracks_found),
            recyclerAdapter = createRecyclerAdapter()
    )

    private fun createRecyclerAdapter(): TracksRecyclerAdapter {
        val activity = activity ?: throw IllegalStateException("Activity is null")
        val adapter = TracksRecyclerAdapter(activity)
        adapter.setOnTrackClickListener { startPosition, _ -> onTrackClick(startPosition) }
        this.adapter = adapter
        return adapter
    }

    private fun createLimitedQueueIds(startPosition: Int): LongArray {
        val tracks: LongArray
        val data = adapter.cursor
        if (data != null) {
            var limit = QueueConfig.MAX_QUEUE_SIZE
            val count = data.count
            if (startPosition + limit > count) {
                limit = count - startPosition
            }
            tracks = LongArray(limit)
            var trackIndex = 0
            var i = startPosition
            while (i < startPosition + limit) {
                if (data.moveToPosition(i)) {
                    tracks[trackIndex] = data.getLong(TracksProvider.COLUMN_ID)
                } else {
                    throw RuntimeException("Could not move Cursor to position $i")
                }
                trackIndex++
                i++
            }
        } else {
            throw IllegalStateException("Cursor is null")
        }
        return tracks
    }

    private fun queueFromIds(ids: LongArray): Observable<List<Media>> {
        return queueProvider.fromTracks(ids)
    }

    private fun onTrackClick(startPosition: Int) {
        disposeOnStop(Observable.fromCallable { createLimitedQueueIds(startPosition) }
                .flatMap { queueFromIds(it) }
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onQueueLoaded(startPosition, it) },
                        { onQueueEmpty() }
                ))
    }

    private fun onQueueLoaded(startPosition: Int,
                              queue: List<Media>) {
        if (isAdded) {
            if (queue.isEmpty()) {
                onQueueEmpty()
            } else {
                playbackInitializer.setQueueAndPlay(queue, 0)
                val activity = activity
                if (activity != null) {
                    NowPlayingActivity.start(activity, null, getItemView(startPosition))
                }
            }
        }
    }

    private fun onQueueEmpty() {
        if (isAdded) {
            Toast.makeText(activity, R.string.The_queue_is_empty, Toast.LENGTH_LONG).show()
        }
    }
}
