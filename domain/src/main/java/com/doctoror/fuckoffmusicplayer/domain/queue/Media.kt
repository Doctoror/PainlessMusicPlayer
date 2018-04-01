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
package com.doctoror.fuckoffmusicplayer.domain.queue

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class Media @JvmOverloads constructor(
        val id: Long = 0,
        val data: Uri? = null,
        val title: String? = null,
        val duration: Long = 0,
        val artist: String? = null,
        val album: String? = null,
        val albumId: Long = 0,
        val albumArt: String? = null,
        val track: Int = 0) : Parcelable {

    constructor(source: Parcel) : this(
            source.readLong(),
            source.readParcelable<Uri>(Uri::class.java.classLoader),
            source.readString(),
            source.readLong(),
            source.readString(),
            source.readString(),
            source.readLong(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeLong(id)
        writeParcelable(data, 0)
        writeString(title)
        writeLong(duration)
        writeString(artist)
        writeString(album)
        writeLong(albumId)
        writeString(albumArt)
        writeInt(track)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Media

        if (id != other.id) return false

        return true
    }

    override fun hashCode() = id.hashCode()

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Media> = object : Parcelable.Creator<Media> {
            override fun createFromParcel(source: Parcel) = Media(source)
            override fun newArray(size: Int) = arrayOfNulls<Media?>(size)
        }
    }
}
