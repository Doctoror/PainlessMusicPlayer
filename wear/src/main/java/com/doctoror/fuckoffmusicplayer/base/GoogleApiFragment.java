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
