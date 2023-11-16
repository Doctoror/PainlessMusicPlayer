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

import android.os.Parcel
import android.os.Parcelable
import androidx.databinding.ObservableField

class ParcelableObservableField<T : Parcelable>(value: T? = null)
    : ObservableField<T>(value), Parcelable {

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(get(), 0)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParcelableObservableField<*>

        if (get() != other.get()) return false

        return true
    }

    override fun hashCode() = get()?.hashCode() ?: 0

    companion object {

        private fun <T : Parcelable> fromParcel(source: Parcel) = ParcelableObservableField<T>(
                source.readParcelable(ParcelableObservableField::class.java.classLoader))

        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableObservableField<*>> =
                object : Parcelable.Creator<ParcelableObservableField<*>> {

                    override fun createFromParcel(source: Parcel) =
                            fromParcel<Parcelable>(source)

                    override fun newArray(size: Int) =
                            arrayOfNulls<ParcelableObservableField<Parcelable>>(size)
                }
    }
}
