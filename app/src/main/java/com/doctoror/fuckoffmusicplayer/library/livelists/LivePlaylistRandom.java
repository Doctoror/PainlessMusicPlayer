package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.RandomPlaylistFactory;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

import javax.inject.Inject;

/**
 * Random 50 live playlist
 */
public final class LivePlaylistRandom implements LivePlaylist {

    private final CharSequence mTitle;

    @Inject
    RandomPlaylistFactory mPlaylistFactory;

    public LivePlaylistRandom(@NonNull final Context context) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        mTitle = context.getText(R.string.Random_playlist);
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @WorkerThread
    @Override
    public List<Media> create() {
        return mPlaylistFactory.randomPlaylist();
    }

}
