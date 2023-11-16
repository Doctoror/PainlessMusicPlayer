package com.doctoror.fuckoffmusicplayer.data.albums

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetchException
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher
import java.io.IOException
import java.util.concurrent.ExecutionException

class AlbumArtFetcherImpl(
    private val contentResolver: ContentResolver,
    private val requestManager: RequestManager
) : AlbumArtFetcher {

    override fun fetch(
        uri: String,
        width: Int,
        height: Int
    ): Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        fetchApiLevel29(uri, width, height)
    } else {
        fetchLegacy(uri, width, height)
    }

    private fun fetchLegacy(
        uri: String,
        width: Int,
        height: Int
    ): Bitmap = try {
        requestManager
            .asBitmap()
            .apply(
                RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .centerCrop()
                    .dontAnimate()
            )
            .load(uri)
            .submit(width, height)
            .get()
    } catch (e: InterruptedException) {
        throw AlbumArtFetchException(e)
    } catch (e: ExecutionException) {
        throw AlbumArtFetchException(e)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun fetchApiLevel29(
        uri: String,
        width: Int,
        height: Int
    ): Bitmap = try {
        contentResolver.loadThumbnail(Uri.parse(uri), Size(width, height), null)
    } catch (e: IOException) {
        throw AlbumArtFetchException(e)
    }
}
