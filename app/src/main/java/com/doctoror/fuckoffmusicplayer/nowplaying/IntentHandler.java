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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;
import com.doctoror.commons.util.Log;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 07.11.16.
 */

final class IntentHandler {

    private static final String TAG = "IntentHandler";

    private IntentHandler() {

    }

    @NonNull
    static List<Media> playlistFromActionView(@NonNull final ContentResolver contentResolver,
            @NonNull final Intent intent) throws IOException {
        final Uri data = intent.getData();
        if (data == null) {
            Log.w(TAG, "Intent data is null");
            throw new IOException("Intent data is null");
        }

        final String scheme = data.getScheme();
        if (scheme == null) {
            Log.w(TAG, "Uri scheme is null");
            throw new IOException("Uri scheme is null");
        }
        switch (scheme) {
            case "file":
                return playFile(contentResolver, data);

            default:
                Log.w(TAG, "Unhandled Uri scheme: " + scheme);
                throw new IOException("Unhandled Uri scheme: " + scheme);
        }
    }

    @NonNull
    private static List<Media> playFile(@NonNull final ContentResolver contentResolver,
            @NonNull final Uri data) throws IOException {
        try {
            return PlaylistUtils.forFile(contentResolver, data);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
