package com.doctoror.fuckoffmusicplayer.root;

import com.doctoror.fuckoffmusicplayer.R;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.wearable.view.drawer.WearableNavigationDrawer;

/**
 * Created by Yaroslav Mytkalyk on 17.11.16.
 */

final class RootNavigationAdapter
        extends WearableNavigationDrawer.WearableNavigationDrawerAdapter {

    interface OnItemSelectedListener {

        void onItemSelected(int id);
    }

    static final int ID_NOW_PLAYING = 0;
    static final int ID_PLAYLIST = 1;

    @NonNull
    private final Item[] mItems = new Item[2];

    @NonNull
    private final OnItemSelectedListener mOnItemSelectedListener;

    public RootNavigationAdapter(@NonNull final Context context,
            @NonNull final OnItemSelectedListener listener) {
        mItems[0] = new Item(ID_NOW_PLAYING, context.getString(R.string.Now_Playing),
                context.getDrawable(R.drawable.ic_play_arrow_white_24dp));

        mItems[1] = new Item(ID_PLAYLIST, context.getString(R.string.Playlist),
                context.getDrawable(R.drawable.ic_playlist_play_white_24dp));

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
