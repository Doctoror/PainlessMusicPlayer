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

import org.junit.Test;

import android.content.res.ColorStateList;
import android.support.test.InstrumentationRegistry;

import static org.junit.Assert.*;

/**
 * {@link ThemeUtils} test
 */
public final class ThemeUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testGetColorNullTheme() throws Exception {
        //noinspection ConstantConditions
        ThemeUtils.getColor(null, android.R.attr.textColorPrimary);
    }

    @Test
    public void testGetColor() throws Exception {
        ThemeUtils.getColor(InstrumentationRegistry.getTargetContext().getTheme(),
                android.R.attr.textColorPrimary);
    }

    @Test(expected = NullPointerException.class)
    public void testGetColorStateListNullTheme() throws Exception {
        //noinspection ConstantConditions
        ThemeUtils.getColorStateList(null, android.R.attr.textColorPrimary);
    }

    @Test
    public void testGetColorStateList() throws Exception {
        final ColorStateList colorStateList = ThemeUtils.getColorStateList(
                InstrumentationRegistry.getTargetContext().getTheme(),
                android.R.attr.textColorPrimary);

        assertNotNull(colorStateList);
    }

}
