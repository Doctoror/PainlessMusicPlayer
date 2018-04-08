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
import android.support.v4.content.ContextCompat
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable

class LibraryPermissionsProvider(
        private val context: Context,
        private val rxPermissions: RxPermissions) {

    fun permissionsGranted() = ContextCompat.checkSelfPermission(context,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    fun requestPermission(): Observable<Boolean> {
        RuntimePermissions.setPermissionsRequested()
        return rxPermissions.request(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}
