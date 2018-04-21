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
package com.doctoror.fuckoffmusicplayer.data.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.fuckoffmusicplayer.data.settings.nano.SettingsProto;
import com.doctoror.fuckoffmusicplayer.data.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;
import com.doctoror.fuckoffmusicplayer.domain.settings.Theme;
import com.doctoror.fuckoffmusicplayer.domain.settings.ThemeKt;

import io.reactivex.Completable;

/**
 * Application settings
 */
public final class SettingsImpl implements Settings {

    private static final String FILE_NAME = "settings";

    private final Object lock = new Object();
    private final Object lockIo = new Object();

    @NonNull
    private final Context context;

    @NonNull
    private final SchedulersProvider schedulersProvider;

    @NonNull
    private Theme theme = Theme.NIGHT;

    private boolean scrobbleEnabled = true;

    public SettingsImpl(
            @NonNull final Context context,
            @NonNull final SchedulersProvider schedulersProvider) {
        this.context = context;
        this.schedulersProvider = schedulersProvider;
        final SettingsProto.Settings settings = ProtoUtils.readFromFile(context, FILE_NAME,
                new SettingsProto.Settings());
        synchronized (lock) {
            if (settings != null) {
                theme = ThemeKt.themeFromIndex(settings.theme);
                scrobbleEnabled = settings.scrobbleEnabled;
            }
        }
    }

    @Override
    public boolean isScrobbleEnabled() {
        synchronized (lock) {
            return scrobbleEnabled;
        }
    }

    @Override
    public void setScrobbleEnabled(final boolean enabled) {
        synchronized (lock) {
            if (scrobbleEnabled != enabled) {
                scrobbleEnabled = enabled;
                persistAsync();
            }
        }
    }

    @NonNull
    @Override
    public Theme getTheme() {
        synchronized (lock) {
            return theme;
        }
    }

    @Override
    public void setTheme(@NonNull final Theme theme) {
        synchronized (lock) {
            if (this.theme != theme) {
                this.theme = theme;
                persistAsync();
            }
        }
    }

    private void persistAsync() {
        Completable
                .fromAction(this::persist)
                .subscribeOn(schedulersProvider.io())
                .subscribe();
    }

    @WorkerThread
    private void persist() {
        final SettingsProto.Settings settings = new SettingsProto.Settings();
        synchronized (lock) {
            settings.theme = theme.getIndex();
            settings.scrobbleEnabled = scrobbleEnabled;
        }
        synchronized (lockIo) {
            ProtoUtils.writeToFile(context, FILE_NAME, settings);
        }
    }
}
