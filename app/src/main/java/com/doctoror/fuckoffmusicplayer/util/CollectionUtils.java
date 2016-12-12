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
package com.doctoror.fuckoffmusicplayer.util;

import android.support.annotation.NonNull;

import java.util.Collection;

/**
 * Collection utils
 */
public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static long[] toLongArray(@NonNull final Collection<Long> collection) {
        final long[] longs = new long[collection.size()];
        int i = 0;
        for (final Long item : collection) {
            longs[i] = item;
            i++;
        }
        return longs;
    }

    @NonNull
    public static long[] toReverseLongArray(@NonNull final Collection<Long> collection) {
        final long[] longs = new long[collection.size()];
        int i = longs.length - 1;
        for (final Long item : collection) {
            longs[i] = item;
            i--;
        }
        return longs;
    }
}
