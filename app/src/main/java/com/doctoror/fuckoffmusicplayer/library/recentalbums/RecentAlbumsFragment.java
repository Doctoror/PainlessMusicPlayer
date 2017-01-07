package com.doctoror.fuckoffmusicplayer.library.recentalbums;

import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.inject.Inject;

import rx.Observable;

/**
 * Used for showing albums for genre
 */
public final class RecentAlbumsFragment extends ConditionalAlbumListFragment {

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);
    }

    @Override
    protected Observable<Cursor> load() {
        return mAlbumsProvider.loadRecentlyPlayedAlbums();
    }
}
