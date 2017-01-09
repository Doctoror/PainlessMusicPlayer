package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Genre playlist factory
 */
public interface PlaylistProviderGenres {

    @Nullable
    @WorkerThread
    List<Media> fromGenre(long genreId);
}
