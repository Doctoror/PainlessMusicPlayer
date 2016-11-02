/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer;

import com.google.android.exoplayer2.audio.AudioTrack;

import com.doctoror.fuckoffmusicplayer.settings.Theme;

import android.app.Application;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by Yaroslav Mytkalyk on 18.10.16.
 */
public final class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(Theme.getInstance(this).getDayNightMode());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AudioTrack.enablePreV21AudioSessionWorkaround = true;
        }
    }
}
