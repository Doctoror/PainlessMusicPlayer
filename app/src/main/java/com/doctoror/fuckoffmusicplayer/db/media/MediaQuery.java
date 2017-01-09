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
package com.doctoror.fuckoffmusicplayer.db.media;

import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.net.Uri;
import android.provider.MediaStore;

/**
 * Parameters for querying {@link Media}
 */
final class MediaQuery {

    static final Uri CONTENT_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    static final int COLUMN_ID = 0;
    static final int COLUMN_TRACK = 1;
    static final int COLUMN_TITLE = 2;
    static final int COLUMN_ARTIST = 3;
    static final int COLUMN_ALBUM = 4;
    static final int COLUMN_ALBUM_ID = 5;
    static final int COLUMN_DURATION = 6;
    static final int COLUMN_DATA = 7;
    static final int COLUMN_ALBUM_ART = 8;

    static final String[] PROJECTION = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            "(SELECT _data FROM album_art WHERE album_art.album_id=audio.album_id) AS "
                    + MediaStore.Audio.Albums.ALBUM_ART
    };

    private MediaQuery() {
        throw new UnsupportedOperationException();
    }
}
