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
 * Application settings
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

    private final Object mLock = new Object();
    private final Object mLockIO = new Object();

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
        synchronized (mLock) {
            if (settings != null) {
                mThemeType = settings.theme;
                mLibraryTab = settings.libraryTab;
                mScrobbleEnabled = settings.scrobbleEnabled;
            }
        }
    }

    public boolean isScrobbleEnabled() {
        synchronized (mLock) {
            return mScrobbleEnabled;
        }
    }

    public void setScrobbleEnabled(final boolean enabled) {
        synchronized (mLock) {
            if (mScrobbleEnabled != enabled) {
                mScrobbleEnabled = enabled;
                persistAsync();
            }
        }
    }

    @Theme.ThemeType
    public int getThemeType() {
        synchronized (mLock) {
            return mThemeType;
        }
    }

    public void setThemeType(@Theme.ThemeType final int themeType) {
        synchronized (mLock) {
            if (mThemeType != themeType) {
                mThemeType = themeType;
                AppCompatDelegate.setDefaultNightMode(Theme.getDayNightMode(themeType));
                persistAsync();
            }
        }
    }

    public void setLibraryTab(final int tab) {
        synchronized (mLock) {
            if (mLibraryTab != tab) {
                mLibraryTab = tab;
                persistAsync();
            }
        }
    }

    public int getLibraryTab() {
        synchronized (mLock) {
            return mLibraryTab;
        }
    }

    private void persistAsync() {
        Observable.create(s -> persist()).subscribeOn(Schedulers.io()).subscribe();
    }

    @WorkerThread
    private void persist() {
        final SettingsProto.Settings settings = new SettingsProto.Settings();
        synchronized (mLock) {
            settings.theme = mThemeType;
            settings.libraryTab = mLibraryTab;
        }
        synchronized (mLockIO) {
            ProtoUtils.writeToFile(mContext, FILE_NAME, settings);
        }
    }
}
