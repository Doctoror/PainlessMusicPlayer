/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.library.albums

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums
import com.doctoror.fuckoffmusicplayer.presentation.Henson
import com.doctoror.fuckoffmusicplayer.presentation.queue.QueueActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Handles album click from adapter view
 */
class AlbumClickHandler(
    private val host: Fragment,
    private val queueProvider: QueueProviderAlbums) {

    private var disposable: Disposable? = null

    fun onAlbumClick(
            albumId: Long,
            albumName: String?,
            itemViewProvider: () -> View?) {
        disposable?.dispose()
        disposable = queueProvider.fromAlbum(albumId)
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (it.isEmpty()) {
                                onAlbumQueueEmpty()
                            } else {
                                onAlbumQueueLoaded(it, albumName, itemViewProvider)
                            }
                        },
                        { onAlbumQueueEmpty() })
    }

    fun onStop() {
        disposable?.dispose()
        disposable = null
    }

    private fun onAlbumQueueEmpty() {
        if (host.isAdded) {
            host.activity?.let {
                Toast.makeText(it, R.string.The_queue_is_empty, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onAlbumQueueLoaded(
            queue: List<Media>,
            albumName: String?,
            itemViewProvider: () -> View?) {
        host.activity?.let {
            val intent = buildNavigationIntent(it, queue, albumName)
            val options = makeActivityOptions(it, itemViewProvider)
            host.startActivity(intent, options)
        }
    }

    private fun buildNavigationIntent(
            activity: Activity,
            queue: List<Media>,
            albumName: String?
    ) = Henson
            .with(activity)
            .gotoQueueActivity()
            .hasCoverTransition(true)
            .hasItemViewTransition(false)
            .isNowPlayingQueue(false)
            .queue(queue)
            .title(albumName)
            .build()

    private fun makeActivityOptions(
            activity: Activity,
            itemViewProvider: () -> View?) = itemViewProvider.invoke()?.let {
        ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, it, QueueActivity.TRANSITION_NAME_ALBUM_ART).toBundle()
    }
}
