package com.doctoror.fuckoffmusicplayer.library.livelists;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 08.11.16.
 */

interface LivePlaylist {

    CharSequence getTitle();

    @WorkerThread
    List<Media> create(@NonNull Context context);

}
