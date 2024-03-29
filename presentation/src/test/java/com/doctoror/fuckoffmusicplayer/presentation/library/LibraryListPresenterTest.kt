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
package com.doctoror.fuckoffmusicplayer.presentation.library

import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView
import com.doctoror.commons.reactivex.TestSchedulersProvider
import com.doctoror.fuckoffmusicplayer.presentation.widget.CursorRecyclerViewAdapter
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.verifyNoInteractions
import java.io.IOException

class LibraryListPresenterTest {

    private val libraryPermissionChecker: LibraryPermissionsChecker = mock()
    private val libraryPermissionProvider: LibraryPermissionsRequester = mock()
    private val optionsMenuInvalidator: OptionsMenuInvalidator = mock()
    private val searchQuerySource = PublishSubject.create<String>()
    private val viewModel = LibraryListViewModel()

    private val underTest = LibraryListPresenter(
        libraryPermissionChecker,
        libraryPermissionProvider,
        optionsMenuInvalidator,
        mock(),
        TestSchedulersProvider(),
        searchQuerySource,
        viewModel
    )

    private fun givenPermissionDenied() {
        whenever(libraryPermissionChecker.permissionsGranted())
            .thenReturn(false)

        whenever(libraryPermissionProvider.requestPermission())
            .thenReturn(Observable.just(false))
    }

    private fun givenPermissionGranted() {
        whenever(libraryPermissionChecker.permissionsGranted())
            .thenReturn(true)

        whenever(libraryPermissionProvider.requestPermission())
            .thenReturn(Observable.just(true))
    }

    private fun givenDataSourceReturns(toReturn: Observable<Cursor>): LibraryDataSource {
        val dataSource: LibraryDataSource = mock {
            on(it.invoke(any())).doReturn(toReturn)
        }

        underTest.setDataSource(dataSource)
        return dataSource
    }

    private fun givenRecyclerAdapterMocked() {
        viewModel.recyclerAdapter.set(mock<CursorRecyclerViewAdapter<RecyclerView.ViewHolder>>())
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
            viewModel.displayedChild.get()
        )
    }

    @Test
    fun showsViewProgressWhenPermissionGranted() {
        // Given
        givenPermissionGranted()

        // When
        underTest.onStart()

        // Then
        assertEquals(
            viewModel.animatorChildProgress,
            viewModel.displayedChild.get()
        )
    }

    @Test
    fun invalidatesOptionsMenuWhenPermissionGranted() {
        // Given
        givenPermissionGranted()

        // When
        underTest.onStart()

        // Then
        verify(optionsMenuInvalidator).invoke()
    }

    @Test
    fun subscribesToQuerySourceWhenPermissionGranted() {
        // Given
        assertFalse(searchQuerySource.hasObservers())
        givenPermissionGranted()

        // When
        underTest.onStart()

        // Then
        assertTrue(searchQuerySource.hasObservers())
    }

    @Test
    fun doesNotLoadFromDataSourceIfPermissionLost() {
        // Given
        givenPermissionGranted()
        val dataSource: LibraryDataSource = mock()
        underTest.setDataSource(dataSource)
        underTest.onStart()

        // When
        givenPermissionDenied()
        searchQuerySource.onNext("")

        // Then
        verifyNoInteractions(dataSource)
    }

    @Test
    fun loadsFromDataSourceOnQuery() {
        // Given
        givenPermissionGranted()
        val dataSource = givenDataSourceReturns(Observable.empty())

        underTest.onStart()
        val query = "query"

        // When
        searchQuerySource.onNext(query)

        // Then
        verify(dataSource).invoke(query)
    }

    @Test
    fun showsViewErrorAndResetsCursorOnQueryError() {
        // Given
        givenPermissionGranted()
        givenDataSourceReturns(Observable.error(IOException()))
        givenRecyclerAdapterMocked()

        underTest.onStart()

        // When
        searchQuerySource.onNext("")

        // Then
        assertEquals(
            viewModel.animatorChildError,
            viewModel.displayedChild.get()
        )

        verify(viewModel.recyclerAdapter.get() as CursorRecyclerViewAdapter).changeCursor(null)
    }

    @Test
    fun setsLoadedCursorToAdapter() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()

        val cursor: Cursor = mock()
        givenDataSourceReturns(Observable.just(cursor))

        underTest.onStart()

        // When
        searchQuerySource.onNext("")

        // Then
        verify(viewModel.recyclerAdapter.get() as CursorRecyclerViewAdapter).changeCursor(cursor)
    }

    @Test
    fun showsEmptyViewForEmptyCursor() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()
        givenDataSourceReturns(Observable.just(mock()))

        underTest.onStart()

        // When
        searchQuerySource.onNext("")

        // Then
        assertEquals(
            viewModel.animatorChildEmpty,
            viewModel.displayedChild.get()
        )
    }

    @Test
    fun showsViewContentForEmptyCursorIfCannotShowEmptyView() {
        // Given
        underTest.canShowEmptyView = false

        givenPermissionGranted()
        givenRecyclerAdapterMocked()
        givenDataSourceReturns(Observable.just(mock()))

        underTest.onStart()

        // When
        searchQuerySource.onNext("")

        // Then
        assertEquals(
            viewModel.animatorChildContent,
            viewModel.displayedChild.get()
        )
    }

    @Test
    fun showsViewContentForNonEmptyCursor() {
        // Given
        givenPermissionGranted()
        givenRecyclerAdapterMocked()

        val cursor: Cursor = mock()
        whenever(cursor.count).thenReturn(1)
        givenDataSourceReturns(Observable.just(cursor))

        underTest.onStart()

        // When
        searchQuerySource.onNext("")

        // Then
        assertEquals(
            viewModel.animatorChildContent,
            viewModel.displayedChild.get()
        )
    }

    @Test(expected = IllegalStateException::class)
    fun throwsWhenAdapterNotSetOnStop() {
        // When
        underTest.onStop()
    }
}
