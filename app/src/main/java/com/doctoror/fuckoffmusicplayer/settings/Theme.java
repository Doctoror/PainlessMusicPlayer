package com.doctoror.fuckoffmusicplayer.settings;

import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatDelegate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Yaroslav Mytkalyk on 02.11.16.
 */
public final class Theme {

    static final int NIGHT = 0;
    static final int DAY = 1;
    static final int DAYNIGHT = 2;

    @IntDef({
            NIGHT,
            DAY,
            DAYNIGHT
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ThemeType {

    }

    @AppCompatDelegate.NightMode
    public static int getDayNightMode(@ThemeType final int theme) {
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
