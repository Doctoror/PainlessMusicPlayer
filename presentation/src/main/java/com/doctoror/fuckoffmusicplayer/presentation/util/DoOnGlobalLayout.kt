package com.doctoror.fuckoffmusicplayer.presentation.util

import android.view.View
import android.view.ViewTreeObserver

class DoOnGlobalLayout(private val view: View) {

    fun doOnGlobalLayout(action: Runnable) {
        view.viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {

                override fun onGlobalLayout() {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    action.run()
                }
            }
        )
    }
}
