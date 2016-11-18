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
package com.doctoror.fuckoffmusicplayer.root;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.base.FragmentAwareActivity;
import com.doctoror.fuckoffmusicplayer.base.GoogleApiFragment;
import com.doctoror.fuckoffmusicplayer.base.LifecycleNotifierFragment;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityRootBinding;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingFragment;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistFragment;
import com.doctoror.fuckoffmusicplayer.util.GooglePlayServicesUtil;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.widget.Toast;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class RootActivity extends FragmentAwareActivity {

    private static final int REQUEST_CODE_GOOGLE_API = 1;

    private static final int ANIMATOR_CHILD_PRGORESS = 0;
    private static final int ANIMATOR_CHILD_CONTENT = 1;

    private final RootActivityModel mModelViewState = new RootActivityModel();
    private final Map<String, SoftReference<? extends Fragment>> mFragmentRefs = new HashMap<>();

    private GoogleApiClient mGoogleApiClient;
    private ActivityRootBinding mBinding;

    private boolean mFragmentTransactionsAllowed;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentTransactionsAllowed = true;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mOnConnectionFailedListener)
                .build();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_root);
        mBinding.setViewState(mModelViewState);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentTransactionsAllowed = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFragmentTransactionsAllowed = true;
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        final Fragment fragment = getCurrentFragment();
        if (fragment instanceof GoogleApiFragment) {
            ((GoogleApiFragment) fragment).onGoogleApiClientDisconnected();
        }
        mGoogleApiClient.disconnect();
    }

    private void setViewConnecting() {
        mModelViewState.setFixButtonVisible(false);
        mModelViewState.setProgressVisible(true);
        mModelViewState.setMessage(getText(R.string.Connecting));
        mModelViewState.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);

        mBinding.navigationDrawer.setAdapter(null);
    }

    private void setViewConnected() {
        mModelViewState.setFixButtonVisible(false);
        mModelViewState.setProgressVisible(false);

        if (mModelViewState.getAnimatorChild().get() != ANIMATOR_CHILD_CONTENT) {
            mModelViewState.setAnimatorChild(ANIMATOR_CHILD_CONTENT);

            mBinding.navigationDrawer.setAdapter(new RootNavigationAdapter(this,
                    this::onNavigationItemSelected));
            mBinding.drawerLayout.peekDrawer(Gravity.TOP);
        }
    }

    private void onNavigationItemSelected(final int id) {
        switch (id) {
            case RootNavigationAdapter.ID_NOW_PLAYING:
                showFragmentNowPlaying();
                break;

            case RootNavigationAdapter.ID_PLAYLIST:
                showFragmentPlaylist();
                break;

            default:
                break;
        }
    }

    private void showFragmentIfNone() {
        if (getCurrentFragment() == null) {
            showFragmentNowPlaying();
        }
    }

    private void showFragmentNowPlaying() {
        showFragment(NowPlayingFragment.class.getCanonicalName());
    }

    private void showFragmentPlaylist() {
        showFragment(PlaylistFragment.class.getCanonicalName());
    }

    private void showFragment(@NonNull final String fname) {
        if (mFragmentTransactionsAllowed) {
            final FragmentManager fm = getFragmentManager();
            final Fragment fragment = fm.findFragmentById(R.id.content);
            if (fragment == null || !fname.equals(fragment.getClass().getCanonicalName())) {
                final SoftReference<? extends Fragment> fRef = mFragmentRefs.get(fname);
                Fragment f = fRef != null ? fRef.get() : null;
                if (f == null) {
                    f = Fragment.instantiate(this, fname);
                    mFragmentRefs.put(fname, new SoftReference<>(f));
                }
                final FragmentTransaction ft = fm.beginTransaction();
                if (fragment == null) {
                    ft.add(R.id.content, f);
                } else {
                    ft.replace(R.id.content, f);
                }
                ft.commit();
            }
        }
    }

    @Override
    public void onFragmentStart(@NonNull final LifecycleNotifierFragment fragment) {
        super.onFragmentStart(fragment);
        if (mGoogleApiClient.isConnected() && fragment instanceof GoogleApiFragment) {
            ((GoogleApiFragment) fragment).onGoogleApiClientConnected(mGoogleApiClient);
        }
    }

    @Nullable
    private Fragment getCurrentFragment() {
        return getFragmentManager().findFragmentById(R.id.content);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_API && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            final Fragment f = getCurrentFragment();
            if (f instanceof GoogleApiFragment) {
                ((GoogleApiFragment) f).onGoogleApiClientConnected(mGoogleApiClient);
            }
            setViewConnected();
            showFragmentIfNone();
        }

        @Override
        public void onConnectionSuspended(final int i) {
            setViewConnecting();
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener
            = connectionResult -> {
        mModelViewState.setProgressVisible(false);
        mModelViewState.setMessage(GooglePlayServicesUtil
                .toHumanReadableMessage(getResources(), connectionResult.getErrorCode()));
        mModelViewState.setFixButtonVisible(connectionResult.hasResolution());
        if (connectionResult.hasResolution()) {
            mBinding.btnFix.setOnClickListener(v -> {
                try {
                    connectionResult
                            .startResolutionForResult(RootActivity.this, REQUEST_CODE_GOOGLE_API);
                } catch (IntentSender.SendIntentException e) {
                    Toast.makeText(RootActivity.this, R.string.Could_not_fix_this_issue,
                            Toast.LENGTH_LONG).show();
                    mModelViewState.setFixButtonVisible(false);
                }
            });
        }
        mModelViewState.setAnimatorChild(ANIMATOR_CHILD_PRGORESS);
    };
}
