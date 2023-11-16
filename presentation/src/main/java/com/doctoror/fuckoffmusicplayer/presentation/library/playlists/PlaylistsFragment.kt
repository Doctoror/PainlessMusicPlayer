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
package com.doctoror.fuckoffmusicplayer.presentation.library.playlists

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager
import com.doctoror.fuckoffmusicplayer.domain.queue.Media
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderPlaylists
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRandom
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned
import com.doctoror.fuckoffmusicplayer.presentation.Henson
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment
import com.doctoror.fuckoffmusicplayer.presentation.library.recentalbums.RecentAlbumsActivity
import com.doctoror.fuckoffmusicplayer.presentation.queue.QueueActivity
import com.doctoror.fuckoffmusicplayer.presentation.util.ViewUtils
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

/**
 * "Playlsits" fragment
 */
class PlaylistsFragment : LibraryListFragment() {

    @Inject
    lateinit var recentActivityManager: RecentActivityManager

    @Inject
    lateinit var queueProviderPlaylists: QueueProviderPlaylists

    @Inject
    lateinit var queueProviderRandom: QueueProviderRandom

    @Inject
    lateinit var queueProviderRecentlyScanned: QueueProviderRecentlyScanned

    private lateinit var adapter: PlaylistsRecyclerAdapter

    private var loading: Boolean = false

    private var noTracksToast: Toast? = null

    private var recyclerView: RecyclerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun obtainConfig() = Config(
            canShowEmptyView = false,
            dataSource = { queueProviderPlaylists.load(it) },
            emptyMessage = "",
            recyclerAdapter = createRecyclerAdapter()
    )

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        super.setupRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    private fun createRecyclerAdapter(): PlaylistsRecyclerAdapter {
        val activity = activity ?: throw IllegalStateException("Activity is null")

        adapter = PlaylistsRecyclerAdapter(activity, generateLivePlaylists(resources))
        adapter.setOnPlaylistClickListener(OnPlaylistClickListener())
        return adapter
    }

    override fun onStop() {
        super.onStop()
        clearLoadingFlag()
    }

    @SuppressLint("ShowToast")
    private fun showNoTracksToast() {
        if (noTracksToast == null) {
            noTracksToast = Toast.makeText(activity, R.string.The_queue_is_empty, Toast.LENGTH_LONG)
        }
        if (noTracksToast!!.view?.windowToken == null) {
            noTracksToast!!.show()
        }
    }

    private fun clearLoadingFlag() {
        loading = false
    }

    private fun itemViewForPosition(position: Int): View? {
        return ViewUtils.getItemView(recyclerView, position)
    }

    private fun loadLivePlaylistAndPlay(livePlaylist: LivePlaylist, position: Int) {
        if (loading) {
            return
        } else {
            loading = true
        }

        val activity = activity
        if (activity == null) {
            clearLoadingFlag()
            return
        }

        when (livePlaylist.type) {
            LivePlaylist.TYPE_RECENTLY_PLAYED_ALBUMS -> {
                clearLoadingFlag()
                goToRecentAlbumsActivity(activity, position)
            }

            LivePlaylist.TYPE_RECENTLY_SCANNED -> loadLivePlaylistAndPlay(position,
                    livePlaylist.title.toString(),
                    queueProviderRecentlyScanned.recentlyScannedQueue())

            LivePlaylist.TYPE_RANDOM_PLAYLIST -> loadLivePlaylistAndPlay(position,
                    livePlaylist.title.toString(),
                    queueProviderRandom.randomQueue())
        }
    }

    private fun loadLivePlaylistAndPlay(position: Int,
                                        name: String,
                                        queueSource: Observable<List<Media>>) {
        disposeOnStop(queueSource
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { onLivePlaylistLoaded(position, name, it) },
                        { onLivePlaylistLoadFailed(it) },
                        { onLivePlaylistLoadComplete() }
                )
        )
    }

    private fun onLivePlaylistLoaded(position: Int,
                                     name: String,
                                     medias: List<Media>) {
        if (isAdded) {
            onQueueLoaded(itemViewForPosition(position), name, medias)
        }
    }

    private fun onLivePlaylistLoadFailed(t: Throwable) {
        if (isAdded) {
            clearLoadingFlag()
            Toast.makeText(activity,
                    getString(R.string.Failed_to_load_data_s, t.message),
                    Toast.LENGTH_LONG).show()
        }
    }

    private fun onLivePlaylistLoadComplete() {
        if (isAdded) {
            clearLoadingFlag()
        }
    }

    private fun goToRecentAlbumsActivity(context: Activity, position: Int) {
        val recentAlbums = recentActivityManager.getRecentlyPlayedAlbums()
        if (recentAlbums.isEmpty()) {
            Toast.makeText(context, R.string.You_played_no_albums_yet, Toast.LENGTH_LONG).show()
        } else {
            val intent = Henson.with(context).gotoRecentAlbumsActivity().build()

            val view = itemViewForPosition(position)
            if (view != null) {
                val options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(context, view,
                                RecentAlbumsActivity.TRANSITION_NAME_ROOT)

                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
        }
    }

    private fun generateLivePlaylists(res: Resources): List<LivePlaylist> {
        val livePlaylists = ArrayList<LivePlaylist>(3)
        livePlaylists.add(LivePlaylist(LivePlaylist.TYPE_RECENTLY_PLAYED_ALBUMS,
                res.getText(R.string.Recently_played_albums)))

        livePlaylists.add(LivePlaylist(LivePlaylist.TYPE_RECENTLY_SCANNED,
                res.getText(R.string.Recently_added)))

        livePlaylists.add(LivePlaylist(LivePlaylist.TYPE_RANDOM_PLAYLIST,
                res.getText(R.string.Random_playlist)))
        return livePlaylists
    }

    private fun onQueueLoaded(itemView: View?,
                              name: String?,
                              queue: List<Media>?) {
        if (queue != null && !queue.isEmpty()) {
            activity?.let {
                val intent = Henson.with(it).gotoQueueActivity()
                        .hasCoverTransition(false)
                        .hasItemViewTransition(true)
                        .isNowPlayingQueue(false)
                        .queue(queue)
                        .title(name)
                        .build()

                var options: Bundle? = null
                if (itemView != null) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(it,
                            itemView, QueueActivity.TRANSITION_NAME_ROOT).toBundle()

                }
                startActivity(intent, options)
            }
        } else {
            showNoTracksToast()
            clearLoadingFlag()
        }
    }

    private inner class OnPlaylistClickListener : PlaylistsRecyclerAdapter.OnPlaylistClickListener {

        override fun onLivePlaylistClick(playlist: LivePlaylist, position: Int) {
            loadLivePlaylistAndPlay(playlist, position)
        }

        override fun onPlaylistClick(id: Long, name: String?, position: Int) {
            disposeOnStop(queueProviderPlaylists.loadQueue(id)
                    .take(1)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { onQueueLoaded(itemViewForPosition(position), name, it) },
                            { onLivePlaylistLoadFailed(it) }
                    )
            )
        }

        override fun onPlaylistDeleteClick(id: Long, name: String?) {
            val activity = activity
            val fragmentManager = fragmentManager
            if (activity != null && fragmentManager != null) {
                DeletePlaylistDialogFragment.show(activity,
                        fragmentManager,
                        id,
                        name)
            }
        }
    }
}
