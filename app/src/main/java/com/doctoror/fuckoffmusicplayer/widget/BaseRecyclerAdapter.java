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
package com.doctoror.fuckoffmusicplayer.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
        this(context, (List<T>) null);
    }

    public BaseRecyclerAdapter(@NonNull final Context context, @Nullable final T[] items) {
        this(context, items == null ? null : Arrays.asList(items));
    }

    public BaseRecyclerAdapter(@NonNull final Context context, @Nullable final List<T> items) {
        mLayoutInflater = LayoutInflater.from(context);
        if (items == null) {
            mItems = new ArrayList<>(15);
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

    public final boolean removeItem(@NonNull final T item) {
        final int position = mItems.indexOf(item);
        if (position != -1 && mItems.remove(position) != null) {
            notifyItemRemoved(position);
            return true;
        }
        return false;
    }

    @NonNull
    protected final LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    public final T getItem(final int position) {
        return mItems.get(position);
    }

    public final int indexOf(@NonNull final T item) {
        return mItems.indexOf(item);
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

    public final void setItems(@Nullable final T[] items) {
        setItems(items == null ? null : Arrays.asList(items));
    }

    public final void swap(final int i, final int j) {
        Collections.swap(mItems, i, j);
    }
}
