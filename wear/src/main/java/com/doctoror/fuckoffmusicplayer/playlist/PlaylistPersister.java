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
package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.commons.wear.nano.ProtoPlaybackData;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 15.11.16.
 */

final class PlaylistPersister {

    private static final String FILE_NAME_PLAYLIST = "playlist";

    private PlaylistPersister() {
        throw new UnsupportedOperationException();
    }

    static void persist(@NonNull final Context context,
            @Nullable final ProtoPlaybackData.Playlist playlist) {
        if (playlist != null) {
            ProtoUtils.writeToFile(context, FILE_NAME_PLAYLIST, playlist);
        } else {
            context.deleteFile(FILE_NAME_PLAYLIST);
        }
    }

    @Nullable
    static ProtoPlaybackData.Playlist read(@NonNull final Context context) {
        return ProtoUtils.readFromFile(context, FILE_NAME_PLAYLIST,
                new ProtoPlaybackData.Playlist());
    }
}
