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
package com.doctoror.fuckoffmusicplayer.parcelable.converter

import android.databinding.ObservableField
import android.os.Parcel
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationItem
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Test

class NavigationItemObservableFieldConverterTest {

    private val underTest = NavigationItemObservableFieldConverter()

    @Test
    fun convertsNullValue() {
        // Given
        val parcel = Parcel.obtain()

        // When
        underTest.toParcel(null, parcel)

        // Then
        assertNull(valueFromParcel(underTest, parcel))
    }

    @Test
    fun convertsFieldWithNullValue() {
        // Given
        val parcel = Parcel.obtain()
        val value = ObservableField<NavigationItem>()

        // When
        underTest.toParcel(value, parcel)

        // Then
        assertEquals(value, valueFromParcel(underTest, parcel))
    }

    @Test
    fun convertsFieldWithValue() {
        // Given
        val parcel = Parcel.obtain()
        val value = ObservableField(NavigationItem.SETTINGS)

        // When
        underTest.toParcel(value, parcel)

        // Then
        assertEquals(value, valueFromParcel(underTest, parcel))
    }

    private fun valueFromParcel(
            converter: NavigationItemObservableFieldConverter,
            parcel: Parcel): ObservableField<NavigationItem>? {
        parcel.setDataPosition(0)
        try {
            return converter.fromParcel(parcel)
        } finally {
            parcel.recycle()
        }
    }

    private fun assertEquals(
            expected: ObservableField<NavigationItem>?,
            actual: ObservableField<NavigationItem>?) {
        if (expected == null || actual == null) {
            assertEquals(expected as Any?, actual as Any?)
        } else {
            val expectedValue = expected.get()
            val actualValue = actual.get()
            assertEquals(expectedValue as Any?, actualValue as Any?)
        }
    }
}
