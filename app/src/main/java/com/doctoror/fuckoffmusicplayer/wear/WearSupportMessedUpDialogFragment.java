package com.doctoror.fuckoffmusicplayer.wear;

import com.google.android.gms.common.ConnectionResult;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.f2prateek.dart.Dart;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.IntentSender;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by Yaroslav Mytkalyk on 25.11.16.
 */
public final class WearSupportMessedUpDialogFragment extends DialogFragment {

    private static final String TAG = "WearSupportMessedUpDialogFragment";

    private static final String EXTRA_CONNECTION_RESULT = "EXTRA_CONNECTION_RESULT";
    private static final String EXTRA_RESOLUTION_REQUEST_CODE = "EXTRA_RESOLUTION_REQUEST_CODE";

    public static void show(@NonNull final Context context,
            @NonNull final FragmentManager fragmentManager,
            @NonNull final String tag,
            @NonNull final ConnectionResult connectionResult,
            final int resolutionRequestCode) {
        if (connectionResult.getErrorCode() == ConnectionResult.SUCCESS) {
            throw new IllegalArgumentException("Will not show for successful connection");
        }

        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_CONNECTION_RESULT, connectionResult);
        args.putInt(EXTRA_RESOLUTION_REQUEST_CODE, resolutionRequestCode);

        final DialogFragment instance = (DialogFragment) Fragment.instantiate(context,
                WearSupportMessedUpDialogFragment.class.getCanonicalName());
        instance.setArguments(args);
        instance.show(fragmentManager, tag);
    }

    private ConnectionResult connectionResult;
    private int resolutionRequestCode;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        connectionResult = args.getParcelable(EXTRA_CONNECTION_RESULT);
        resolutionRequestCode = args.getInt(EXTRA_RESOLUTION_REQUEST_CODE);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Dart.inject(this);
        final Resources res = getResources();
        final CharSequence reason;
        switch (connectionResult.getErrorCode()) {
            case ConnectionResult.SERVICE_MISSING:
                reason = res.getText(R.string.Google_Play_Services_are_missing);
                break;

            case ConnectionResult.SERVICE_DISABLED:
                reason = res.getText(R.string.Google_Play_Services_are_disabled);
                break;

            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                reason = res.getText(R.string.Google_Play_Services_are_outdated);
                break;

            case ConnectionResult.SERVICE_INVALID:
            default:
                reason = res.getText(R.string.Google_Play_Services_are_invalid);
                break;
        }

        final CharSequence message = res.getString(
                R.string.This_app_wont_support_Android_Wear_because_s, reason);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(message);
        if (connectionResult.hasResolution()) {
            builder.setPositiveButton(R.string.Fix, (di, w) -> onFixClick());
            builder.setNegativeButton(R.string.Ignore, null);
        } else {
            builder.setNegativeButton(R.string.Dismiss, null);
        }

        return builder.create();
    }

    private void onFixClick() {
        try {
            connectionResult.startResolutionForResult(getActivity(), resolutionRequestCode);
        } catch (IntentSender.SendIntentException e) {
            Log.w(TAG, e);
            Toast.makeText(getActivity(), R.string.Failed_to_start_resolution, Toast.LENGTH_SHORT)
                    .show();
        }
    }
}
