package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.playlist.Media;

import java.util.List;

/**
 * Factory for creating "recently scanned" playlist
 */
public interface RecentlyScannedPlaylistFactory {

    List<Media> loadRecentlyScannedPlaylist();

}
