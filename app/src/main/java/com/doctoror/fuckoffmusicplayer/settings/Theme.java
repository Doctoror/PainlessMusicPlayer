package com.doctoror.fuckoffmusicplayer.settings;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Yaroslav Mytkalyk on 02.11.16.
 */
public final class Theme {

    private static volatile Theme sTheme;

    @NonNull
    public static Theme getInstance(@NonNull final Context context) {
        if (sTheme == null) {
            synchronized (Theme.class) {
                if (sTheme == null) {
                    sTheme = new Theme(context.getApplicationContext());
                }
            }
        }
        return sTheme;
    }

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

    private final ThemePrefs mPrefs;

    @ThemeType
    private int mThemeType;

    private Theme(@NonNull final Context context) {
        mPrefs = ThemePrefs.with(context);
        //noinspection WrongConstant
        mThemeType = mPrefs.getTheme();
    }

    void setThemeType(@ThemeType final int themeType) {
        if (mThemeType != themeType) {
            mThemeType = themeType;
            mPrefs.setTheme(themeType);
            AppCompatDelegate.setDefaultNightMode(getDayNightMode(themeType));
        }
    }

    @ThemeType
    public int getThemeType() {
        return mThemeType;
    }

    @AppCompatDelegate.NightMode
    public int getDayNightMode() {
        return getDayNightMode(mThemeType);
    }

    @AppCompatDelegate.NightMode
    private static int getDayNightMode(@ThemeType final int theme) {
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
