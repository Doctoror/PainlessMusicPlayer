package com.doctoror.fuckoffmusicplayer.playlist;

import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import java.util.Collection;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */

public interface RecentPlaylistsManager {

    @NonNull
    long[] getRecentAlbums();

    @WorkerThread
    void storeAlbumsSync(@NonNull Collection<Long> albumIds);

    void storeAlbum(long albumId);

    void clear();
}
