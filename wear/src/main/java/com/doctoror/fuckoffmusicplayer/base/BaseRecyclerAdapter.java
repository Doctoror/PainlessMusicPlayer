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
package com.doctoror.fuckoffmusicplayer.base;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 20.10.16.
 */

public abstract class BaseRecyclerAdapter<T, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    @NonNull
    private final LayoutInflater mLayoutInflater;

    private final List<T> mItems;

    public BaseRecyclerAdapter(@NonNull final Context context) {
        this(context, null);
    }

    public BaseRecyclerAdapter(@NonNull final Context context, @Nullable final List<T> items) {
        mLayoutInflater = LayoutInflater.from(context);
        if (items == null) {
            mItems = new ArrayList<>();
        } else {
            mItems = new ArrayList<>(items);
        }
    }

    /**
     * Constructor with ability to avoid shadow copy in case the input is already a shadow copy
     * created by subclass or when shadow copy is not needed
     *
     * @param context Context to get {@link LayoutInflater} from
     * @param items   initial items
     * @param nocopy  if true, will not create defensive copy of input
     */
    protected BaseRecyclerAdapter(@NonNull final Context context, @Nullable final List<T> items,
            final boolean nocopy) {
        mLayoutInflater = LayoutInflater.from(context);
        if (items == null) {
            mItems = null;
        } else if (nocopy) {
            mItems = items;
        } else {
            mItems = new ArrayList<>(items);
        }
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public void setItem(final int position, @Nullable final T item) {
        mItems.set(position, item);
        notifyItemChanged(position);
    }

    public void removeItem(final int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    protected final LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    public final T getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public final void setItems(@Nullable final List<T> items) {
        mItems.clear();
        if (items != null) {
            mItems.addAll(items);
        }
        notifyDataSetChanged();
    }
}
