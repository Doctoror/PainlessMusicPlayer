package com.doctoror.fuckoffmusicplayer.library;

import com.doctoror.fuckoffmusicplayer.RuntimePermissions;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Yaroslav Mytkalyk on 11.01.17.
 */
public abstract class LibraryPermissionsFragment extends Fragment {

    private static final String KEY_INSTANCE_STATE = "LibraryPermissionsFragment.INSTANCE_STATE";

    private Subscription mPermissionTimerSubscription;

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
            mPermissionTimerSubscription = Observable.timer(500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(l -> requestPermission());
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

    @Override
    public void onStop() {
        super.onStop();
        if (mPermissionTimerSubscription != null) {
            mPermissionTimerSubscription.unsubscribe();
            mPermissionTimerSubscription = null;
        }
    }

    @Parcel
    static final class InstanceState {

        boolean permissionsRequested;
    }

}
