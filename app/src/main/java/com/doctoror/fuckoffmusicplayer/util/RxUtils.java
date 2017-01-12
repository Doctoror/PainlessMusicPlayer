/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.util;

import com.doctoror.commons.util.Log;

import android.support.annotation.NonNull;

import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Subscription;

/**
 * RxJava utils
 */
public final class RxUtils {

    private static final String TAG = "RxUtils";

    private RxUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Subscribes and blocks until onNext() is called. The value from onNext() is read into value
     * {@link Box}
     *
     * @param observable {@link Observable} to subscribe
     * @param value      container to set value to
     * @param <T>        the type of values emitted from {@link Observable}
     */
    public static <T> void subscribeBlocking(@NonNull final Observable<T> observable,
            @NonNull final Box<T> value) {
        final CountDownLatch cdl = new CountDownLatch(1);
        final Subscription subscription = observable
                .subscribe((results) -> {
                            value.setValue(results);
                            cdl.countDown();
                        }
                );
        try {
            cdl.await();
        } catch (InterruptedException e) {
            Log.w(TAG, e);
        }
        subscription.unsubscribe();
    }
}
