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
package com.doctoror.fuckoffmusicplayer.presentation.library.albums;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.doctoror.fuckoffmusicplayer.presentation.library.DeleteItemDialogFragment;
import com.doctoror.fuckoffmusicplayer.presentation.media.MediaManagerService;

public final class DeleteAlbumDialogFragment extends DeleteItemDialogFragment {

    private static final String TAG = "DeleteAlbumDialogFragment";

    public static void show(
            @NonNull final Context context,
            @NonNull final FragmentManager fragmentManager,
            final long albumId,
            @Nullable final String albumName) {
        DeleteItemDialogFragment.show(context,
                DeleteAlbumDialogFragment.class,
                fragmentManager,
                TAG,
                albumId,
                albumName);
    }

    @Override
    protected void performDelete() {
        final Activity activity = getActivity();
        if (activity != null) {
            MediaManagerService.deleteAlbum(activity, getTargetId());
        }
    }
}
