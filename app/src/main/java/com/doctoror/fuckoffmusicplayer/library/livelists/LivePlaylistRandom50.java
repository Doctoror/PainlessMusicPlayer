package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistUtils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 08.11.16.
 */

final class LivePlaylistRandom50 implements LivePlaylist {

    private final CharSequence mTitle;

    LivePlaylistRandom50(@NonNull final Resources resources) {
        mTitle = resources.getText(R.string.Random_50);
    }

    @Override
    public CharSequence getTitle() {
        return mTitle;
    }

    @WorkerThread
    @Override
    public List<Media> create(@NonNull final Context context) {
        return PlaylistUtils.forSelection(context.getContentResolver(),
                null,
                null,
                "RANDOM()",
                50);
    }

}
