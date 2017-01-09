package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.queue.Media;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */

public interface PlaylistProviderRandom {

    List<Media> randomPlaylist();

}
