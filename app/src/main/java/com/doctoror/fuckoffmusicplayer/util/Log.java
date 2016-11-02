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

import com.doctoror.fuckoffmusicplayer.BuildConfig;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Like android {@link android.util.Log}, but can be controlled what will be logged
 */
public final class Log {

    private static final int TAG_MAX = 23;

    private static final boolean LOG_V = false;
    private static final boolean LOG_D = BuildConfig.DEBUG;
    private static final boolean LOG_I = BuildConfig.DEBUG;
    private static final boolean LOG_W = BuildConfig.DEBUG;

    @NonNull
    private static String tag(@NonNull final String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new RuntimeException("tag must not be empty");
        }
        if (tag.length() > TAG_MAX) {
            return tag.substring(0, TAG_MAX);
        }
        return tag;
    }

    public static int v(@NonNull final String tag, @Nullable final String msg) {
        if (LOG_V) {
            return android.util.Log.v(tag(tag), msg);
        }
        return 0;
    }

    public static int v(@NonNull final String tag, @Nullable final String msg,
            @Nullable final Throwable tr) {
        if (LOG_V) {
            return android.util.Log.v(tag(tag), msg, tr);
        }
        return 0;
    }

    public static int d(@NonNull final String tag, @Nullable final String msg) {
        if (LOG_D) {
            return android.util.Log.d(tag(tag), msg);
        }
        return 0;
    }

    public static int d(@NonNull final String tag, @Nullable final String msg,
            @Nullable final Throwable tr) {
        if (LOG_D) {
            return android.util.Log.d(tag(tag), msg, tr);
        }
        return 0;
    }

    public static int i(@NonNull final String tag, @Nullable final String msg) {
        if (LOG_I) {
            return android.util.Log.i(tag(tag), msg);
        }
        return 0;
    }

    public static int i(@NonNull final String tag, @Nullable final String msg,
            @Nullable final Throwable tr) {
        if (LOG_I) {
            return android.util.Log.i(tag(tag), msg, tr);
        }
        return 0;
    }

    public static int w(@NonNull final String tag, @Nullable final String msg) {
        if (LOG_W) {
            return android.util.Log.w(tag(tag), msg);
        }
        return 0;
    }

    public static int w(@NonNull final String tag, @Nullable final Throwable tr) {
        if (LOG_W) {
            return android.util.Log.w(tag(tag), tr);
        }
        return 0;
    }

    public static int w(@NonNull final String tag, @Nullable final String msg,
            @Nullable final Throwable tr) {
        if (LOG_W) {
            return android.util.Log.w(tag(tag), msg, tr);
        }
        return 0;
    }
}
