package com.doctoror.fuckoffmusicplayer.settings;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Yaroslav Mytkalyk on 02.11.16.
 */

public final class SettingsActivity extends BaseActivity {

    private static final String TAG_DIALOG_DAYNIGHT_ACCURACY = "TAG_DIALOG_DAYNIGHT_ACCURACY";

    @BindView(R.id.radioGroup)
    RadioGroup mRadioGroup;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initActionBar();
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
        bindTheme(getTheme1().getThemeType());

        mRadioGroup.setOnCheckedChangeListener((radioGroup, id) -> {
            getTheme1().setThemeType(buttonIdToTheme(id));
            restart();
        });
    }

    private void bindTheme(@Theme.ThemeType final int theme) {
        mRadioGroup.check(themeToButtonId(theme));
        if (theme == Theme.DAYNIGHT) {
            checkPermissions();
        }
    }

    @IdRes
    private static int themeToButtonId(@Theme.ThemeType final int theme) {
        switch (theme) {
            case Theme.DAYNIGHT:
                return R.id.radioDayNight;

            case Theme.DAY:
                return R.id.radioDay;

            case Theme.NIGHT:
                return R.id.radioNight;

            default:
                throw new IllegalArgumentException("Unexpected theme: " + theme);
        }
    }

    @Theme.ThemeType
    private static int buttonIdToTheme(@IdRes final int buttonId) {
        switch (buttonId) {
            case R.id.radioDayNight:
                return Theme.DAYNIGHT;

            case R.id.radioDay:
                return Theme.DAY;

            case R.id.radioNight:
                return Theme.NIGHT;

            default:
                throw new IllegalArgumentException("Unexpected button id: " + buttonId);
        }
    }

    private void checkPermissions() {
        if (!RxPermissions.getInstance(this)
                .isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new DaynightAccuracyDialog().show(getFragmentManager(), TAG_DIALOG_DAYNIGHT_ACCURACY);
        }
    }
}
