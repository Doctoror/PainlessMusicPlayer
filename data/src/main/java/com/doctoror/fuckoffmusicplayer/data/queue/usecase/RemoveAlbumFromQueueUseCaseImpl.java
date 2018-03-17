/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.data.queue.usecase;

import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveAlbumFromQueueUseCase;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public final class RemoveAlbumFromQueueUseCaseImpl implements RemoveAlbumFromQueueUseCase {

    private static final String TAG = "RemoveAlbumFromQueueUseCaseImpl";

    private final AlbumMediaIdsProvider albumMediaIdsProvider;
    private final PlaybackData playbackData;

    public RemoveAlbumFromQueueUseCaseImpl(
            @NonNull final AlbumMediaIdsProvider albumMediaIdsProvider,
            @NonNull final PlaybackData playbackData) {
        this.albumMediaIdsProvider = albumMediaIdsProvider;
        this.playbackData = playbackData;
    }

    @Override
    public void removeAlbumFromCurrentQueue(final long albumId) {
        final long[] ids;
        try {
            ids = albumMediaIdsProvider.getAlbumMediaIds(albumId);
        } catch (IOException e) {
            Log.w(TAG, "Will not remove album from current queue", e);
            return;
        }
        removeMediasFromCurrentQueue(playbackData, ids);
    }

    private static void removeMediasFromCurrentQueue(
            @NonNull final PlaybackData playbackData,
            @NonNull final long... mediaIds) {
        final List<Media> queue = playbackData.getQueue();
        boolean modified = false;
        if (queue != null) {
            for (final long id : mediaIds) {
                modified |= removeMediaFromQueue(queue, id);
            }
        }
        if (modified) {
            playbackData.setPlayQueue(queue);
        }
    }

    private static boolean removeMediaFromQueue(
            @NonNull final List<Media> queue,
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
