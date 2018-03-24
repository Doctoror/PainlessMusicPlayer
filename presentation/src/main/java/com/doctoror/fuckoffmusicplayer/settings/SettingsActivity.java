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

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.domain.settings.Theme;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import org.parceler.Parcel;
import org.parceler.Parcels;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;

/**
 * Settings activity
 */
public final class SettingsActivity extends BaseActivity {

    private static final String TAG_DIALOG_DAYNIGHT_ACCURACY = "TAG_DIALOG_DAYNIGHT_ACCURACY";

    private static final String EXTRA_STATE = "EXTRA_STATE";

    @BindView(R.id.radioGroup)
    RadioGroup mRadioGroup;

    @BindView(R.id.checkboxScrobble)
    CompoundButton mBtnScrobble;

    @InjectExtra
    @Nullable
    Boolean suppressDayNightWarnings;

    @Inject
    DayNightModeMapper mDayNightModeMapper;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);

        Dart.inject(this);
        restoreInstanceState(savedInstanceState);

        initView();
        initActionBar();
    }

    private boolean suppressDayNightWarnings() {
        return suppressDayNightWarnings != null && suppressDayNightWarnings;
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
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        bindTheme(getSettings().getTheme());

        mRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            @Theme final int theme = buttonIdToTheme(id);
            getSettings().setTheme(theme);
            AppCompatDelegate.setDefaultNightMode(mDayNightModeMapper.toDayNightMode(theme));
            restart(Henson.with(SettingsActivity.this).gotoSettingsActivity()
                    .suppressDayNightWarnings(suppressDayNightWarnings)
                    .build());
        });

        mBtnScrobble.setChecked(getSettings().isScrobbleEnabled());
        mBtnScrobble.setOnCheckedChangeListener(
                (cb, value) -> getSettings().setScrobbleEnabled(value));
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final InstanceState instanceState = new InstanceState();
        instanceState.suppressDayNightWarnings = suppressDayNightWarnings;
        outState.putParcelable(EXTRA_STATE, Parcels.wrap(instanceState));
    }

    private void restoreInstanceState(@Nullable final Bundle instanceState) {
        if (instanceState != null) {
            final InstanceState state = Parcels.unwrap(instanceState.getParcelable(EXTRA_STATE));
            suppressDayNightWarnings = state.suppressDayNightWarnings;
        }
    }

    private void bindTheme(@Theme final int theme) {
        mRadioGroup.check(themeToButtonId(theme));
        if (theme == Theme.DAYNIGHT && !suppressDayNightWarnings()) {
            checkPermissions();
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

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            suppressDayNightWarnings = Boolean.TRUE;
            new DaynightAccuracyDialog().show(getFragmentManager(), TAG_DIALOG_DAYNIGHT_ACCURACY);
        }
    }

    @Parcel
    static final class InstanceState {

        @Nullable
        Boolean suppressDayNightWarnings;
    }
}
