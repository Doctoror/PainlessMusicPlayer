/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.data.playback.unit

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Used for delayed stop action invocation.
 */
class PlaybackServiceUnitStopTimeout(private val stopAction: Runnable) {

    /**
     * The timeout after which [stopAction] should be performed, in seconds.
     */
    private val timeout = 8L

    /**
     * The [stopAction] timer [Disposable].
     */
    private var disposable: Disposable? = null

    /**
     * Start a timer after which the [stopAction] will be performed.
     */
    fun initializeStopTimer() {
        disposable = Observable
                .timer(timeout, TimeUnit.SECONDS)
                .subscribe { stopAction.run() }
    }

    /**
     * Abort [stopAction] invocation timer.
     */
    fun abortStopTimer() {
        disposable?.dispose()
        disposable = null
    }
}
