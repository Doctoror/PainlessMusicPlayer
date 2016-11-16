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
package com.doctoror.fuckoffmusicplayer.media;

import com.doctoror.commons.util.ProtoPersister;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

final class MediaPersister {

    private static final String TAG = "MediaPersister";

    private static final String FILE_NAME_MEDIA_ART = "media_art";
    private static final String FILE_NAME_MEDIA = "media";
    private static final String FILE_NAME_PLAYBACK_STATE = "playback_state";

    private MediaPersister() {
        throw new UnsupportedOperationException();
    }

    static void persistPlaybackState(@NonNull final Context context,
            @NonNull final ProtoPlaybackData.PlaybackState ps) {
        ProtoPersister.writeToFile(context, FILE_NAME_PLAYBACK_STATE, ps);
    }

    static void deleteMedia(@NonNull final Context context) {
        context.deleteFile(FILE_NAME_MEDIA);
    }

    static void deletePlaybackState(@NonNull final Context context) {
        context.deleteFile(FILE_NAME_PLAYBACK_STATE);
    }

    static void persistMedia(@NonNull final Context context,
            @NonNull final ProtoPlaybackData.Media ps) {
        ProtoPersister.writeToFile(context, FILE_NAME_MEDIA, ps);
    }

    @Nullable
    static ProtoPlaybackData.PlaybackState readPlaybackState(@NonNull final Context context) {
        return ProtoPersister.readFromFile(context, new ProtoPlaybackData.PlaybackState(),
                FILE_NAME_PLAYBACK_STATE);
    }

    @Nullable
    static ProtoPlaybackData.Media readMedia(@NonNull final Context context) {
        return ProtoPersister.readFromFile(context, new ProtoPlaybackData.Media(),
                FILE_NAME_MEDIA);
    }

    static void persistAlbumArt(@NonNull final Context context,
            @Nullable final byte[] albumArt) {
        ProtoPersister.writeOrDeletePrivateFile(context, FILE_NAME_MEDIA_ART, albumArt);
    }

    @Nullable
    static Bitmap readAlbumArt(@NonNull final Context context) {
        return BitmapFactory.decodeFile(context.getFileStreamPath(FILE_NAME_MEDIA_ART)
                .getAbsolutePath());
    }
}
