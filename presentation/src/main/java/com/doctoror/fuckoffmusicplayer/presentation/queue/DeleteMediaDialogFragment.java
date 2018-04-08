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
package com.doctoror.fuckoffmusicplayer.presentation.queue;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;

import com.doctoror.fuckoffmusicplayer.presentation.library.DeleteItemDialogFragment;
import com.doctoror.fuckoffmusicplayer.presentation.media.MediaManagerService;

public final class DeleteMediaDialogFragment extends DeleteItemDialogFragment {

    private static final String TAG = "DeleteMediaDialogFragment";

    public interface Callback {
        void onPerformDelete(long id);
    }

    public static void show(
            @NonNull final Context context,
            @NonNull final FragmentManager fragmentManager,
            final long albumId,
            @Nullable final String albumName) {
        DeleteItemDialogFragment.show(context,
                DeleteMediaDialogFragment.class,
                fragmentManager,
                TAG,
                albumId,
                albumName != null ? albumName : "");
    }

    @Override
    protected void performDelete() {
        final Activity activity = getActivity();
        if (activity != null) {
            if (activity instanceof Callback) {
                ((Callback) activity).onPerformDelete(getTargetId());
            }
            MediaManagerService.deleteMedia(activity, getTargetId());
        }
    }
}
