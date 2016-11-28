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

import com.doctoror.commons.util.FileUtils;
import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.commons.wear.nano.WearPlaybackData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Helper for persisting album art, media and playback state
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
            @NonNull final WearPlaybackData.PlaybackState ps) {
        ProtoUtils.writeToFile(context, FILE_NAME_PLAYBACK_STATE, ps);
    }

    static void deleteMedia(@NonNull final Context context) {
        context.deleteFile(FILE_NAME_MEDIA);
    }

    static void deletePlaybackState(@NonNull final Context context) {
        context.deleteFile(FILE_NAME_PLAYBACK_STATE);
    }

    static void persistMedia(@NonNull final Context context,
            @NonNull final WearPlaybackData.Media ps) {
        ProtoUtils.writeToFile(context, FILE_NAME_MEDIA, ps);
    }

    @Nullable
    static WearPlaybackData.PlaybackState readPlaybackState(@NonNull final Context context) {
        return ProtoUtils.readFromFile(context, FILE_NAME_PLAYBACK_STATE,
                new WearPlaybackData.PlaybackState());
    }

    @Nullable
    static WearPlaybackData.Media readMedia(@NonNull final Context context) {
        return ProtoUtils.readFromFile(context, FILE_NAME_MEDIA, new WearPlaybackData.Media());
    }

    static void persistAlbumArt(@NonNull final Context context,
            @Nullable final byte[] albumArt) {
        FileUtils.writeOrDeletePrivateFile(context, FILE_NAME_MEDIA_ART, albumArt);
    }

    @Nullable
    static Bitmap readAlbumArt(@NonNull final Context context) {
        return BitmapFactory.decodeFile(context.getFileStreamPath(FILE_NAME_MEDIA_ART)
                .getAbsolutePath());
    }
}
