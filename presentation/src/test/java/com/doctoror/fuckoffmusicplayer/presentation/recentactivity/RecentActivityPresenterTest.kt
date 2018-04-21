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
package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import android.content.res.Resources
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.MediaStore
import com.doctoror.commons.reactivex.TestSchedulersProvider
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsProvider
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class RecentActivityPresenterTest {

    private val albumItemsFactory = AlbumItemsFactory()
    private val albumsProvider: AlbumsProvider = mock()
    private val fragment: RecentActivityFragment = mock()
    private val libraryPermissionProvider: LibraryPermissionsProvider = mock()
    private val queueProvider: QueueProviderAlbums = mock()
    private val resources: Resources = mock {
        on(it.getText(R.string.Recently_added)).doReturn("Recently added")
        on(it.getText(R.string.Recently_played_albums)).doReturn("Recently played albums")
    }
    private val viewModel = RecentActivityViewModel()

    private val underTest = RecentActivityPresenter(
            albumItemsFactory,
            albumsProvider,
            fragment,
            libraryPermissionProvider,
            queueProvider,
            resources,
            mock(),
            TestSchedulersProvider(),
            viewModel)

    private fun givenPermissionDenied() {
        whenever(libraryPermissionProvider.permissionsGranted())
                .thenReturn(false)

        whenever(libraryPermissionProvider.requestPermission())
                .thenReturn(Observable.just(false))
    }

    private fun givenPermissionGranted() {
        whenever(libraryPermissionProvider.permissionsGranted())
                .thenReturn(true)

        whenever(libraryPermissionProvider.requestPermission())
                .thenReturn(Observable.just(true))
    }

    private fun givenRecyclerAdapterMocked() {
        viewModel.recyclerAdapter.set(mock<RecentActivityRecyclerAdapter>())
    }

    private fun givenAlbumsProviderMocked() {
        whenever(albumsProvider.loadRecentlyPlayedAlbums(any())).thenReturn(Observable.empty())
        whenever(albumsProvider.loadRecentlyScannedAlbums(any())).thenReturn(Observable.empty())
    }

    private fun mockEmptyCursor(): Cursor = mock {
        on(it.isAfterLast).doReturn(true)
    }

    private fun makeSingleMediaItemCursor() = MatrixCursor(arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.FIRST_YEAR
    )).apply {
        addRow(arrayOf(
                "1",
                "Album",
                "Album Art",
                "1990"))
    }

    @Test
    fun requestsPermissionOnStart() {
        // Given
        givenPermissionDenied()

        // When
        underTest.onStart()

        // Then
        verify(libraryPermissionProvider).requestPermission()
    }

    @Test
    fun showsViewPermissionDeniedWhenPermissionDenied() {
        // Given
        givenPermissionDenied()

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildPermissionDenied,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsViewProgressWhenPermissionGranted() {
        // Given
        givenPermissionGranted()
        givenAlbumsProviderMocked()

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildProgress,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsErrorOnRecentlyPlayedError() {
        // Given
        givenPermissionGranted()

        whenever(albumsProvider.loadRecentlyPlayedAlbums(any()))
                .thenReturn(Observable.error(IOException()))

        whenever(albumsProvider.loadRecentlyScannedAlbums(any()))
                .thenReturn(Observable.empty())

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildError,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsErrorOnRecentlyScannedError() {
        // Given
        givenPermissionGranted()

        whenever(albumsProvider.loadRecentlyPlayedAlbums(any()))
                .thenReturn(Observable.error(IOException()))

        whenever(albumsProvider.loadRecentlyScannedAlbums(any()))
                .thenReturn(Observable.empty())

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildError,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsViewEmptyWhenLoadedDataIsEmpty() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()

        val emptyCursor = mockEmptyCursor()

        whenever(albumsProvider.loadRecentlyPlayedAlbums(any()))
                .thenReturn(Observable.just(emptyCursor))

        whenever(albumsProvider.loadRecentlyScannedAlbums(any()))
                .thenReturn(Observable.just(emptyCursor))

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildEmpty,
                viewModel.displayedChild.get())
    }


    @Test
    fun showsViewContentWhenLoadedRecentlyPlayedButRecentlyScannedIsEmpty() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()

        val recentlyPlayedCursor = makeSingleMediaItemCursor()
        whenever(albumsProvider.loadRecentlyPlayedAlbums(any()))
                .thenReturn(Observable.just(recentlyPlayedCursor))

        val emptyCursor = mockEmptyCursor()
        whenever(albumsProvider.loadRecentlyScannedAlbums(any()))
                .thenReturn(Observable.just(emptyCursor))

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildContent,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsViewContentWhenLoadedRecentlyScannedButRecentlyPlayedIsEmpty() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()

        val emptyCursor = mockEmptyCursor()
        whenever(albumsProvider.loadRecentlyPlayedAlbums(any()))
                .thenReturn(Observable.just(emptyCursor))

        val recentlyScannedCursor = makeSingleMediaItemCursor()
        whenever(albumsProvider.loadRecentlyScannedAlbums(any()))
                .thenReturn(Observable.just(recentlyScannedCursor))

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildContent,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsViewContentWhenEverythingLoaded() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()

        val recentlyPlayedCursor = makeSingleMediaItemCursor()
        whenever(albumsProvider.loadRecentlyPlayedAlbums(any()))
                .thenReturn(Observable.just(recentlyPlayedCursor))

        val recentlyScannedCursor = makeSingleMediaItemCursor()
        whenever(albumsProvider.loadRecentlyScannedAlbums(any()))
                .thenReturn(Observable.just(recentlyScannedCursor))

        // When
        underTest.onStart()

        // Then
        assertEquals(
                viewModel.animatorChildContent,
                viewModel.displayedChild.get())
    }
}
