/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.widget;

import android.graphics.Rect;

import androidx.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

/**
 * Holds insets.
 */
public final class InsetsHolder {

    private static final InsetsHolder INSTANCE = new InsetsHolder();

    @NonNull
    public static InsetsHolder getInstance() {
        return INSTANCE;
    }

    private final Subject<Rect> mInsetsSubject = BehaviorSubject.create();

    private final Rect mInsets = new Rect();

    public void onInsetsChanged(@NonNull final Rect insets) {
        mInsets.set(insets);
        mInsetsSubject.onNext(mInsets);
    }

    @NonNull
    public Observable<Rect> observable() {
        return mInsetsSubject;
    }
}
