package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreVolumeNames;
import com.doctoror.fuckoffmusicplayer.db.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */
public final class PlaylistProviderGenresMediaStore implements PlaylistProviderGenres {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public PlaylistProviderGenresMediaStore(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @Nullable
    @Override
    public List<Media> fromGenre(final long genreId) {
        return mMediaProvider.load(MediaStore.Audio.Genres.Members.getContentUri(
                MediaStoreVolumeNames.EXTERNAL, genreId),
                MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC,
                null,
                "RANDOM()",
                PlaylistConfig.MAX_PLAYLIST_SIZE);
    }
}
