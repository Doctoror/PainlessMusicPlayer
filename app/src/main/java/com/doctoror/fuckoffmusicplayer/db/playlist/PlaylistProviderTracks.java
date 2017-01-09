package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

public interface PlaylistProviderTracks {

    @Nullable
    List<Media> fromTracks(@NonNull long[] trackIds, @Nullable String sortOrder);

    @Nullable
    @WorkerThread
    List<Media> fromTracksSearch(@Nullable String query);

}
