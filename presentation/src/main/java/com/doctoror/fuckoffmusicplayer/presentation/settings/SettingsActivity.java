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
package com.doctoror.fuckoffmusicplayer.presentation.settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivitySettingsBinding;
import com.doctoror.fuckoffmusicplayer.presentation.Henson;
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.presentation.mvvm.OnPropertyChangedCallback;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import org.parceler.Parcel;
import org.parceler.Parcels;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Settings activity
 */
public final class SettingsActivity extends BaseActivity {

    private static final String TAG_DIALOG_DAYNIGHT_ACCURACY = "TAG_DIALOG_DAYNIGHT_ACCURACY";

    private static final String EXTRA_STATE = "EXTRA_STATE";

    private final DayNightAccuracyDialogCallback dayNightAccuracyDialogCallback
            = new DayNightAccuracyDialogCallback();

    private final RestartCallback restartCallback = new RestartCallback();

    @InjectExtra
    @Nullable
    Boolean suppressDayNightWarnings;

    @Inject
    SettingsPresenter presenter;

    @Inject
    SettingsViewModel viewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);

        Dart.inject(this);
        restoreInstanceState(savedInstanceState);

        initView();
        initActionBar();

        getLifecycle().addObserver(presenter);
    }

    private void initActionBar() {
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    private void initView() {
        viewModel.suppressDayNightWarnings =
                suppressDayNightWarnings != null && suppressDayNightWarnings;

        final ActivitySettingsBinding binding = DataBindingUtil
                .setContentView(this, R.layout.activity_settings);

        binding.setModel(viewModel);
        binding.executePendingBindings();

        binding.radioGroup.setOnCheckedChangeListener((radioGroup, id) ->
                presenter.onThemeClick(id)
        );

        binding.checkboxScrobble.setOnCheckedChangeListener(
                (cb, value) -> presenter.onScrobbleEnabled(value));
    }

    private void restoreInstanceState(@Nullable final Bundle instanceState) {
        if (instanceState != null) {
            final InstanceState state = Parcels.unwrap(instanceState.getParcelable(EXTRA_STATE));
            suppressDayNightWarnings = state.suppressDayNightWarnings;
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final InstanceState instanceState = new InstanceState();
        instanceState.suppressDayNightWarnings = viewModel.suppressDayNightWarnings;
        outState.putParcelable(EXTRA_STATE, Parcels.wrap(instanceState));
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindViewModel();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindViewModel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(presenter);
    }

    private void bindViewModel() {
        viewModel.dayNightAccuracyDialogShown
                .addOnPropertyChangedCallback(dayNightAccuracyDialogCallback);

        viewModel.restart.addOnPropertyChangedCallback(restartCallback);

        dayNightAccuracyDialogCallback.onPropertyChanged2(viewModel.dayNightAccuracyDialogShown, 0);
    }

    private void unbindViewModel() {
        viewModel.restart.removeOnPropertyChangedCallback(restartCallback);

        viewModel.dayNightAccuracyDialogShown
                .removeOnPropertyChangedCallback(dayNightAccuracyDialogCallback);
    }

    private final class DayNightAccuracyDialogCallback
            extends OnPropertyChangedCallback<ObservableBoolean> {

        @Override
        protected void onPropertyChanged2(
                @NonNull final ObservableBoolean sender, final int propertyId) {
            if (sender.get()) {
                sender.set(false);
                new DaynightAccuracyDialog().show(
                        getSupportFragmentManager(), TAG_DIALOG_DAYNIGHT_ACCURACY);
            }
        }
    }

    private final class RestartCallback extends OnPropertyChangedCallback<ObservableBoolean> {

        @Override
        protected void onPropertyChanged2(@NonNull ObservableBoolean sender, int propertyId) {
            if (sender.get()) {
                sender.set(false);
                restart(Henson.with(SettingsActivity.this).gotoSettingsActivity()
                        .suppressDayNightWarnings(viewModel.suppressDayNightWarnings)
                        .build());
            }
        }
    }

    @Parcel
    static final class InstanceState {

        boolean suppressDayNightWarnings;
    }
}
