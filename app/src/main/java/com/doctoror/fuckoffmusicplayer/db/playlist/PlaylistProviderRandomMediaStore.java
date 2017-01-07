package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */

public final class PlaylistProviderRandomMediaStore implements PlaylistProviderRandom {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public PlaylistProviderRandomMediaStore(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @Override
    public List<Media> randomPlaylist() {
        return mMediaProvider.load(
                MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC,
                null,
                "RANDOM()",
                PlaylistConfig.MAX_PLAYLIST_SIZE);
    }
}
