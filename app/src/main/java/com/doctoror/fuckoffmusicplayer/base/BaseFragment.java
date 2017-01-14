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
import android.support.v4.util.ArraySet;

import java.util.Collection;

import rx.Subscription;

/**
 * The base {@link Fragment}
 */
public abstract class BaseFragment extends Fragment {

    private final Collection<Subscription> mSubscriptions = new ArraySet<>();

    /**
     * Register a {@link Subscription} that will be unsubscribed onStop()
     *
     * @param subscription the {@link Subscription} to register
     * @return the registered {@link Subscription}
     */
    @NonNull
    @MainThread
    public Subscription registerOnStartSubscription(@NonNull final Subscription subscription) {
        //noinspection ConstantConditions
        if (subscription == null) {
            throw new NullPointerException("subscription must not be null");
        }
        mSubscriptions.add(subscription);
        return subscription;
    }

    @Override
    public void onStop() {
        super.onStop();
        for (final Subscription s : mSubscriptions) {
            s.unsubscribe();
        }
        mSubscriptions.clear();
    }
}
