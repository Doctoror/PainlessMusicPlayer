package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.db.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.SqlUtils;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public final class MediaStoreAlbumPlaylistFactory implements AlbumPlaylistFactory {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public MediaStoreAlbumPlaylistFactory(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @Nullable
    @Override
    public List<Media> fromAlbumSearch(@Nullable final String query) {
        final StringBuilder sel = new StringBuilder(256);
        sel.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC);
        if (!TextUtils.isEmpty(query)) {
            sel.append(" AND ").append(MediaStore.Audio.Media.ALBUM).append(" LIKE ")
                    .append(SqlUtils.escapeAndWrapForLikeArgument(query));
        }

        return mMediaProvider.load(sel.toString(),
                null,
                MediaStore.Audio.Media.ALBUM + ',' + MediaStore.Audio.Media.TRACK,
                PlaylistConfig.MAX_PLAYLIST_SIZE);
    }

    @Nullable
    @Override
    public List<Media> fromAlbum(final long albumId) {
        return fromAlbums(new long[]{albumId}, null);
    }

    @Nullable
    @Override
    public List<Media> fromAlbums(@NonNull final long[] albumIds, @Nullable final Long forArtist) {
        final List<Media> playlist = new ArrayList<>(15 * albumIds.length);
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < albumIds.length; i++) {
            final long albumId = albumIds[i];

            final StringBuilder selection = new StringBuilder(256);
            selection.append(MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC).append(" AND ");
            selection.append(MediaStore.Audio.Media.ALBUM_ID).append('=').append(albumId);
            if (forArtist != null) {
                selection.append(" AND ")
                        .append(MediaStore.Audio.Media.ARTIST_ID).append('=').append(forArtist);
            }

            playlist.addAll(mMediaProvider.load(selection.toString(), null,
                    MediaStore.Audio.Media.TRACK, null));
        }
        return playlist;
    }
}
