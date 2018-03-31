package com.doctoror.fuckoffmusicplayer.parcelable.converter

import android.databinding.ObservableInt
import android.os.Parcel
import junit.framework.Assert.assertEquals
import org.junit.Test

class ObservableIntConverterTest {

    private val underTest = ObservableIntConverter()

    @Test
    fun convertsNullValue() {
        // Given
        val parcel = Parcel.obtain()

        // When
        underTest.toParcel(null, parcel)

        // Then
        assertEquals(ObservableInt(), valueFromParcelConverter(underTest, parcel))
    }

    @Test
    fun convertsFieldWithValue() {
        // Given
        val parcel = Parcel.obtain()
        val value = ObservableInt(2)

        // When
        underTest.toParcel(value, parcel)

        // Then
        assertEquals(value, valueFromParcelConverter(underTest, parcel))
    }

    private fun assertEquals(
            expected: ObservableInt?,
            actual: ObservableInt?) {
        if (expected == null || actual == null) {
            assertEquals(expected as Any?, actual as Any?)
        } else {
            val expectedValue = expected.get()
            val actualValue = actual.get()
            assertEquals(expectedValue, actualValue)
        }
    }
}
