package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.RecentlyScannedPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

import javax.inject.Inject;

/**
 * Recent 50 live playlist
 */
public final class LivePlaylistRecentlyScanned implements LivePlaylist {

    private final CharSequence mTitle;

    @Inject
    RecentlyScannedPlaylistFactory mPlaylistFactory;

    public LivePlaylistRecentlyScanned(@NonNull final Context context) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        mTitle = context.getText(R.string.Recently_scanned);
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @WorkerThread
    @Override
    public List<Media> create() {
        return mPlaylistFactory.loadRecentlyScannedPlaylist();
    }
}
