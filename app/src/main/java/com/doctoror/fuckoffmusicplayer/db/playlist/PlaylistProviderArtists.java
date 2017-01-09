package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

public interface PlaylistProviderArtists {

    @Nullable
    @WorkerThread
    List<Media> fromArtist(final long artistId);


    @Nullable
    @WorkerThread
    List<Media> fromArtistSearch(@Nullable String query);
}
