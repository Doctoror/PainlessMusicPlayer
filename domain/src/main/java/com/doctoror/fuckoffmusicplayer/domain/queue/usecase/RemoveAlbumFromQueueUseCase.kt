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
package com.doctoror.fuckoffmusicplayer.domain.queue.usecase

import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumMediaIdsProvider
import java.io.IOException

class RemoveAlbumFromQueueUseCase(
        private val albumMediaIdsProvider: AlbumMediaIdsProvider,
        private val removeMediasFromCurrentQueueUseCase: RemoveMediasFromCurrentQueueUseCase) {

    fun removeAlbumFromCurrentQueue(albumId: Long) {
        val ids: LongArray
        try {
            ids = albumMediaIdsProvider.getAlbumMediaIds(albumId)
        } catch (e: IOException) {
            Log.w("RemoveAlbumFromQueue", "Will not remove album from current queue", e)
            return
        }

        removeMediasFromCurrentQueueUseCase.removeMediasFromCurrentQueue(*ids)
    }
}
