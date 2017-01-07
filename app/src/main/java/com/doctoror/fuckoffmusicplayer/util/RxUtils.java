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
