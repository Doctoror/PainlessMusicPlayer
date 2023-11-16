package com.doctoror.fuckoffmusicplayer.presentation.util

import android.graphics.Bitmap
import android.widget.ImageView
import com.doctoror.commons.util.Log
import com.doctoror.fuckoffmusicplayer.R
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

private const val PLACEHOLDER = R.drawable.album_art_placeholder

class AlbumArtIntoTargetApplier(private val albumArtFetcher: AlbumArtFetcher) {

    fun apply(
        uri: String?,
        target: ImageView,
        listener: Listener?
    ) {
        if (uri.isNullOrBlank()) {
            target.setImageResource(PLACEHOLDER)
            listener?.onSuccess()
            return
        }

        if (target.width != 0 && target.height != 0) {
            onSizeAvailable(uri, target, listener)
        }

        DoOnGlobalLayout(target).doOnGlobalLayout {
            onSizeAvailable(uri, target, listener)
        }
    }

    private fun onSizeAvailable(
        uri: String,
        target: ImageView,
        listener: Listener?
    ) {
        Single
            .fromCallable {
                albumArtFetcher.fetch(
                    uri,
                    target.width,
                    target.height
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Bitmap> {
                override fun onSubscribe(d: Disposable) {}
                override fun onSuccess(bitmap: Bitmap) {
                    target.setImageBitmap(bitmap)
                    listener?.onSuccess()
                }

                override fun onError(e: Throwable) {
                    Log.w("AlbumArtIntoTargetApplier", "onError", e)
                    target.setImageResource(PLACEHOLDER)
                    listener?.onFailure()
                }
            })
    }

    interface Listener {

        fun onSuccess()

        fun onFailure()
    }
}
