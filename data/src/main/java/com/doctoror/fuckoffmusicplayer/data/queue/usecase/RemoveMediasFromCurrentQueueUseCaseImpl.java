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

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveMediasFromCurrentQueueUseCase;

import android.support.annotation.NonNull;

import java.util.Iterator;
import java.util.List;

public final class RemoveMediasFromCurrentQueueUseCaseImpl implements
        RemoveMediasFromCurrentQueueUseCase {

    private final PlaybackData playbackData;

    public RemoveMediasFromCurrentQueueUseCaseImpl(@NonNull final PlaybackData playbackData) {
        this.playbackData = playbackData;
    }

    @Override
    public void removeMediasFromCurrentQueue(@NonNull final long... mediaIds) {
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

    private static boolean removeFromQueue(@NonNull final List<Media> queue, final long id) {
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
