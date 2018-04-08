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
package com.doctoror.fuckoffmusicplayer.presentation.rxpermissions

import android.app.Activity
import android.support.annotation.MainThread
import com.tbruyelle.rxpermissions2.RxPermissions

class RxPermissionsProvider(private val activity: Activity) {

    private var rxPermissions: RxPermissions? = null

    @MainThread
    fun provideRxPermissions(): RxPermissions {
        if (rxPermissions == null) {
            rxPermissions = RxPermissions(activity)
        }
        return rxPermissions!!
    }
}
