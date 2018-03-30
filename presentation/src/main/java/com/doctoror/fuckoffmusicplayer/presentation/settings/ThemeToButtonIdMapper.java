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

import android.support.annotation.IdRes;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.settings.Theme;

import javax.inject.Inject;

final class ThemeToButtonIdMapper {

    @Inject
    ThemeToButtonIdMapper() {

    }

    @IdRes
    int themeToButtonId(@Theme final int theme) {
        switch (theme) {
            case Theme.DAY:
                return R.id.radioDay;

            case Theme.NIGHT:
                return R.id.radioNight;

            case Theme.DAYNIGHT:
                return R.id.radioDayNight;

            default:
                throw new IllegalArgumentException("Unexpected theme: " + theme);
        }
    }

    @Theme
    int buttonIdToTheme(@IdRes final int buttonId) {
        switch (buttonId) {
            case R.id.radioDay:
                return Theme.DAY;

            case R.id.radioNight:
                return Theme.NIGHT;

            case R.id.radioDayNight:
                return Theme.DAYNIGHT;

            default:
                throw new IllegalArgumentException("Unexpected button id: " + buttonId);
        }
    }
}
