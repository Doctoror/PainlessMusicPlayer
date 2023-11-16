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

import androidx.annotation.VisibleForTesting
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import androidx.recyclerview.widget.RecyclerView

class RecentActivityViewModel {

    @VisibleForTesting
    val animatorChildProgress = 0

    @VisibleForTesting
    val animatorChildPermissionDenied = 1

    @VisibleForTesting
    val animatorChildEmpty = 2

    @VisibleForTesting
    val animatorChildError = 3

    @VisibleForTesting
    val animatorChildContent = 4

    val displayedChild = ObservableInt()
    val recyclerAdapter = ObservableField<RecyclerView.Adapter<RecyclerView.ViewHolder>>()

    fun showViewProgress() {
        displayedChild.set(animatorChildProgress)
    }

    fun showViewPermissionDenied() {
        displayedChild.set(animatorChildPermissionDenied)
    }

    fun showViewEmpty() {
        displayedChild.set(animatorChildEmpty)
    }

    fun showViewError() {
        displayedChild.set(animatorChildError)
    }

    fun showViewContent() {
        displayedChild.set(animatorChildContent)
    }
}
