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
    private final ThemeToButtonIdMapper themeToButtonIdMapper;

    @Inject
    SettingsPresenter(
            @NonNull final DayNightModeMapper dayNightModeMapper,
            @NonNull final DayNightPermissionProvider dayNightPermissionProvider,
            @NonNull final SettingsViewModel viewModel,
            @NonNull final Settings settings,
            @NonNull final ThemeToButtonIdMapper themeToButtonIdMapper) {
        this.dayNightModeMapper = dayNightModeMapper;
        this.dayNightPermissionProvider = dayNightPermissionProvider;
        this.viewModel = viewModel;
        this.settings = settings;
        this.themeToButtonIdMapper = themeToButtonIdMapper;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        viewModel.themeCheckedItem.set(themeToButtonIdMapper.themeToButtonId(settings.getTheme()));
        viewModel.isScrobbleEnabled.set(settings.isScrobbleEnabled());

        requestDayNightAccuracyIfRequired();
    }

    void onScrobbleEnabled(final boolean enabled) {
        if (settings.isScrobbleEnabled() != enabled) {
            settings.setScrobbleEnabled(enabled);
        }
    }

    void onThemeClick(@IdRes final int buttonId) {
        final Theme theme = themeToButtonIdMapper.buttonIdToTheme(buttonId);
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
}
