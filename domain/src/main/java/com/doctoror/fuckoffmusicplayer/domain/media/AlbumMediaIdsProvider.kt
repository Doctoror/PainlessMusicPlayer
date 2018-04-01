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
package com.doctoror.fuckoffmusicplayer.domain.media

import java.io.IOException

interface AlbumMediaIdsProvider {

    /**
     * Retrieves list if media ids that belong to the specified album
     *
     * @param ambumId the id of the album to get media ids for
     * @return ist if media ids that belong to the specified album
     */
    @Throws(IOException::class)
    fun getAlbumMediaIds(ambumId: Long): LongArray
}
