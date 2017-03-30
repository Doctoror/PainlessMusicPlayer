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
package com.doctoror.fuckoffmusicplayer.base;

import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.settings.Settings;
import com.doctoror.fuckoffmusicplayer.settings.Theme;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public abstract class BaseActivity extends AppCompatActivity {

    private final Collection<Disposable> mDisposables = new ArrayList<>();

    @Theme.ThemeType
    private int mThemeUsed;

    private boolean mFragmentTransactionsAllowed;

    private boolean mFinishingAfterTransition;

    @Inject
    Settings mSettings;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        DaggerHolder.getInstance(this).mainComponent().inject(this);

        mThemeUsed = mSettings.getThemeType();

        mFragmentTransactionsAllowed = true;
        mFinishingAfterTransition = false;
    }

    /**
     * Register a {@link Disposable} that will be disposed onStop()
     *
     * @param disposable the {@link Disposable} to register
     * @return the registered {@link Disposable}
     */
    @NonNull
    @MainThread
    public Disposable registerOnStartSubscription(@NonNull final Disposable disposable) {
        //noinspection ConstantConditions
        if (disposable == null) {
            throw new NullPointerException("disposable must not be null");
        }
        mDisposables.add(disposable);
        return disposable;
    }

    public Settings getSettings() {
        return mSettings;
    }

    public boolean isFinishingAfterTransition() {
        return mFinishingAfterTransition;
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
        if (mThemeUsed != mSettings.getThemeType()) {
            restart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFragmentTransactionsAllowed = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (final Disposable d : mDisposables) {
            d.dispose();
        }
        mDisposables.clear();
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
    public boolean shouldUpRecreateTask(final Intent targetIntent) {
        // The default method returns false when launched from notification and task was swiped out
        // from recents
        // http://stackoverflow.com/questions/19999619/navutils-navigateupto-does-not-start-any-activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (BaseActivityMarshmallow.shouldUpRecreateTask(this)) {
                return true;
            }
        }

        return super.shouldUpRecreateTask(targetIntent);
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

        if (shouldUpRecreateTask(upIntent)) {
            startActivity(upIntent);
            finish();
        } else {
            ActivityCompat.finishAfterTransition(this);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private static final class BaseActivityMarshmallow {

        private BaseActivityMarshmallow() {
            throw new UnsupportedOperationException();
        }

        static boolean shouldUpRecreateTask(@NonNull final Activity activity) {
            // The default method returns false when launched from notification and task was swiped out
            // from recents
            // http://stackoverflow.com/questions/19999619/navutils-navigateupto-does-not-start-any-activity
            final ActivityManager activityManager = (ActivityManager) activity.getSystemService(
                    Context.ACTIVITY_SERVICE);
            final List<ActivityManager.AppTask> tasks = activityManager.getAppTasks();
            for (final ActivityManager.AppTask t : tasks) {
                if (t.getTaskInfo().numActivities == 1) {
                    // If only one activity, we should recreate task
                    return true;
                }
            }
            return false;
        }

    }
}
