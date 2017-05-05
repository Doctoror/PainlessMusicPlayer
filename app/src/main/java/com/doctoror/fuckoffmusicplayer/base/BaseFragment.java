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
package com.doctoror.fuckoffmusicplayer.base;

import android.app.Fragment;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * The base {@link Fragment}
 */
public abstract class BaseFragment extends Fragment {

    private final Object mOnStopDisposableLock = new Object();

    private CompositeDisposable mOnStopDisposable;

    @NonNull
    private CompositeDisposable getOnStopDisposable() {
        synchronized (mOnStopDisposableLock) {
            if (mOnStopDisposable == null) {
                mOnStopDisposable = new CompositeDisposable();
            }
            return mOnStopDisposable;
        }
    }

    /**
     * Register a {@link Disposable} that will be disposed onStop()
     *
     * @param disposable the {@link Disposable} to register
     * @return the registered {@link Disposable}
     */
    @NonNull
    @MainThread
    public Disposable disposeOnStop(@NonNull final Disposable disposable) {
        //noinspection ConstantConditions
        if (disposable == null) {
            throw new NullPointerException("disposable must not be null");
        }
        getOnStopDisposable().add(disposable);
        return disposable;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mOnStopDisposable != null) {
            synchronized (mOnStopDisposableLock) {
                if (mOnStopDisposable != null) {
                    mOnStopDisposable.dispose();
                    mOnStopDisposable = null;
                }
            }
        }
    }
}
