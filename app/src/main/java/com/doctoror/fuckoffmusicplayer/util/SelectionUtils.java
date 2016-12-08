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
import java.util.Collections;

/**
 * Created by Yaroslav Mytkalyk on 6/23/16.
 */
public final class SelectionUtils {

    private SelectionUtils() {
    }

    /**
     * Builds IN selection with long args
     *
     * @param column    the column name to build selection for
     * @param arguments the arguments
     * @return the IN selection
     */
    @NonNull
    public static String inSelectionLong(@NonNull final String column,
                                          @NonNull final long[] arguments) {
        final StringBuilder selection = new StringBuilder(256);
        selection.append(column);
        selection.append(" IN (");
        boolean first = true;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < arguments.length; i++) {
            if (first) {
                first = false;
            } else {
                selection.append(',');
            }
            selection.append('\'').append(arguments[i]).append('\'');
        }
        selection.append(')');
        return selection.toString();
    }

    /**
     * Builds IN selection with long args
     *
     * @param column    the column name to build selection for
     * @param arguments the arguments
     * @return the IN selection
     */
    @NonNull
    public static <T> String notInSelection(@NonNull final String column,
            @NonNull final Collection<T> arguments) {
        final StringBuilder selection = new StringBuilder(256);
        selection.append(column);
        selection.append(" NOT IN (");
        boolean first = true;
        for (final T arg : arguments) {
            if (first) {
                first = false;
            } else {
                selection.append(',');
            }
            selection.append('\'').append(arg).append('\'');
        }
        selection.append(')');
        return selection.toString();
    }
}
