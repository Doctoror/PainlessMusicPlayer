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

import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.FileUtils;
import com.doctoror.fuckoffmusicplayer.util.Log;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcels;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

/**
 * Created by Yaroslav Mytkalyk on 30.10.16.
 */

public final class FileManagerService extends IntentService {

    private static final String TAG = "FileManagerService";

    private static final String ACTION_MEDIA_DELETE = "ACTION_MEDIA_DELETE";
    private static final String EXTRA_MEDIA = "EXTRA_MEDIA";

    public static void delete(@NonNull final Context context,
            @NonNull final Media media) {
        final Intent intent = new Intent(context, FileManagerService.class);
        intent.setAction(ACTION_MEDIA_DELETE);
        intent.putExtra(EXTRA_MEDIA, Parcels.wrap(media));
        context.startService(intent);
    }

    public FileManagerService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        switch (intent.getAction()) {
            case ACTION_MEDIA_DELETE:
                onActionMediaDelete(intent);
                break;
        }
    }

    private void onActionMediaDelete(@NonNull final Intent intent) {
        if (!RxPermissions.getInstance(this).isGranted(
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission not granted");
            return;
        }

        final Media media = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MEDIA));
        if (media == null) {
            throw new IllegalArgumentException("EXTRA_MEDIA must not be null");
        }
        try {
            FileUtils.delete(getContentResolver(), media);
        } catch (Exception e) {
            Log.w(TAG, e);
        }
    }
}
