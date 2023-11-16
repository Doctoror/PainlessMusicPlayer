package com.doctoror.fuckoffmusicplayer.domain.albums

import android.graphics.Bitmap

interface AlbumArtFetcher {

    @Throws(AlbumArtFetchException::class)
    fun fetch(
        uri: String,
        width: Int,
        height: Int
    ): Bitmap
}

class AlbumArtFetchException(cause: Exception) : Exception(cause)
