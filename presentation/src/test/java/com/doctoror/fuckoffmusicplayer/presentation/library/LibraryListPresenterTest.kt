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

import android.support.v7.widget.RecyclerView
import com.doctoror.fuckoffmusicplayer.presentation.widget.CursorRecyclerViewAdapter
import com.doctoror.fuckoffmusicplayer.reactivex.TestSchedulersProvider
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

// TODO not everything tested
class LibraryListPresenterTest {

    private val libraryPermissionProvider: LibraryPermissionsProvider = mock()
    private val optionsMenuInvalidator: OptionsMenuInvalidator = mock()
    private val searchQuerySource = PublishSubject.create<String>()
    private val viewModel = LibraryListViewModel()

    private val underTest = LibraryListPresenter(
            libraryPermissionProvider,
            optionsMenuInvalidator,
            TestSchedulersProvider(),
            searchQuerySource,
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
                LibraryListViewModel.ANIMATOR_CHILD_PERMISSION_DENIED,
                viewModel.displayedChild.get())
    }

    @Test
    fun showsViewProgressWhenPermissionGranted() {
        // Given
        givenPermissionGranted()

        // When
        underTest.onStart()

        // Then
        assertEquals(
                LibraryListViewModel.ANIMATOR_CHILD_PROGRESS,
                viewModel.displayedChild.get())
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
        verifyZeroInteractions(dataSource)
    }

    @Test
    fun loadsFromDataSourceOnQuery() {
        // Given
        givenPermissionGranted()

        val query = "query"
        val dataSource: LibraryDataSource = mock {
            on(it.invoke(any())).doReturn(Observable.empty())
        }

        underTest.setDataSource(dataSource)
        underTest.onStart()

        // When
        searchQuerySource.onNext(query)

        // Then
        verify(dataSource).invoke(query)
    }

    @Test
    fun showsViewErrorAndResetsCursorOnQueryError() {
        // Given
        givenPermissionGranted()

        val query = "query"
        val dataSource: LibraryDataSource = mock {
            on(it.invoke(any())).doReturn(Observable.error(IOException()))
        }

        val adapter: CursorRecyclerViewAdapter<RecyclerView.ViewHolder> = mock()
        viewModel.recyclerAdapter.set(adapter)
        underTest.setDataSource(dataSource)
        underTest.onStart()

        // When
        searchQuerySource.onNext(query)

        // Then
        assertEquals(
                LibraryListViewModel.ANIMATOR_CHILD_ERROR,
                viewModel.displayedChild.get())

        verify(adapter).changeCursor(null)
    }

    @Test(expected = IllegalStateException::class)
    fun throwsWhenAdapterNotSetOnStop() {
        // When
        underTest.onStop()
    }
}
