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
import android.support.v7.widget.RecyclerView
import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.presentation.widget.CursorRecyclerViewAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

typealias OptionsMenuInvalidator = () -> Unit
typealias LibraryDataSource = (String?) -> Observable<Cursor>

class LibraryListPresenter(
        private val libraryPermissionProvider: LibraryPermissionsProvider,
        private val optionsMenuInvalidator: OptionsMenuInvalidator,
        private val searchQuerySource: Observable<String>,
        private val viewModel: LibraryListModel) :
        LibraryPermissionsPresenter(libraryPermissionProvider) {

    private val tag = "LibraryListPresenter"

    private var disposablePrevious: Disposable? = null
    private var disposable: Disposable? = null

    private var dataSource: LibraryDataSource? = null

    fun setDataSource(dataSource: LibraryDataSource) {
        this.dataSource = dataSource
    }

    var canShowEmptyView: Boolean = true

    override fun onStop() {
        super.onStop()
        onDataReset()
        disposablePrevious?.dispose()
        disposablePrevious = null

        disposable?.dispose()
        disposable = null
    }

    override fun onPermissionDenied() {
        viewModel.showViewPermissionDenied()
    }

    override fun onPermissionGranted() {
        viewModel.showViewProgress()
        optionsMenuInvalidator.invoke()
        disposeOnStop(searchQuerySource.subscribe(this::restartLoader))
    }

    private fun restartLoader(searchFilter: String?) {
        if (libraryPermissionProvider.permissionsGranted()) {
            val dataSource = this.dataSource
                    ?: throw IllegalStateException("LibraryDataSource not set")
            disposablePrevious = disposable
            disposable = disposeOnStop(dataSource.invoke(searchFilter)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onNextSearchResult, this::onSearchResultLoadFailed))
        } else {
            Log.w(tag, "restartLoader is called, but READ_EXTERNAL_STORAGE is not granted")
        }
    }

    private fun onNextSearchResult(cursor: Cursor) {
        onDataLoaded(cursor)
        disposablePrevious?.dispose()
        disposablePrevious = null

        if (cursor.count == 0 && canShowEmptyView) {
            viewModel.showViewEmpty()
        } else {
            viewModel.showViewContent()
        }
    }

    private fun onSearchResultLoadFailed(t: Throwable) {
        Log.w(tag, "onSearchResultLoadFailed()", t)
        disposablePrevious?.dispose()
        disposablePrevious = null

        viewModel.showViewError()
        onDataReset()
    }

    private fun obtainCursorRecyclerAdapterOrThrow():
            CursorRecyclerViewAdapter<out RecyclerView.ViewHolder> {
        val adapter = viewModel.recyclerAdapter.get()
                ?: throw IllegalStateException("RecyclerView.Adapter not set on ViewModel")

        @Suppress("FoldInitializerAndIfToElvis")
        if (adapter !is CursorRecyclerViewAdapter<out RecyclerView.ViewHolder>) {
            throw IllegalStateException("Expecting CursorRecyclerViewAdapter")
        }

        return adapter
    }

    private fun onDataLoaded(data: Cursor) {
        obtainCursorRecyclerAdapterOrThrow().changeCursor(data)
    }

    private fun onDataReset() {
        obtainCursorRecyclerAdapterOrThrow().changeCursor(null)
    }
}
