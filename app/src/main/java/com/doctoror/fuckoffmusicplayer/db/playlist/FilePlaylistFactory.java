package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * "File" playlist factory
 */
public interface FilePlaylistFactory {

    @NonNull
    List<Media> forFile(@NonNull Uri uri) throws Exception;

}
