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
package com.doctoror.fuckoffmusicplayer.presentation.util;

import android.content.res.ColorStateList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;

/**
 * {@link ThemeUtils} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class ThemeUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testGetColorNullTheme() {
        //noinspection ConstantConditions
        ThemeUtils.getColor(null, android.R.attr.textColorPrimary);
    }

    @Test
    public void testGetColor() {
        ThemeUtils.getColor(RuntimeEnvironment.application.getTheme(),
                android.R.attr.textColorPrimary);
    }

    @Test(expected = NullPointerException.class)
    public void testGetColorStateListNullTheme() {
        //noinspection ConstantConditions
        ThemeUtils.getColorStateList(null, android.R.attr.textColorPrimary);
    }

    @Test
    public void testGetColorStateList() {
        final ColorStateList colorStateList = ThemeUtils.getColorStateList(
                RuntimeEnvironment.application.getTheme(),
                android.R.attr.textColorPrimary);

        assertNotNull(colorStateList);
    }

}
