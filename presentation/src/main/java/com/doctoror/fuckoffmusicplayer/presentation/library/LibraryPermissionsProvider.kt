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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.MainThread
import androidx.core.content.ContextCompat
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.doctoror.fuckoffmusicplayer.presentation.rxpermissions.RxPermissionsProvider
import io.reactivex.Observable

class LibraryPermissionsProvider(
        private val context: Context,
        private val runtimePermissions: RuntimePermissions,
        private val rxPermissionsProvider: RxPermissionsProvider) {

    fun permissionsGranted() = ContextCompat.checkSelfPermission(context, getPermission()) ==
            PackageManager.PERMISSION_GRANTED

    @MainThread
    fun requestPermission(): Observable<Boolean> {
        runtimePermissions.permissionsRequested = true
        return rxPermissionsProvider
                .provideRxPermissions()
                .request(getPermission())
    }

    private fun getPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
}
