/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.doctoror.fuckoffmusicplayer.domain.settings.Theme;

import javax.inject.Inject;

public final class DayNightModeMapper {

    @Inject
    DayNightModeMapper() {

    }

    @AppCompatDelegate.NightMode
    public int toDayNightMode(@NonNull final Theme theme) {
        switch (theme) {
            case NIGHT:
                return AppCompatDelegate.MODE_NIGHT_YES;

            case DAY:
                return AppCompatDelegate.MODE_NIGHT_NO;

            case DAYNIGHT:
                return AppCompatDelegate.MODE_NIGHT_AUTO;

            default:
                throw new IllegalArgumentException("Unexpected theme: " + theme);
        }
    }
}
