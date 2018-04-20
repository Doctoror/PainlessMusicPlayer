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
package com.doctoror.fuckoffmusicplayer.presentation.recentactivity

import android.database.Cursor
import com.doctoror.fuckoffmusicplayer.domain.albums.COLUMN_ALBUM
import com.doctoror.fuckoffmusicplayer.domain.albums.COLUMN_ALBUM_ART
import com.doctoror.fuckoffmusicplayer.domain.albums.COLUMN_ID

import java.util.ArrayList

class AlbumItemsFactory {

    fun itemsFromCursor(c: Cursor): List<AlbumItem> {
        val items = ArrayList<AlbumItem>(c.count)
        if (c.moveToFirst()) {
            while (!c.isAfterLast) {
                items.add(itemFromCursor(c))
                c.moveToNext()
            }
        }
        return items
    }

    private fun itemFromCursor(c: Cursor): AlbumItem {
        val item = AlbumItem()
        item.id = c.getLong(COLUMN_ID)
        item.title = c.getString(COLUMN_ALBUM)
        item.albumArt = c.getString(COLUMN_ALBUM_ART)
        return item
    }
}
