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
package com.doctoror.fuckoffmusicplayer.presentation.library;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.doctoror.fuckoffmusicplayer.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

/**
 * Asking delete confirmation and runtime permissions. Starts deletion process if confirmed.
 * Subclasses must implement {@link #performDelete()} to handle deletion.
 */
public abstract class DeleteItemDialogFragment extends DialogFragment {

    private static final String EXTRA_ID = "EXTRA_ID";
    private static final String EXTRA_NAME = "EXTRA_NAME";

    public static void show(@NonNull final Context context,
            @NonNull final Class<? extends DeleteItemDialogFragment> tClass,
            @NonNull final FragmentManager fragmentManager,
            @NonNull final String tag,
            final long targetId,
            @Nullable final String targetName) {
        final DialogFragment f = (DialogFragment) Fragment.instantiate(context,
                tClass.getCanonicalName(), createArguments(targetId, targetName));
        f.show(fragmentManager, tag);
    }

    @NonNull
    private static Bundle createArguments(final long targetId,
            @Nullable final String targetName) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ID, targetId);
        args.putString(EXTRA_NAME, targetName);
        return args;
    }

    private long mId;
    private String mName;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Arguments must contain EXTRA_ID");
        }
        mId = args.getLong(EXTRA_ID);
        if (mId == 0) {
            throw new IllegalArgumentException("Arguments must contain non-zero EXTRA_ID");
        }
        mName = args.getString(EXTRA_NAME);
    }

    @Override
    public Dialog onCreateDialog(@NonNull final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.Are_you_sure_you_want_to_permanently_delete_s,
                        mName))
                .setPositiveButton(R.string.Delete, (d, w) -> onDeleteClick())
                .setNegativeButton(R.string.Cancel, null)
                .create();
    }

    private void onDeleteClick() {
        final Activity activity = getActivity();
        new RxPermissions(activity)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        performDelete();
                    }
                });
    }

    protected final long getTargetId() {
        return mId;
    }

    protected abstract void performDelete();
}
