package com.doctoror.fuckoffmusicplayer.util;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.doctoror.fuckoffmusicplayer.R;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
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
