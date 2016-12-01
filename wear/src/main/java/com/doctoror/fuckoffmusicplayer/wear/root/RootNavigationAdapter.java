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
package com.doctoror.fuckoffmusicplayer.wear.root;

import com.doctoror.fuckoffmusicplayer.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.wearable.view.drawer.WearableNavigationDrawer;

/**
 * Root navigation drawer adapter
 */
final class RootNavigationAdapter
        extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

    interface OnItemSelectedListener {

        void onItemSelected(int id);
    }

    static final int ID_NOW_PLAYING = 0;
    static final int ID_PLAYLIST = 1;
    static final int ID_SEARCH = 2;

    @NonNull
    private final Item[] mItems = new Item[3];

    @NonNull
    private final OnItemSelectedListener mOnItemSelectedListener;

    RootNavigationAdapter(@NonNull final Context context,
            @NonNull final OnItemSelectedListener listener) {
        mItems[0] = new Item(ID_NOW_PLAYING, context.getString(R.string.Now_Playing),
                context.getDrawable(R.drawable.ic_play_arrow_white_24dp));

        mItems[1] = new Item(ID_PLAYLIST, context.getString(R.string.Playlist),
                context.getDrawable(R.drawable.ic_playlist_play_white_24dp));

        mItems[2] = new Item(ID_SEARCH, context.getString(R.string.Search),
                context.getDrawable(R.drawable.ic_search_white_24dp));

        mOnItemSelectedListener = listener;
    }

    @Override
    public String getItemText(final int i) {
        return mItems[i].title;
    }

    @Override
    public Drawable getItemDrawable(final int i) {
        return mItems[i].icon;
    }

    @Override
    public void onItemSelected(final int i) {
        mOnItemSelectedListener.onItemSelected(mItems[i].id);
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    private static final class Item {

        final int id;
        final String title;
        final Drawable icon;

        Item(final int id, final String title, final Drawable icon) {
            this.id = id;
            this.title = title;
            this.icon = icon;
        }
    }
}
