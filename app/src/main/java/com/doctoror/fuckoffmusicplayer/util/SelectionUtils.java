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

/**
 * Selection utils
 */
public final class SelectionUtils {

    private SelectionUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns ordering by long field
     *
     * @param column    the column name to build ordering for
     * @param arguments the arguments
     * @return  ordering by long field
     */
    @NonNull
    public static String orderByLongField(@NonNull final String column,
            @NonNull final long[] arguments) {
        //noinspection ConstantConditions
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("column must not be null or empty");
        }
        final StringBuilder order = new StringBuilder(256);
        boolean first = true;
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < arguments.length; i++) {
            if (first) {
                first = false;
            } else {
                order.append(',');
            }
            order.append(column).append('=').append(arguments[i]).append(" DESC");
        }
        return order.toString();
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
        //noinspection ConstantConditions
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("column must not be null or empty");
        }
        final StringBuilder selection = new StringBuilder(256);
        selection.append(column);
        selection.append(" IN ");
        selection.append('(');
        appendCommaSeparatedArgumentsLong(selection, arguments);
        selection.append(')');
        return selection.toString();
    }

    /**
     * Builds IN selection
     *
     * @param column    the column name to build selection for
     * @param arguments the arguments
     * @return the IN selection
     */
    @NonNull
    public static <T> String inSelection(@NonNull final String column,
            @NonNull final Iterable<T> arguments) {
        //noinspection ConstantConditions
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("column must not be null or empty");
        }
        final StringBuilder selection = new StringBuilder(256);
        selection.append(column);
        selection.append(" IN ");
        selection.append('(');
        appendCommaSeparatedArguments(selection, arguments);
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
            @NonNull final Iterable<T> arguments) {
        //noinspection ConstantConditions
        if (column == null || column.isEmpty()) {
            throw new IllegalArgumentException("column must not be null or empty");
        }
        final StringBuilder selection = new StringBuilder(256);
        selection.append(column);
        selection.append(" NOT IN ");
        selection.append('(');
        appendCommaSeparatedArguments(selection, arguments);
        selection.append(')');
        return selection.toString();
    }

    static <T> void appendCommaSeparatedArguments(@NonNull final StringBuilder target,
            @NonNull final Iterable<T> arguments) {
        //noinspection ConstantConditions
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        boolean first = true;
        for (final T arg : arguments) {
            if (first) {
                first = false;
            } else {
                target.append(',');
            }
            target.append('\'').append(arg).append('\'');
        }
    }

    static void appendCommaSeparatedArgumentsLong(@NonNull final StringBuilder target,
            @NonNull final long[] arguments) {
        //noinspection ConstantConditions
        if (target == null) {
            throw new NullPointerException("target must not be null");
        }
        boolean first = true;
        for (final long arg : arguments) {
            if (first) {
                first = false;
            } else {
                target.append(',');
            }
            target.append('\'').append(arg).append('\'');
        }
    }
}
