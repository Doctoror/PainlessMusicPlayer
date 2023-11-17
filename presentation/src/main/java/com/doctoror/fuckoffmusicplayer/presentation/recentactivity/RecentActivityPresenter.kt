package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import android.content.res.Resources
import android.database.Cursor
import android.view.View
import com.doctoror.commons.reactivex.SchedulersProvider
import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsChecker
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsPresenter
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsRequester
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.AlbumClickHandler
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

private const val MAX_HISTORY_SECTION_LENGTH = 12

class RecentActivityPresenter(
    private val albumClickHandler: AlbumClickHandler,
    private val albumItemsFactory: AlbumItemsFactory,
    private val albumsProvider: AlbumsProvider,
    private val libraryPermissionsChecker: LibraryPermissionsChecker,
    libraryPermissionRequester: LibraryPermissionsRequester,
    private val resources: Resources,
    runtimePermissions: RuntimePermissions,
    private val schedulersProvider: SchedulersProvider,
    private val viewModel: RecentActivityViewModel
) : LibraryPermissionsPresenter(
    libraryPermissionsChecker,
    libraryPermissionRequester,
    runtimePermissions,
    schedulersProvider
) {

    private val tag = "RecentActivityPresenter"

    fun onAlbumClick(
        id: Long,
        album: String?,
        itemViewProvider: () -> View?
    ) {
        albumClickHandler.onAlbumClick(id, album, itemViewProvider)
    }

    override fun onStop() {
        super.onStop()
        albumClickHandler.onStop()
    }

    override fun onPermissionDenied() {
        viewModel.showViewPermissionDenied()
    }

    override fun onPermissionGranted() {
        viewModel.showViewProgress()
        load()
    }

    private fun load() {
        if (libraryPermissionsChecker.permissionsGranted()) {

            val recentlyPlayed = albumsProvider
                .loadRecentlyPlayedAlbums(MAX_HISTORY_SECTION_LENGTH).take(1)

            val recentlyScanned = albumsProvider
                .loadRecentlyScannedAlbums(MAX_HISTORY_SECTION_LENGTH).take(1)

            disposeOnStop(
                Observable.combineLatest(
                    recentlyPlayed, recentlyScanned,
                    RecyclerAdapterDataFunc()
                )
                    .subscribeOn(schedulersProvider.io())
                    .observeOn(schedulersProvider.mainThread())
                    .subscribe({ this.onRecentActivityLoaded(it) }, this::onError)
            )
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
