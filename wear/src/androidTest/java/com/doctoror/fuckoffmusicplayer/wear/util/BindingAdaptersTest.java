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
package com.doctoror.fuckoffmusicplayer.wear.util;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import static org.junit.Assert.*;

/**
 * {@link BindingAdapters} test
 */
public final class BindingAdaptersTest {

    @Test
    public void testSetRecyclerAdapter() throws Exception {
        final Context context = InstrumentationRegistry.getContext();

        final RecyclerView recyclerView = new RecyclerView(context);
        assertNull(recyclerView.getAdapter());

        final RecyclerView.Adapter<?> adapter
                = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent,
                    final int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {

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

}
