package com.doctoror.fuckoffmusicplayer.presentation.library

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryListViewModelTest {

    private val underTest = LibraryListViewModel()

    @Test
    fun showsViewProgress() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewProgress()

        // Then
        assertEquals(underTest.animatorChildProgress, underTest.displayedChild.get())
    }

    @Test
    fun showsViewPermissionDenied() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewPermissionDenied()

        // Then
        assertEquals(
                underTest.animatorChildPermissionDenied,
                underTest.displayedChild.get())
    }

    @Test
    fun showsViewEmpty() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewEmpty()

        // Then
        assertEquals(underTest.animatorChildEmpty, underTest.displayedChild.get())
    }

    @Test
    fun showsViewError() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewError()

        // Then
        assertEquals(underTest.animatorChildError, underTest.displayedChild.get())
    }

    @Test
    fun showsViewContent() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewContent()

        // Then
        assertEquals(underTest.animatorChildContent, underTest.displayedChild.get())
    }
}
