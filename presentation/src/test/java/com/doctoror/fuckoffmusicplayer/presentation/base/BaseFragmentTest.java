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
package com.doctoror.fuckoffmusicplayer.presentation.base;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.annotation.SuppressLint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.reactivex.disposables.Disposable;

/**
 * {@link BaseFragment} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class BaseFragmentTest {

    @Test
    public void testCompositeDisposables() {
        final Disposable disposable1 = new TrackableDisposable();
        final Disposable disposable2 = new TrackableDisposable();

        @SuppressLint("ValidFragment")
        final BaseFragment baseFragment = new BaseFragment() {
        };

        baseFragment.disposeOnStop(disposable1);
        baseFragment.disposeOnStop(disposable2);

        assertFalse(disposable1.isDisposed());
        assertFalse(disposable2.isDisposed());

        baseFragment.onStop();

        assertTrue(disposable1.isDisposed());
        assertTrue(disposable2.isDisposed());
    }
}
