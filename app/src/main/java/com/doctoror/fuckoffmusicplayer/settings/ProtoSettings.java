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
public final class ProtoSettings implements Settings {

    // Not a leak
    @SuppressLint("StaticFieldLeak")
    private static volatile ProtoSettings sInstance;

    @NonNull
    public static ProtoSettings getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (ProtoSettings.class) {
                if (sInstance == null) {
                    sInstance = new ProtoSettings(context.getApplicationContext());
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

    private boolean mScrobbleEnabled = true;

    private ProtoSettings(@NonNull final Context context) {
        mContext = context;
        final SettingsProto.Settings settings = ProtoUtils.readFromFile(context, FILE_NAME,
                new SettingsProto.Settings());
        synchronized (mLock) {
            if (settings != null) {
                mThemeType = settings.theme;
                mScrobbleEnabled = settings.scrobbleEnabled;
            }
        }
    }

    @Override
    public boolean isScrobbleEnabled() {
        synchronized (mLock) {
            return mScrobbleEnabled;
        }
    }

    @Override
    public void setScrobbleEnabled(final boolean enabled) {
        synchronized (mLock) {
            if (mScrobbleEnabled != enabled) {
                mScrobbleEnabled = enabled;
                persistAsync();
            }
        }
    }

    @Override
    @Theme.ThemeType
    public int getThemeType() {
        synchronized (mLock) {
            return mThemeType;
        }
    }

    @Override
    public void setThemeType(@Theme.ThemeType final int themeType) {
        synchronized (mLock) {
            if (mThemeType != themeType) {
                mThemeType = themeType;
                AppCompatDelegate.setDefaultNightMode(Theme.getDayNightMode(themeType));
                persistAsync();
            }
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
            settings.scrobbleEnabled = mScrobbleEnabled;
        }
        synchronized (mLockIO) {
            ProtoUtils.writeToFile(mContext, FILE_NAME, settings);
        }
    }
}
