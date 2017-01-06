package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

public interface TrackPlaylistFactory {

    @Nullable
    List<Media> forTracks(@NonNull long[] trackIds, @Nullable String sortOrder);

    @Nullable
    @WorkerThread
    List<Media> fromTracksSearch(@Nullable String query);

}
