package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.TracksProvider;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.SqlUtils;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */

public final class MediaStoreArtistPlaylistFactory implements ArtistPlaylistFactory {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public MediaStoreArtistPlaylistFactory(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @Nullable
    @Override
    public List<Media> fromArtist(final long artistId) {
        return mMediaProvider.load(TracksProvider.SELECTION_NON_HIDDEN_MUSIC + " AND "
                        + MediaStore.Audio.Media.ARTIST_ID + '=' + artistId,
                null,
                MediaStore.Audio.Media.ALBUM_ID + ',' + MediaStore.Audio.Media.TRACK,
                null);
    }

    @Nullable
    @Override
    public List<Media> fromArtistSearch(@Nullable final String query) {
        final StringBuilder sel = new StringBuilder(256);
        sel.append(TracksProvider.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            sel.append(" AND ").append(MediaStore.Audio.Media.ARTIST).append(" LIKE ")
                    .append(SqlUtils.escapeAndWrapForLikeArgument(query));
        }

        return mMediaProvider.load(sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK,
                PlaylistConfig.MAX_PLAYLIST_SIZE);
    }
}
