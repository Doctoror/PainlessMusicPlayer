package com.doctoror.fuckoffmusicplayer.presentation.library

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class LibraryPermissionsChecker(private val context: Context) {

    fun permissionsGranted() = ContextCompat.checkSelfPermission(
        context,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    ) == PackageManager.PERMISSION_GRANTED
}
