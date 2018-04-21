package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import android.content.res.Resources
import android.database.Cursor
import com.doctoror.commons.reactivex.SchedulersProvider
import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsPresenter
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsProvider
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.AlbumClickHandler
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

private const val MAX_HISTORY_SECTION_LENGTH = 6

class RecentActivityPresenter(
        private val albumItemsFactory: AlbumItemsFactory,
        private val albumsProvider: AlbumsProvider,
        private val fragment: RecentActivityFragment,
        private val libraryPermissionProvider: LibraryPermissionsProvider,
        private val queueProvider: QueueProviderAlbums,
        private val resources: Resources,
        runtimePermissions: RuntimePermissions,
        private val schedulersProvider: SchedulersProvider,
        private val viewModel: RecentActivityViewModel)
    : LibraryPermissionsPresenter(
        libraryPermissionProvider, runtimePermissions, schedulersProvider) {

    private val tag = "RecentActivityPresenter"

    fun onAlbumClick(
            id: Long,
            album: String?,
            itemViewProvider: AlbumClickHandler.ItemViewProvider) {
        AlbumClickHandler.onAlbumClick(
                fragment, queueProvider, id, album, itemViewProvider)
    }

    override fun onPermissionDenied() {
        viewModel.showViewPermissionDenied()
    }

    override fun onPermissionGranted() {
        viewModel.showViewProgress()
        load()
    }

    private fun load() {
        if (libraryPermissionProvider.permissionsGranted()) {

            val recentlyPlayed = albumsProvider
                    .loadRecentlyPlayedAlbums(MAX_HISTORY_SECTION_LENGTH).take(1)

            val recentlyScanned = albumsProvider
                    .loadRecentlyScannedAlbums(MAX_HISTORY_SECTION_LENGTH).take(1)

            disposeOnStop(Observable.combineLatest(recentlyPlayed, recentlyScanned,
                    RecyclerAdapterDataFunc())
                    .subscribeOn(schedulersProvider.io())
                    .observeOn(schedulersProvider.mainThread())
                    .subscribe({ this.onRecentActivityLoaded(it) }, this::onError))
        } else {
            Log.w(tag, "load() is called, READ_EXTERNAL_STORAGE is not granted")
        }
    }

    private fun onError(t: Throwable) {
        Log.w(tag, t)
        viewModel.showViewError()
    }

    private fun onRecentActivityLoaded(data: List<Any>) {
        val recyclerAdapter = viewModel.recyclerAdapter.get() as? RecentActivityRecyclerAdapter
                ?: throw IllegalStateException("recyclerAdapter is either not set or not a RecentActivityRecyclerAdapter")
        recyclerAdapter.setItems(data)
        if (data.isEmpty() || dataIsOnlyHeaders(data)) {
            viewModel.showViewEmpty()
        } else {
            viewModel.showViewContent()
        }
    }

    private fun dataIsOnlyHeaders(data: List<Any>): Boolean {
        for (item in data) {
            if (item !is RecentActivityHeader) {
                return false
            }
        }
        return true
    }

    private inner class RecyclerAdapterDataFunc
        : BiFunction<Cursor, Cursor, List<Any>> {

        override fun apply(rPlayed: Cursor, rAdded: Cursor): List<Any> {
            val data = ArrayList<Any>(MAX_HISTORY_SECTION_LENGTH + 2)
            try {
                val rPlayedList = albumItemsFactory.itemsFromCursor(rPlayed)
                if (!rPlayedList.isEmpty()) {
                    data.add(RecentActivityHeader(resources.getText(R.string.Recently_played_albums)))
                    data.addAll(rPlayedList)
                }

                data.add(RecentActivityHeader(resources.getText(R.string.Recently_added)))
                data.addAll(albumItemsFactory.itemsFromCursor(rAdded))
            } finally {
                rPlayed.close()
                rAdded.close()
            }
            return data
        }
    }
}
