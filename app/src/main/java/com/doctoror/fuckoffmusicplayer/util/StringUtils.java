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
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by Yaroslav Mytkalyk on 7/8/16.
 */
public final class StringUtils {

    private StringUtils() {

    }

    @NonNull
    public static String capWords(@NonNull final String input) {
        boolean prevWasWhiteSp = true;
        final char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (Character.isLetter(chars[i])) {
                if (prevWasWhiteSp) {
                    chars[i] = Character.toUpperCase(chars[i]);
                }
                prevWasWhiteSp = false;
            } else {
                prevWasWhiteSp = Character.isWhitespace(chars[i]);
            }
        }
        return new String(chars);
    }

    @NonNull
    public static String notNullString(@Nullable final String string) {
        return string != null ? string : "";
    }

    @Nullable
    public static String firstNonEmptyString(@NonNull final String[] array) {
        for (int i = 0; i < array.length; i++) {
            final String item = array[i];
            if (!TextUtils.isEmpty(item)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Like {@link android.database.DatabaseUtils#sqlEscapeString(String)}, but does not wrap the
     * input in single quotes
     *
     * @param input the value to escape
     * @return escaped input
     */
    @NonNull
    public static String sqlEscape(@NonNull final String input) {
        if (input.indexOf('\'') != -1) {
            final StringBuilder sb = new StringBuilder(input.length());
            final int length = input.length();
            for (int i = 0; i < length; i++) {
                final char c = input.charAt(i);
                if (c == '\'') {
                    sb.append('\'');
                }
                sb.append(c);
            }
            return sb.toString();
        } else {
            return input;
        }
    }
}


