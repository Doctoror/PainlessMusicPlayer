package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Genre playlist factory
 */
public interface GenrePlaylistFactory {

    @Nullable
    @WorkerThread
    List<Media> fromGenre(long genreId);
}
