package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksQuery;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistFactory;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Random 50 live playlist
 */
public final class LivePlaylistRandom50 implements LivePlaylist {

    private final CharSequence mTitle;

    public LivePlaylistRandom50(@NonNull final Resources resources) {
        mTitle = resources.getText(R.string.Random_50);
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @WorkerThread
    @Override
    public List<Media> create(@NonNull final Context context) {
        return PlaylistFactory.fromSelection(context.getContentResolver(),
                TracksQuery.SELECTION_NON_HIDDEN_MUSIC,
                null,
                "RANDOM()",
                50);
    }

}
