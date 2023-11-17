package com.doctoror.fuckoffmusicplayer.presentation.library

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import com.doctoror.fuckoffmusicplayer.RuntimePermissions
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner::class)
class LibraryPermissionsCheckerTest {

    private val context: Context = mock()
    private val runtimePermissions: RuntimePermissions = mock()

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
