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
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveAlbumFromQueueUseCase;
import com.doctoror.fuckoffmusicplayer.domain.queue.usecase.RemoveMediasFromCurrentQueueUseCase;

import android.support.annotation.NonNull;

import java.io.IOException;

public final class RemoveAlbumFromQueueUseCaseImpl implements RemoveAlbumFromQueueUseCase {

    private static final String TAG = "RemoveAlbumFromQueueUseCaseImpl";

    private final AlbumMediaIdsProvider albumMediaIdsProvider;
    private final RemoveMediasFromCurrentQueueUseCase removeMediasFromCurrentQueueUseCase;

    public RemoveAlbumFromQueueUseCaseImpl(
            @NonNull final AlbumMediaIdsProvider albumMediaIdsProvider,
            @NonNull final RemoveMediasFromCurrentQueueUseCase removeMediasFromCurrentQueueUseCase) {
        this.albumMediaIdsProvider = albumMediaIdsProvider;
        this.removeMediasFromCurrentQueueUseCase = removeMediasFromCurrentQueueUseCase;
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

        removeMediasFromCurrentQueueUseCase.removeMediasFromCurrentQueue(ids);
    }
}
