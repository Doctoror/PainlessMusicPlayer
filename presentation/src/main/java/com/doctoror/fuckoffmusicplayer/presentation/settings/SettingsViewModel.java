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
package com.doctoror.fuckoffmusicplayer.presentation.settings;

import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableInt;

import com.doctoror.fuckoffmusicplayer.di.scopes.ActivityScope;

import javax.inject.Inject;

@ActivityScope
public final class SettingsViewModel {

    public final ObservableBoolean isScrobbleEnabled = new ObservableBoolean();
    public final ObservableInt themeCheckedItem = new ObservableInt();

    final ObservableBoolean dayNightAccuracyDialogShown = new ObservableBoolean();
    final ObservableBoolean restart = new ObservableBoolean();

    boolean suppressDayNightWarnings;

    @Inject
    SettingsViewModel() {
    }
}
