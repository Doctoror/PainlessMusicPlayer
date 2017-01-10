package com.doctoror.fuckoffmusicplayer.library.albums;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.filemanager.FileManagerService;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

/**
 * Created by Yaroslav Mytkalyk on 09.01.17.
 */

public final class DeleteAlbumDialogFragment extends DialogFragment {

    private static final String EXTRA_ALBUM_ID = "EXTRA_ALBUM_ID";
    private static final String EXTRA_ALBUM_NAME = "EXTRA_ALBUM_NAME";

    public static void show(
            @NonNull final FragmentManager fragmentManager,
            @NonNull final String tag,
            final long albumId,
            @NonNull final String albumName) {
        final Bundle args = new Bundle();
        args.putLong(EXTRA_ALBUM_ID, albumId);
        args.putString(EXTRA_ALBUM_NAME, albumName);

        final DeleteAlbumDialogFragment f = new DeleteAlbumDialogFragment();
        f.setArguments(args);
        f.show(fragmentManager, tag);
    }

    private long mAlbumId;
    private String mAlbumName;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if (args == null) {
            throw new IllegalArgumentException("Arguments must contain EXTRA_ALBUM_ID");
        }
        mAlbumId = args.getLong(EXTRA_ALBUM_ID);
        if (mAlbumId == 0) {
            throw new IllegalArgumentException("Arguments must contain non-zero EXTRA_ALBUM_ID");
        }
        mAlbumName = args.getString(EXTRA_ALBUM_NAME);
        if (mAlbumName == null) {
            throw new IllegalArgumentException("Arguments must contain EXTRA_ALBUM_NAME");
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.Are_you_sure_you_want_to_permanently_delete_s,
                        mAlbumName))
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
                        FileManagerService.deleteAlbum(activity, mAlbumId);
                    }
                });
    }
}
