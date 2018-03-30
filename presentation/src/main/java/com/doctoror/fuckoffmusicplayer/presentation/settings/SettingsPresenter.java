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

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.di.scopes.ActivityScope;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;
import com.doctoror.fuckoffmusicplayer.domain.settings.Theme;
import com.doctoror.fuckoffmusicplayer.presentation.base.BasePresenter;

import javax.inject.Inject;

@ActivityScope
final class SettingsPresenter extends BasePresenter {

    private final DayNightModeMapper dayNightModeMapper;
    private final DayNightPermissionProvider dayNightPermissionProvider;
    private final SettingsViewModel viewModel;
    private final Settings settings;

    @Inject
    SettingsPresenter(
            @NonNull final DayNightModeMapper dayNightModeMapper,
            @NonNull final DayNightPermissionProvider dayNightPermissionProvider,
            @NonNull final SettingsViewModel viewModel,
            @NonNull final Settings settings) {
        this.dayNightModeMapper = dayNightModeMapper;
        this.dayNightPermissionProvider = dayNightPermissionProvider;
        this.viewModel = viewModel;
        this.settings = settings;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        viewModel.themeCheckedItem.set(themeToButtonId(settings.getTheme()));
        viewModel.isScrobbleEnabled.set(settings.isScrobbleEnabled());

        requestDayNightAccuracyIfRequired();
    }

    void onScrobbleEnabled(final boolean enabled) {
        if (settings.isScrobbleEnabled() != enabled) {
            settings.setScrobbleEnabled(enabled);
        }
    }

    void onThemeClick(@IdRes final int buttonId) {
        @Theme final int theme = buttonIdToTheme(buttonId);
        if (settings.getTheme() != theme) {
            settings.setTheme(theme);
            AppCompatDelegate.setDefaultNightMode(dayNightModeMapper.toDayNightMode(theme));

            viewModel.restart.set(true);
        }
    }

    private void requestDayNightAccuracyIfRequired() {
        if (!viewModel.suppressDayNightWarnings && settings.getTheme() == Theme.DAYNIGHT) {
            requestDayNightPermissionIfRequired();
        }
    }

    private void requestDayNightPermissionIfRequired() {
        if (!dayNightPermissionProvider.hasPermission()) {
            viewModel.suppressDayNightWarnings = true;
            viewModel.dayNightAccuracyDialogShown.set(true);
        }
    }

    @IdRes
    private static int themeToButtonId(@Theme final int theme) {
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
    private static int buttonIdToTheme(@IdRes final int buttonId) {
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
