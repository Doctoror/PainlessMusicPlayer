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
import com.doctoror.fuckoffmusicplayer.remote.RemoteControl;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityRootBinding;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingFragment;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistFragment;
import com.doctoror.fuckoffmusicplayer.search.SearchFragment;
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
import android.support.wearable.activity.WearableActivity;
import android.view.Gravity;
import android.widget.Toast;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

public final class RootActivity extends WearableActivity {

    private static final int REQUEST_CODE_GOOGLE_API = 1;

    private final RemoteControl mRemoteControl = RemoteControl.getInstance();
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

        mBinding.navigationDrawer.setAdapter(new RootNavigationAdapter(this,
                this::onNavigationItemSelected));
        mBinding.drawerLayout.peekDrawer(Gravity.TOP);

        if (savedInstanceState == null) {
            showFragmentNowPlaying();
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        mFragmentTransactionsAllowed = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mModelViewState.setHandheldConnected(false);
        mModelViewState.setFixButtonVisible(false);
        mModelViewState.setMessageDrawableTop(null);
        mModelViewState.setMessage(null);

        mFragmentTransactionsAllowed = true;
        mRemoteControl.setPlaybackNodeListener(c -> {
            mModelViewState.setHandheldConnected(c);
            if (c) {
                mModelViewState.setMessageDrawableTop(null);
                mModelViewState.setMessage(null);
            } else {
                mModelViewState.setMessageDrawableTop(
                        getDrawable(R.drawable.ic_bluetooth_disabled_white_48dp));
                mModelViewState.setMessage(getText(R.string.Handheld_not_connected));
            }
        });
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRemoteControl.onGoogleApiClientDisconnected();
        mRemoteControl.setPlaybackNodeListener(null);
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding = null;
    }

    private void onNavigationItemSelected(final int id) {
        switch (id) {
            case RootNavigationAdapter.ID_NOW_PLAYING:
                showFragmentNowPlaying();
                break;

            case RootNavigationAdapter.ID_PLAYLIST:
                showFragmentPlaylist();
                break;

            case RootNavigationAdapter.ID_SEARCH:
                showFragmentSearch();
                break;

            default:
                break;
        }
    }

    public void goToNowPlaying() {
        if (mBinding != null) {
            mBinding.navigationDrawer.setCurrentItem(0, true);
        }
    }

    private void showFragmentNowPlaying() {
        showFragment(NowPlayingFragment.class.getCanonicalName());
    }

    private void showFragmentPlaylist() {
        showFragment(PlaylistFragment.class.getCanonicalName());
    }

    private void showFragmentSearch() {
        showFragment(SearchFragment.class.getCanonicalName());
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
    protected void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_GOOGLE_API && resultCode == RESULT_OK) {
            mModelViewState.setFixButtonVisible(false);
            mModelViewState.setMessage(null);
            mModelViewState.setMessageDrawableTop(null);
            mGoogleApiClient.connect();
        }
    }

    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks
            = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(@Nullable final Bundle bundle) {
            mModelViewState.setHandheldConnected(false);
            mModelViewState.setFixButtonVisible(false);
            mModelViewState.setMessageDrawableTop(null);
            mRemoteControl.onGoogleApiClientConnected(getApplicationContext(), mGoogleApiClient);
        }

        @Override
        public void onConnectionSuspended(final int i) {
            mRemoteControl.onGoogleApiClientDisconnected();
            mModelViewState.setHandheldConnected(false);
            mModelViewState.setMessageDrawableTop(null);
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener mOnConnectionFailedListener
            = connectionResult -> {
        mModelViewState.setMessageDrawableTop(getDrawable(R.drawable.ic_gms_white_48));
        mModelViewState.setHandheldConnected(false);
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
    };
}
