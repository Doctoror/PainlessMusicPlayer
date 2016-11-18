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
package com.doctoror.fuckoffmusicplayer.filemanager;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import org.parceler.Parcels;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

/**
 * Created by Yaroslav Mytkalyk on 30.10.16.
 */

public final class DeleteFileDialogFragment extends DialogFragment {

    private static final String EXTRA_MEDIA = "EXTRA_MEDIA";

    public interface Callback {

        void onDeleteClick(@NonNull Media media);

        void onDeleteCancel();

        void onDeleteDialogDismiss();
    }

    public static void show(@NonNull final Media media,
            @NonNull final FragmentManager fragmentManager,
            @NonNull final String tag) {
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_MEDIA, Parcels.wrap(media));

        final DeleteFileDialogFragment f = new DeleteFileDialogFragment();
        f.setArguments(args);
        f.show(fragmentManager, tag);
    }

    private Media media;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Arguments must contain EXTRA_MEDIA");
        }
        media = Parcels.unwrap(args.getParcelable(EXTRA_MEDIA));
        if (media == null) {
            throw new IllegalArgumentException("Arguments must contain EXTRA_MEDIA");
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.Are_you_sure_you_want_to_delete_s, media.getTitle()))
                .setPositiveButton(R.string.Delete, (d, w) -> onDeleteClick())
                .setNegativeButton(R.string.Cancel, (d, w) -> onDeleteCancel())
                .create();
    }

    private void onDeleteCancel() {
        final Activity activity = getActivity();
        if (activity instanceof Callback) {
            ((Callback) activity).onDeleteCancel();
        }
    }

    private void onDeleteClick() {
        final Activity activity = getActivity();
        if (activity instanceof Callback) {
            ((Callback) activity).onDeleteClick(media);
        }
    }

    @Override
    public void onCancel(final DialogInterface dialog) {
        super.onCancel(dialog);
        onDeleteCancel();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof Callback) {
            ((Callback) activity).onDeleteDialogDismiss();
        }
    }
}
