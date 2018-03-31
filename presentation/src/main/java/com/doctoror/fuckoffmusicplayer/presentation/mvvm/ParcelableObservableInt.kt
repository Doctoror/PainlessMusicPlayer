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
package com.doctoror.fuckoffmusicplayer.presentation.mvvm

import android.databinding.ObservableInt
import android.os.Parcel
import android.os.Parcelable

class ParcelableObservableInt(value: Int = 0) : ObservableInt(value), Parcelable {

    constructor(source: Parcel) : this(source.readInt())

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(get())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableObservableInt

        if (get() != other.get()) return false

        return true
    }

    override fun hashCode() = get()

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableObservableInt> = object
            : Parcelable.Creator<ParcelableObservableInt> {
            override fun createFromParcel(source: Parcel) = ParcelableObservableInt(source)
            override fun newArray(size: Int) = arrayOfNulls<ParcelableObservableInt?>(size)
        }
    }
}
