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

import android.arch.lifecycle.Lifecycle.Event.ON_START
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.VisibleForTesting
import com.doctoror.commons.reactivex.SchedulersProvider
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.presentation.base.BasePresenter
import io.reactivex.Completable
import kotlinx.android.parcel.Parcelize
import java.util.concurrent.TimeUnit

abstract class LibraryPermissionsPresenter(
        private val libraryPermissionProvider: LibraryPermissionsProvider,
        private val runtimePermissions: RuntimePermissions,
        private val schedulersProvider: SchedulersProvider) : BasePresenter() {

    private val permissionRequestDelay = 500L
    private val keyInstanceState = "LibraryPermissionsPresenter.INSTANCE_STATE"

    @VisibleForTesting
    var permissionRequested = runtimePermissions.permissionsRequested

    @OnLifecycleEvent(ON_START)
    fun onStart() {
        requestPermissionIfNeeded()
    }

    fun restoreInstanceState(savedInstanceState: Bundle) {
        val state = savedInstanceState.getParcelable<InstanceState>(keyInstanceState)
        if (state != null) {
            permissionRequested = state.permissionsRequested
                    || runtimePermissions.permissionsRequested
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        val state = InstanceState(permissionRequested)
        outState.putParcelable(keyInstanceState, state)
    }

    fun requestPermission() {
        permissionRequested = true
        libraryPermissionProvider.requestPermission()
                .subscribe { granted ->
                    if (granted) {
                        onPermissionGranted()
                    } else {
                        onPermissionDenied()
                    }
                }
    }

    protected abstract fun onPermissionDenied()

    protected abstract fun onPermissionGranted()

    private fun requestPermissionIfNeeded() {
        when {
            libraryPermissionProvider.permissionsGranted() -> onPermissionGranted()
            permissionRequested -> onPermissionDenied()
            else -> disposeOnStop(Completable
                    .timer(
                            permissionRequestDelay,
                            TimeUnit.MILLISECONDS,
                            schedulersProvider.mainThread())
                    .subscribe { requestPermission() })
        }
    }

    @Parcelize
    private data class InstanceState(val permissionsRequested: Boolean) : Parcelable
}
