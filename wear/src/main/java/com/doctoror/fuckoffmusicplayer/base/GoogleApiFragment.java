package com.doctoror.fuckoffmusicplayer.base;

import com.google.android.gms.common.api.GoogleApiClient;

import android.support.annotation.NonNull;

/**
 * Fragment that can receive a {@link GoogleApiClient}.
 * Designed to receive the client from a hosting Actiivity.
 */

public abstract class GoogleApiFragment extends LifecycleNotifierFragment {

    public abstract void onGoogleApiClientConnected(@NonNull GoogleApiClient client);

    public abstract void onGoogleApiClientDisconnected();

    @Override
    public void onDetach() {
        super.onDetach();
        onGoogleApiClientDisconnected();
    }
}
