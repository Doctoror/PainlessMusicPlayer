package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

public interface PlaylistProviderAlbums {

    @Nullable
    @WorkerThread
    List<Media> fromAlbumSearch(@Nullable String query);

    @Nullable
    @WorkerThread
    List<Media> fromAlbum(long albumId);

    @Nullable
    @WorkerThread
    List<Media> fromAlbums(@NonNull long[] albumIds, @Nullable Long forArtist);

}
