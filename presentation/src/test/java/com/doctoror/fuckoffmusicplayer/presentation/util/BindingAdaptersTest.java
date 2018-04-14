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

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.doctoror.fuckoffmusicplayer.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link BindingAdapters} test
 */
@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public final class BindingAdaptersTest {

    @Test
    public void testSetActivated() {
        final View view = new View(RuntimeEnvironment.application);
        assertFalse(view.isActivated());

        BindingAdapters.setActivated(view, true);
        assertTrue(view.isActivated());

        BindingAdapters.setActivated(view, false);
        assertFalse(view.isActivated());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetFormattedDurationNegative() {
        BindingAdapters.setFormattedDuration(
                new TextView(RuntimeEnvironment.application), -1);
    }

    @Test(expected = NullPointerException.class)
    public void testSetFormattedDurationNull() {
        //noinspection ConstantConditions
        BindingAdapters.setFormattedDuration(null, 0);
    }

    @Test
    public void testSetFormattedDuration() {
        final TextView textView = new TextView(RuntimeEnvironment.application);
        assertEquals("", textView.getText());

        BindingAdapters.setFormattedDuration(textView, 0);
        assertEquals("0:00", textView.getText());

        BindingAdapters.setFormattedDuration(textView, 59);
        assertEquals("0:59", textView.getText());

        BindingAdapters.setFormattedDuration(textView, 60);
        assertEquals("1:00", textView.getText());

        BindingAdapters.setFormattedDuration(textView, 61);
        assertEquals("1:01", textView.getText());

        BindingAdapters.setFormattedDuration(textView, 3600);
        assertEquals("1:00:00", textView.getText());

        BindingAdapters.setFormattedDuration(textView, 3661);
        assertEquals("1:01:01", textView.getText());
    }

    @Test
    public void testSetRecyclerAdapter() {
        final Context context = RuntimeEnvironment.application;

        final RecyclerView recyclerView = new RecyclerView(context);
        assertNull(recyclerView.getAdapter());

        final RecyclerView.Adapter<?> adapter
                = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            @Override
            @NonNull
            public RecyclerView.ViewHolder onCreateViewHolder(
                    @NonNull final ViewGroup parent,
                    final int viewType) {
                //noinspection ConstantConditions
                return null;
            }

            @Override
            public void onBindViewHolder(
                    @NonNull final RecyclerView.ViewHolder holder, final int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        };

        BindingAdapters.setRecyclerAdapter(recyclerView, adapter);
        assertEquals(adapter, recyclerView.getAdapter());

        BindingAdapters.setRecyclerAdapter(recyclerView, null);
        assertNull(recyclerView.getAdapter());
    }

    @Test
    public void testSetImageResource() {
        final Context context = RuntimeEnvironment.application;

        final ImageView iv = new ImageView(context);
        assertNull(iv.getDrawable());

        BindingAdapters.setImageResource(iv, R.drawable.album_art_placeholder);

        assertNotNull(iv.getDrawable());
    }

    @Test
    public void testSetColorFilter() {
        final Context context = RuntimeEnvironment.application;

        final ImageView iv = new ImageView(context);
        assertNull(iv.getColorFilter());

        final int color = Color.CYAN;
        BindingAdapters.setColorFiler(iv, color);

        assertNotNull(iv.getColorFilter());
    }
}
