package com.doctoror.fuckoffmusicplayer.parcelable.converter

import android.os.Parcel
import org.parceler.ParcelConverter

internal fun <T> valueFromParcelConverter(
        converter: ParcelConverter<T>,
        parcel: Parcel): T? {
    parcel.setDataPosition(0)
    try {
        return converter.fromParcel(parcel)
    } finally {
        parcel.recycle()
    }
}
