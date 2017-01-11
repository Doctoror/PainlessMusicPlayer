package com.doctoror.fuckoffmusicplayer.home;

import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 11.01.17.
 */

final class AlbumItemsFactory {

    private AlbumItemsFactory() {
        throw new UnsupportedOperationException();
    }

    static List<AlbumItem> itemsFromCursor(@NonNull final Cursor c) {
        final List<AlbumItem> items = new ArrayList<>(c.getCount());
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            items.add(itemFromCursor(c));
        }
        return items;
    }

    @NonNull
    private static AlbumItem itemFromCursor(@NonNull final Cursor c) {
        final AlbumItem item = new AlbumItem();
        item.id = c.getLong(AlbumsProvider.COLUMN_ID);
        item.title = c.getString(AlbumsProvider.COLUMN_ALBUM);
        item.albumArt = c.getString(AlbumsProvider.COLUMN_ALBUM_ART);
        return item;
    }
}
