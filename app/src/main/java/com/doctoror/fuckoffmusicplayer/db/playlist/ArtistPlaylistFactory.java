package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.ContentResolver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

public interface ArtistPlaylistFactory {

    @Nullable
    @WorkerThread
    List<Media> fromArtist(final long artistId);


    @Nullable
    @WorkerThread
    List<Media> fromArtistSearch(@Nullable String query);
}
