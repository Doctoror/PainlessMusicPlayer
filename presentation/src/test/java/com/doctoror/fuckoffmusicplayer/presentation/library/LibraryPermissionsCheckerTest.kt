package com.doctoror.fuckoffmusicplayer.presentation.library

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.S])
@RunWith(RobolectricTestRunner::class)
class LibraryPermissionsCheckerTest {

    private val context: Context = mock()
    private val underTest = LibraryPermissionsChecker(context)

    @Test
    fun detectsPermissionDenied() {
        // Given
        whenever(
            context.checkPermission(
                eq(Manifest.permission.READ_EXTERNAL_STORAGE),
                any(),
                any()
            )
        ).thenReturn(PackageManager.PERMISSION_DENIED)

        // When
        val result = underTest.permissionsGranted()

        // Then
        Assert.assertFalse(result)
    }

    @Test
    fun detectsPermissionGranted() {
        // Given
        whenever(
            context.checkPermission(
                eq(Manifest.permission.READ_EXTERNAL_STORAGE),
                any(),
                any()
            )
        ).thenReturn(PackageManager.PERMISSION_GRANTED)

        // When
        val result = underTest.permissionsGranted()

        // Then
        Assert.assertTrue(result)
    }
}
