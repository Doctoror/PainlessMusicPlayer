package com.doctoror.fuckoffmusicplayer.presentation.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.LinkedList

class CollectionUtilsTest {

    @Test
    fun toArrayListReturnsNullForNullArgument() {
        // When
        val result: List<Any>? = CollectionUtils.toArrayList(null)

        // Then
        assertNull(result)
    }

    @Test
    fun returnsEmptyArrayListForEmptyInput() {
        // When
        val result: List<Any>? = CollectionUtils.toArrayList(LinkedList())

        // Then
        assertTrue(result!!.isEmpty())
    }

    @Test
    fun toArrayListReturnsTheSameInstanceForArrayList() {
        // Given
        val input = arrayListOf("Ass", "Hole")

        // When
        val result = CollectionUtils.toArrayList(input)

        // Then
        assertTrue(input === result)
    }

    @Test
    fun toArrayCreatesNewInstanceForNonArrayListInput() {
        // Given
        val input = LinkedList<String>().apply { add("Arse") }

        // When
        val result = CollectionUtils.toArrayList(input)

        // Then
        assertEquals(input, result)
    }
}
