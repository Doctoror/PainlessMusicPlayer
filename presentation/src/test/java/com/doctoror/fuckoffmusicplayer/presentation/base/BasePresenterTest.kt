package com.doctoror.fuckoffmusicplayer.presentation.base

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BasePresenterTest {

    @Test
    fun disposesOnStop() {
        val disposable1 = TrackableDisposable()
        val disposable2 = TrackableDisposable()

        val basePresenter = object : BasePresenter() {

        }

        basePresenter.disposeOnStop(disposable1)
        basePresenter.disposeOnStop(disposable2)

        assertFalse(disposable1.isDisposed)
        assertFalse(disposable2.isDisposed)

        basePresenter.onStop()

        assertTrue(disposable1.isDisposed)
        assertTrue(disposable2.isDisposed)
    }
}
