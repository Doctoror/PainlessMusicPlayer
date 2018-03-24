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
package com.doctoror.fuckoffmusicplayer.settings;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.doctoror.fuckoffmusicplayer.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

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
