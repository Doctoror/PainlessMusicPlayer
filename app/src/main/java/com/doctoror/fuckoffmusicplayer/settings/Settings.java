package com.doctoror.fuckoffmusicplayer.settings;

import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.settings.nano.SettingsProto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatDelegate;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 25.12.16.
 */

public final class Settings {

    // Not a leak
    @SuppressLint("StaticFieldLeak")
    private static volatile Settings sInstance;

    @NonNull
    public static Settings getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (Settings.class) {
                if (sInstance == null) {
                    sInstance = new Settings(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private static final String FILE_NAME = "settings";

    private final Object LOCK = new Object();
    private final Object LOCK_IO = new Object();

    @NonNull
    private final Context mContext;

    @Theme.ThemeType
    private int mThemeType = Theme.NIGHT;

    private int mLibraryTab;

    private boolean mScrobbleEnabled = true;

    private Settings(@NonNull final Context context) {
        mContext = context;
        final SettingsProto.Settings settings = ProtoUtils.readFromFile(context, FILE_NAME,
                new SettingsProto.Settings());
        synchronized (LOCK) {
            if (settings != null) {
                mThemeType = settings.theme;
                mLibraryTab = settings.libraryTab;
                mScrobbleEnabled = settings.scrobbleEnabled;
            }
        }
    }

    public boolean isScrobbleEnabled() {
        synchronized (LOCK) {
            return mScrobbleEnabled;
        }
    }

    public void setScrobbleEnabled(final boolean enabled) {
        synchronized (LOCK) {
            if (mScrobbleEnabled != enabled) {
                mScrobbleEnabled = enabled;
                persistAsync();
            }
        }
    }

    @Theme.ThemeType
    public int getThemeType() {
        synchronized (LOCK) {
            return mThemeType;
        }
    }

    public void setThemeType(@Theme.ThemeType final int themeType) {
        synchronized (LOCK) {
            if (mThemeType != themeType) {
                mThemeType = themeType;
                AppCompatDelegate.setDefaultNightMode(Theme.getDayNightMode(themeType));
                persistAsync();
            }
        }
    }

    public void setLibraryTab(final int tab) {
        synchronized (LOCK) {
            if (mLibraryTab != tab) {
                mLibraryTab = tab;
                persistAsync();
            }
        }
    }

    public int getLibraryTab() {
        synchronized (LOCK) {
            return mLibraryTab;
        }
    }

    private void persistAsync() {
        Observable.create(s -> persist()).subscribeOn(Schedulers.io()).subscribe();
    }

    @WorkerThread
    private void persist() {
        final SettingsProto.Settings settings = new SettingsProto.Settings();
        synchronized (LOCK) {
            settings.theme = mThemeType;
            settings.libraryTab = mLibraryTab;
        }
        synchronized (LOCK_IO) {
            ProtoUtils.writeToFile(mContext, FILE_NAME, settings);
        }
    }
}
