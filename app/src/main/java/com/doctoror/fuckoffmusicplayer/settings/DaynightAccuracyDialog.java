package com.doctoror.fuckoffmusicplayer.settings;

import com.doctoror.fuckoffmusicplayer.R;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * Offers to switch ACCESS_COARSE_LOCATION permission for better daynight theme accuracy
 */
public final class DaynightAccuracyDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(R.string.Improve_automatic_switching_between_Day_and_Night_)
                .setPositiveButton(R.string.Allow, (d, w) -> onAllowClick())
                .setNegativeButton(R.string.Later, null)
                .create();
    }

    private void onAllowClick() {
        new RxPermissions(getActivity())
                .request(Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe();
    }
}
