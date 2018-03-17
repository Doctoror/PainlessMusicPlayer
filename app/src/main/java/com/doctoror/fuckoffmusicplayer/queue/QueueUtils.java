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
package com.doctoror.fuckoffmusicplayer.queue;

import com.doctoror.fuckoffmusicplayer.data.media.MediaManagerMediaStore;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.content.ContentResolver;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Queue utils
 */
public final class QueueUtils {

    private static final String TAG = "QueueUtils";

    private QueueUtils() {
        throw new UnsupportedOperationException();
    }

    public static void removeAlbumFromCurrentQueue(
            @NonNull final ContentResolver contentResolver,
            @NonNull final PlaybackData playbackData,
            final long albumId) {
        final long[] ids;
        try {
            ids = MediaManagerMediaStore.getAlbumMediaIds(contentResolver, albumId);
        } catch (IOException e) {
            Log.w(TAG, "Will not remove album from current queue", e);
            return;
        }
        removeMediasFromCurrentQueue(playbackData, ids);
    }

    public static void removeMediasFromCurrentQueue(
            @NonNull final PlaybackData playbackData,
            @NonNull final long... mediaIds) {
        final List<Media> queue = playbackData.getQueue();
        boolean modified = false;
        if (queue != null) {
            for (final long id : mediaIds) {
                modified |= removeFromQueue(queue, id);
            }
        }
        if (modified) {
            playbackData.setPlayQueue(queue);
        }
    }

    private static boolean removeFromQueue(@NonNull final List<Media> queue,
            final long id) {
        final Iterator<Media> i = queue.iterator();
        while (i.hasNext()) {
            if (i.next().getId() == id) {
                i.remove();
                return true;
            }
        }
        return false;
    }
}
