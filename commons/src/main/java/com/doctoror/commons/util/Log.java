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
package com.doctoror.commons.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Like android {@link android.util.Log}, but can be controlled what will be logged
 */
public final class Log {

    private static final int TAG_MAX = 23;

    private static boolean LOG_V = false;
    private static boolean LOG_D = false;
    private static boolean LOG_I = false;
    private static boolean LOG_W = false;
    private static boolean LOG_WTF = true;

    public static boolean logVEnabled() {
        return LOG_V;
    }

    public static boolean logDEnabled() {
        return LOG_D;
    }

    public static boolean logIEnabled() {
        return LOG_I;
    }

    public static boolean logWEnabled() {
        return LOG_W;
    }

    public static boolean logWTFEnabled() {
        return LOG_WTF;
    }

    public static void setLogV(final boolean logV) {
        LOG_V = logV;
    }

    public static void setLogD(final boolean logD) {
        LOG_D = logD;
    }

    public static void setLogI(final boolean logI) {
        LOG_I = logI;
    }

    public static void setLogW(final boolean logW) {
        LOG_W = logW;
    }

    public static void setLogWtf(final boolean logWtf) {
        LOG_WTF = logWtf;
    }

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

    public static int wtf(@NonNull final String tag, @Nullable final String msg,
            @Nullable final Throwable tr) {
        if (LOG_WTF) {
            return android.util.Log.wtf(tag(tag), msg, tr);
        }
        return 0;
    }
}
