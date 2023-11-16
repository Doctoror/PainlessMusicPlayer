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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * {@link SelectionUtils} test
 */
public final class SelectionUtilsTest {


    @Test(expected = NullPointerException.class)
    public void testAppendCommaSeparatedArgumentsNull() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.appendCommaSeparatedArguments(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testAppendCommaSeparatedArgumentsNullColumn() throws Exception {
        //noinspection ConstantConditions,unchecked
        SelectionUtils.appendCommaSeparatedArguments(null, Collections.EMPTY_SET);
    }

    @Test(expected = NullPointerException.class)
    public void testAppendCommaSeparatedArgumentsNullArguments() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.appendCommaSeparatedArguments(new StringBuilder(), null);
    }

    @Test
    public void testAppendCommaSeparatedArgumentsEmptySet() throws Exception {
        final StringBuilder sb = new StringBuilder();
        //noinspection unchecked
        SelectionUtils.appendCommaSeparatedArguments(sb, Collections.EMPTY_SET);
        assertEquals(0, sb.length());
    }

    @Test
    public void testAppendCommaSeparatedArgumentsSingleArgument() throws Exception {
        final StringBuilder sb = new StringBuilder();
        SelectionUtils.appendCommaSeparatedArguments(sb, Collections.singleton("666"));
        assertEquals("'666'", sb.toString());
    }

    @Test
    public void testAppendCommaSeparatedArgumentsMultipleArguments() throws Exception {
        final StringBuilder sb = new StringBuilder();
        SelectionUtils.appendCommaSeparatedArguments(sb, Arrays.asList("666", "128"));
        assertEquals("'666','128'", sb.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testAppendCommaSeparatedArgumentsLongNull() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.appendCommaSeparatedArgumentsLong(null, null);
    }

    @Test(expected = NullPointerException.class)
    public void testAppendCommaSeparatedArgumentsLongNullColumn() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.appendCommaSeparatedArgumentsLong(null, new long[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testAppendCommaSeparatedArgumentsLongNullArguments() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.appendCommaSeparatedArgumentsLong(new StringBuilder(), null);
    }

    @Test
    public void testAppendCommaSeparatedArgumentsLongEmptySet() throws Exception {
        final StringBuilder sb = new StringBuilder();
        SelectionUtils.appendCommaSeparatedArgumentsLong(sb, new long[0]);
        assertEquals(0, sb.length());
    }

    @Test
    public void testAppendCommaSeparatedArgumentsLongSingleArgument() throws Exception {
        final StringBuilder sb = new StringBuilder();
        SelectionUtils.appendCommaSeparatedArgumentsLong(sb, new long[] {666});
        assertEquals("'666'", sb.toString());
    }

    @Test
    public void testAppendCommaSeparatedArgumentsLongMultipleArguments() throws Exception {
        final StringBuilder sb = new StringBuilder();
        SelectionUtils.appendCommaSeparatedArgumentsLong(sb, new long[] {666, 1024});
        assertEquals("'666','1024'", sb.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrderByLongFieldNull() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.orderByLongField(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrderByLongFieldNullColumn() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.orderByLongField(null, new long[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOrderByLongFieldEmptyColumn() throws Exception {
        SelectionUtils.orderByLongField("", new long[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testOrderByLongFieldNullArgument() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.orderByLongField("Arse", null);
    }

    @Test
    public void testOrderByLongFieldNoArguments() throws Exception {
        assertEquals("", SelectionUtils.orderByLongField("crap", new long[0]));
    }

    @Test
    public void testOrderByLongFieldSingleArgument() throws Exception {
        final String column = "crap";
        final long[] args = new long[]{666};
        assertEquals("crap=666 DESC", SelectionUtils.orderByLongField(column, args));
    }

    @Test
    public void testOrderByLongField() throws Exception {
        final String column = "shit";
        final long[] args = new long[]{9, 6, 10};
        assertEquals("shit=9 DESC,shit=6 DESC,shit=10 DESC",
                SelectionUtils.orderByLongField(column, args));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInSelectionLongNull() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.inSelectionLong(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInSelectionLongNullColumn() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.inSelectionLong(null, new long[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInSelectionLongEmptyColumn() throws Exception {
        SelectionUtils.inSelectionLong("", new long[0]);
    }

    @Test(expected = NullPointerException.class)
    public void testInSelectionLongNullArguments() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.inSelectionLong("a", null);
    }

    @Test
    public void testInSelectionLongNoArguments() throws Exception {
        assertEquals("crap IN ()", SelectionUtils.inSelectionLong("crap", new long[0]));
    }

    @Test
    public void testInSelectionLongSingleArgument() throws Exception {
        assertEquals("crap IN ('666')", SelectionUtils.inSelectionLong("crap", new long[]{666}));
    }

    @Test
    public void testInSelectionLongMultipleArguments() throws Exception {
        assertEquals("crap IN ('666','128')",
                SelectionUtils.inSelectionLong("crap", new long[]{666, 128}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotInSelectionNull() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.notInSelection(null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotInSelectionNullColumn() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.notInSelection(null, Collections.EMPTY_SET);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotInSelectionEmptyColumn() throws Exception {
        SelectionUtils.notInSelection("", Collections.EMPTY_SET);
    }

    @Test(expected = NullPointerException.class)
    public void testNotInSelectionNullArguments() throws Exception {
        //noinspection ConstantConditions
        SelectionUtils.notInSelection("a", null);
    }

    @Test
    public void testNotInSelectionNoArguments() throws Exception {
        assertEquals("crap NOT IN ()", SelectionUtils.notInSelection("crap", Collections.EMPTY_SET));
    }

    @Test
    public void testNotInSelectionSingleArgument() throws Exception {
        assertEquals("crap NOT IN ('666')",
                SelectionUtils.notInSelection("crap", Collections.singleton("666")));
    }

    @Test
    public void testNotInSelectionMultipleArguments() throws Exception {
        assertEquals("crap NOT IN ('666','128')",
                SelectionUtils.notInSelection("crap", Arrays.asList("666", "128")));
    }
}
