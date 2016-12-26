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

import com.doctoror.commons.R;

import org.junit.Test;

import android.content.Context;
import android.graphics.Color;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import static org.junit.Assert.*;

/**
 * {@link BindingAdapters} test
 */
public final class BindingAdaptersTest {

    @Test
    public void testSetDisplayedChild() {
        final Context context = InstrumentationRegistry.getContext();

        final ViewAnimator va = new ViewAnimator(context);
        va.addView(new View(context));
        va.addView(new View(context));
        va.addView(new View(context));

        BindingAdapters.setDisplayedChild(va, 1);
        assertEquals(1, va.getDisplayedChild());
    }

    @Test
    public void testSetImageResource() {
        final Context context = InstrumentationRegistry.getContext();

        final ImageView iv = new ImageView(context);
        assertNull(iv.getDrawable());

        BindingAdapters.setImageResource(iv, R.drawable.album_art_placeholder);

        assertNotNull(iv.getDrawable());
    }

    @Test
    public void testSetColorFilter() {
        final Context context = InstrumentationRegistry.getContext();

        final ImageView iv = new ImageView(context);
        assertNull(iv.getColorFilter());

        final int color = Color.CYAN;
        BindingAdapters.setColorFiler(iv, color);

        assertNotNull(iv.getColorFilter());
    }

}
