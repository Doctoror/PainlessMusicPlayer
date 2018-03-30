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
