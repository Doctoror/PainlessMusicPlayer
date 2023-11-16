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
package com.doctoror.fuckoffmusicplayer.data.util;

import androidx.annotation.NonNull;

import java.util.Random;

/**
 * Holds {@link Random} instance
 */
public final class RandomHolder {

    @NonNull
    private static final RandomHolder INSTANCE = new RandomHolder();

    @NonNull
    public static RandomHolder getInstance() {
        return INSTANCE;
    }

    @NonNull
    private final Random mRandom = new Random();

    private RandomHolder() {

    }

    @NonNull
    public Random getRandom() {
        return mRandom;
    }
}
