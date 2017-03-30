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
package com.doctoror.fuckoffmusicplayer.library;

import com.doctoror.fuckoffmusicplayer.RuntimePermissions;
import com.doctoror.fuckoffmusicplayer.base.BaseFragment;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * {@link BaseFragment} that asks library permissions
 */
public abstract class LibraryPermissionsFragment extends BaseFragment {

    private static final String KEY_INSTANCE_STATE = "LibraryPermissionsFragment.INSTANCE_STATE";

    private boolean mHasPermissions;
    private boolean mPermissionRequested = RuntimePermissions.arePermissionsRequested();

    private RxPermissions mRxPermissions;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void restoreInstanceState(@NonNull final Bundle savedInstanceState) {
        final InstanceState state = Parcels.unwrap(savedInstanceState
                .getParcelable(KEY_INSTANCE_STATE));
        if (state != null) {
            mPermissionRequested = state.permissionsRequested
                    || RuntimePermissions.arePermissionsRequested();
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final InstanceState state = new InstanceState();
        state.permissionsRequested = mPermissionRequested;
        outState.putParcelable(KEY_INSTANCE_STATE, Parcels.wrap(state));
    }

    @NonNull
    private RxPermissions getRxPermissions() {
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(getActivity());
        }
        return mRxPermissions;
    }

    private void requestPermissionIfNeeded() {
        mHasPermissions = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (mHasPermissions) {
            onPermissionGranted();
        } else if (mPermissionRequested) {
            onPermissionDenied();
        } else {
            registerOnStartSubscription(Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(l -> requestPermission()));
        }
    }

    protected final void requestPermission() {
        mPermissionRequested = true;
        RuntimePermissions.setPermissionsRequested(true);
        getRxPermissions().request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    mHasPermissions = granted;
                    if (granted) {
                        onPermissionGranted();
                    } else {
                        onPermissionDenied();
                    }
                });
    }


    protected abstract void onPermissionGranted();

    protected abstract void onPermissionDenied();

    protected final boolean hasPermissions() {
        return mHasPermissions;
    }

    @Override
    public void onStart() {
        super.onStart();
        requestPermissionIfNeeded();
    }

    @Parcel
    static final class InstanceState {

        boolean permissionsRequested;
    }

}
