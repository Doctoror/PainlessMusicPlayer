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
package com.doctoror.fuckoffmusicplayer.presentation.base;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BasePresenter implements LifecycleObserver {

    private final Object onStopDisposableLock = new Object();

    private CompositeDisposable onStopDisposable;

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        if (onStopDisposable != null) {
            synchronized (onStopDisposableLock) {
                if (onStopDisposable != null) {
                    onStopDisposable.dispose();
                    onStopDisposable = null;
                }
            }
        }
    }

    /**
     * Register a {@link Disposable} that will be disposed onStop()
     *
     * @param disposable the {@link Disposable} to register
     */
    @NonNull
    @MainThread
    protected Disposable disposeOnStop(@NonNull final Disposable disposable) {
        //noinspection ConstantConditions
        if (disposable == null) {
            throw new NullPointerException("disposable must not be null");
        }
        getOnStopDisposable().add(disposable);
        return disposable;
    }

    @NonNull
    private CompositeDisposable getOnStopDisposable() {
        synchronized (onStopDisposableLock) {
            if (onStopDisposable == null) {
                onStopDisposable = new CompositeDisposable();
            }
            return onStopDisposable;
        }
    }
}
