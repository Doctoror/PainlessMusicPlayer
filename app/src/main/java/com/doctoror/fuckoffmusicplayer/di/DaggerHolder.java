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
package com.doctoror.fuckoffmusicplayer.di;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Dagger2 {@link MainComponent} holder
 */
public final class DaggerHolder {

    private static volatile DaggerHolder sInstance;

    @NonNull
    public static DaggerHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (DaggerHolder.class) {
                if (sInstance == null) {
                    sInstance = new DaggerHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final MainComponent mMainComponent;

    private DaggerHolder(@NonNull final Context context) {
        final AppModule appContextModule = new AppModule(context);
        mMainComponent = DaggerMainComponent.builder()
                .appContextModule(appContextModule)
                .build();
    }

    @NonNull
    public MainComponent mainComponent() {
        return mMainComponent;
    }
}
