package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */
public final class MediaStoreGenrePlaylistFactory implements GenrePlaylistFactory {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public MediaStoreGenrePlaylistFactory(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @Nullable
    @Override
    public List<Media> fromGenre(final long genreId) {
        return mMediaProvider.load(TracksProvider.SELECTION_NON_HIDDEN_MUSIC,
                null,
                "RANDOM()",
                PlaylistConfig.MAX_PLAYLIST_SIZE);
    }
}
