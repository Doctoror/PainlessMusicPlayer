package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * "Recentply played albums" live playlist
 */
final class LivePlaylistRecentAlbums implements LivePlaylist {

    private final CharSequence mTitle;

    LivePlaylistRecentAlbums(@NonNull final Resources resources) {
        mTitle = resources.getText(R.string.Recently_played_albums);
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @WorkerThread
    @Override
    public List<Media> create(@NonNull final Context context) {
        throw new UnsupportedOperationException();
    }
}
