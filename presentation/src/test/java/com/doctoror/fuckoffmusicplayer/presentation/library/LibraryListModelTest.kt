package com.doctoror.fuckoffmusicplayer.presentation.library

import org.junit.Assert.assertEquals
import org.junit.Test

class LibraryListModelTest {

    private val underTest = LibraryListModel()

    @Test
    fun showsViewProgress() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewProgress()

        // Then
        assertEquals(LibraryListModel.ANIMATOR_CHILD_PROGRESS, underTest.displayedChild.get())
    }

    @Test
    fun showsViewPermissionDenied() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewPermissionDenied()

        // Then
        assertEquals(
                LibraryListModel.ANIMATOR_CHILD_PERMISSION_DENIED,
                underTest.displayedChild.get())
    }

    @Test
    fun showsViewEmpty() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewEmpty()

        // Then
        assertEquals(LibraryListModel.ANIMATOR_CHILD_EMPTY, underTest.displayedChild.get())
    }

    @Test
    fun showsViewError() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewError()

        // Then
        assertEquals(LibraryListModel.ANIMATOR_CHILD_ERROR, underTest.displayedChild.get())
    }

    @Test
    fun showsViewContent() {
        // Given
        underTest.displayedChild.set(-1)

        // When
        underTest.showViewContent()

        // Then
        assertEquals(LibraryListModel.ANIMATOR_CHILD_CONTENT, underTest.displayedChild.get())
    }
}
