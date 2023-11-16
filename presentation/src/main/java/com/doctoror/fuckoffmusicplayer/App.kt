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
package com.doctoror.fuckoffmusicplayer

import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings
import com.doctoror.fuckoffmusicplayer.presentation.settings.DayNightModeMapper
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import javax.inject.Inject

class App : DaggerApplication() {

    @Inject
    lateinit var dayNightModeMapper: DayNightModeMapper

    @Inject
    lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()
        initLogger()
        initStrictMode()
        initTheme()
    }

    private fun initLogger() {
        Log.setLogV(BuildConfig.DEBUG)
        Log.setLogD(BuildConfig.DEBUG)
        Log.setLogI(BuildConfig.DEBUG)
        Log.setLogW(BuildConfig.DEBUG)
        Log.setLogWtf(BuildConfig.DEBUG)
    }

    private fun initStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())

            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }

    private fun initTheme() {
        AppCompatDelegate.setDefaultNightMode(
                dayNightModeMapper.toDayNightMode(settings.theme))
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerHolder.getInstance(this).mainComponent()
    }
}
