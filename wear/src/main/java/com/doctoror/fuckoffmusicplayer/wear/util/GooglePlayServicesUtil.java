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
package com.doctoror.fuckoffmusicplayer.wear.util;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.doctoror.fuckoffmusicplayer.R;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Google Play Services utils
 */
public final class GooglePlayServicesUtil {

    private GooglePlayServicesUtil() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static CharSequence toHumanReadableMessage(@NonNull final Resources resources,
            final int statusCode) {
        switch (statusCode) {
            case ConnectionResult.SERVICE_MISSING:
                return resources.getText(R.string.Google_Play_Services_are_missing);

            case ConnectionResult.SERVICE_UPDATING:
                return resources.getText(R.string.Google_Play_Services_are_updating_);

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                return resources.getText(R.string.Google_Play_Services_update_required);

            case ConnectionResult.SERVICE_DISABLED:
                return resources.getText(R.string.Google_Play_Services_are_disabled);

            case ConnectionResult.SERVICE_INVALID:
                return resources.getText(R.string.Google_Play_Services_are_invalid);

            default:
                return GoogleApiAvailability.getInstance().getErrorString(statusCode);
        }
    }
}
