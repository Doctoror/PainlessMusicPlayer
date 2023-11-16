/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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

import android.content.res.Configuration
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment
import com.doctoror.fuckoffmusicplayer.presentation.util.AlbumArtIntoTargetApplier
import com.doctoror.fuckoffmusicplayer.presentation.widget.SpacesItemDecoration
import dagger.android.support.AndroidSupportInjection
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class AlbumsFragment : LibraryListFragment() {

    @Inject
    lateinit var albumArtIntoTargetApplier: AlbumArtIntoTargetApplier;

    @Inject
    lateinit var albumClickHandler: AlbumClickHandler

    @Inject
    lateinit var albumsProvider: AlbumsProvider

    @Inject
    lateinit var queueProvider: QueueProviderAlbums

    private var recyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun obtainConfig() = Config(
        canShowEmptyView = true,
        dataSource = { albumsProvider.load(it, Schedulers.io()) },
        emptyMessage = getText(R.string.No_albums_found),
        recyclerAdapter = createRecyclerAdapter()
    )

    override fun setupRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        applyLayoutManager(recyclerView)
        recyclerView.addItemDecoration(
            SpacesItemDecoration(
                resources.getDimensionPixelSize(R.dimen.album_grid_spacing)
            )
        )
    }

    private fun createRecyclerAdapter(): AlbumsRecyclerAdapter {
        val context = activity ?: throw IllegalStateException("Activity is null")
        val adapter = AlbumsRecyclerAdapter(context) { albumArtIntoTargetApplier }
        adapter.setOnAlbumClickListener(object : AlbumsRecyclerAdapter.OnAlbumClickListener {

            override fun onAlbumClick(position: Int, id: Long, album: String) {
                this@AlbumsFragment.onAlbumClick(position, id, album)
            }

            override fun onAlbumDeleteClick(id: Long, name: String?) {
                this@AlbumsFragment.onAlbumDeleteClick(id, name)
            }
        })
        return adapter
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        recyclerView?.let { applyLayoutManager(it) }
    }

    private fun applyLayoutManager(recyclerView: RecyclerView) {
        recyclerView.layoutManager =
            GridLayoutManager(
                activity,
                resources.getInteger(R.integer.albums_grid_columns)
            )
    }

    private fun onAlbumDeleteClick(albumId: Long, name: String?) {
        val context = activity ?: throw IllegalStateException("Activity is null")
        val fragmentManager = fragmentManager
            ?: throw IllegalStateException("FragmentManager is null")
        DeleteAlbumDialogFragment.show(context, fragmentManager, albumId, name)
    }

    private fun onAlbumClick(
        position: Int, albumId: Long,
        albumName: String?
    ) {
        albumClickHandler.onAlbumClick(albumId, albumName) { getItemView(position) }
    }
}
