package com.doctoror.fuckoffmusicplayer.settings;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.wear.WearSupportMessedUpDialogFragment;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
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
    private static final String TAG_DIALOG_GMS_PROBLEM = "TAG_DIALOG_GMS_PROBLEM";
    private static final int REQUEST_CODE_RESOLVE_GMS = 1;

    @BindView(R.id.radioGroup)
    RadioGroup mRadioGroup;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initActionBar();
        initGoogleApiClient();
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

    private void initGoogleApiClient() {
        // Connect client to check of wear support is not messed up
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();
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

    @Theme.ThemeType
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
        if (!RxPermissions.getInstance(this)
                .isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            new DaynightAccuracyDialog().show(getFragmentManager(), TAG_DIALOG_DAYNIGHT_ACCURACY);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLVE_GMS) {
            // No matter the result, go through process again
            mGoogleApiClient.connect();
        }
    }

    private void onWearSupportMessedUp(@NonNull final ConnectionResult connectionResult) {
        final int code = connectionResult.getErrorCode();
        if (code != ConnectionResult.SUCCESS && code != ConnectionResult.SERVICE_UPDATING
                && areFragmentTransactionsAllowed()) {
            WearSupportMessedUpDialogFragment.show(this,
                    getFragmentManager(),
                    TAG_DIALOG_GMS_PROBLEM,
                    connectionResult,
                    REQUEST_CODE_RESOLVE_GMS);
        }
    }

    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener
            = this::onWearSupportMessedUp;
}
