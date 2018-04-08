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

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.RecyclerView

/**
 * Data binding model for [LibraryListFragment]
 */
class LibraryListModel {

    val displayedChild = ObservableInt()
    val emptyMessage = ObservableField<CharSequence>()
    val recyclerAdapter = ObservableField<RecyclerView.Adapter<RecyclerView.ViewHolder>>()

    fun setEmptyMessage(emptyMessage: CharSequence?) {
        this.emptyMessage.set(emptyMessage)
    }

    fun setRecyclerAdapter(adapter: RecyclerView.Adapter<out RecyclerView.ViewHolder>?) {
        @Suppress("UNCHECKED_CAST")
        recyclerAdapter.set(adapter as RecyclerView.Adapter<RecyclerView.ViewHolder>)
    }

    fun showViewProgress() {
        displayedChild.set(ANIMATOR_CHILD_PROGRESS)
    }

    fun showViewPermissionDenied() {
        displayedChild.set(ANIMATOR_CHILD_PERMISSION_DENIED)
    }

    fun showViewEmpty() {
        displayedChild.set(ANIMATOR_CHILD_EMPTY)
    }

    fun showViewError() {
        displayedChild.set(ANIMATOR_CHILD_ERROR)
    }

    fun showViewContent() {
        displayedChild.set(ANIMATOR_CHILD_CONTENT)
    }

    companion object {

        @VisibleForTesting
        internal const val ANIMATOR_CHILD_PROGRESS = 0

        @VisibleForTesting
        internal const val ANIMATOR_CHILD_PERMISSION_DENIED = 1

        @VisibleForTesting
        internal const val ANIMATOR_CHILD_EMPTY = 2

        @VisibleForTesting
        internal const val ANIMATOR_CHILD_ERROR = 3

        @VisibleForTesting
        internal const val ANIMATOR_CHILD_CONTENT = 4
    }
}
