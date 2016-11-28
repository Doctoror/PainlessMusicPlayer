/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer;

import com.doctoror.fuckoffmusicplayer.settings.Theme;
import com.f2prateek.dart.InjectExtra;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public abstract class BaseActivity extends AppCompatActivity {

    private Theme mTheme;

    @Theme.ThemeType
    private int mThemeUsed;

    private boolean mFragmentTransactionsAllowed;

    private boolean mFinishingAfterTransition;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mTheme = Theme.getInstance(this);
        mThemeUsed = mTheme.getThemeType();

        mFragmentTransactionsAllowed = true;
        mFinishingAfterTransition = false;
    }

    public boolean isFinishingAfterTransition() {
        return mFinishingAfterTransition;
    }

    protected final Theme getTheme1() {
        return mTheme;
    }

    protected final void restart() {
        restart(new Intent(this, getClass()));
    }

    protected final void restart(@NonNull final Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        finish();
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFragmentTransactionsAllowed = true;
        // Theme changed while this Activity was in background
        if (mThemeUsed != mTheme.getThemeType()) {
            restart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFragmentTransactionsAllowed = true;
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentTransactionsAllowed = false;
    }

    protected final boolean areFragmentTransactionsAllowed() {
        return mFragmentTransactionsAllowed;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void finishAfterTransition() {
        mFinishingAfterTransition = true;
        super.finishAfterTransition();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final Intent parent = getParentActivityIntent();
                if (parent != null) {
                    if (!navigateUpTo(getParentActivityIntent())) {
                        ActivityCompat.finishAfterTransition(this);
                    }
                } else {
                    ActivityCompat.finishAfterTransition(this);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        final Intent intent = super.getParentActivityIntent();
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        return intent;
    }

    @Override
    public boolean navigateUpTo(final Intent upIntent) {
        ComponentName destInfo = upIntent.getComponent();
        if (destInfo == null) {
            destInfo = upIntent.resolveActivity(getPackageManager());
            if (destInfo == null) {
                return false;
            }
        }

        startActivity(upIntent);
        ActivityCompat.finishAfterTransition(this);
        return true;
    }
}
