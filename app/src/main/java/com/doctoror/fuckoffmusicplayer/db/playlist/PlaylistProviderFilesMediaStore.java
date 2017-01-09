package com.doctoror.fuckoffmusicplayer.db.playlist;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.db.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 06.01.17.
 */
public final class PlaylistProviderFilesMediaStore implements PlaylistProviderFiles {

    private static final String TAG = "MediaStoreFilePlaylistFactory";

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public PlaylistProviderFilesMediaStore(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @NonNull
    @Override
    public List<Media> fromFile(@NonNull final Uri uri) throws Exception {
        final List<Media> playlist = mMediaProvider.load(MediaStore.Audio.Media.DATA.concat("=?"),
                new String[]{uri.getPath()},
                null,
                null);

        if (playlist.isEmpty()) {
            // Not found in MediaStore. Create Media from file data
            playlist.add(mediaFromFile(uri));
        }

        return playlist;
    }

    @NonNull
    private static Media mediaFromFile(@NonNull final Uri uri) throws Exception {
        final MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(uri.getPath());

            final Media media = new Media();
            media.setTitle(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            media.setArtist(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            media.setAlbum(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            media.setData(uri);

            final String duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (!TextUtils.isEmpty(duration)) {
                try {
                    media.setDuration(Long.parseLong(duration));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mediaFromFile() duration is not a number: " + duration);
                }
            }

            final String trackNumber = r
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            if (!TextUtils.isEmpty(trackNumber)) {
                try {
                    media.setTrack(Integer.parseInt(duration));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mediaFromFile() track number is not a number: " + duration);
                }
            }

            return media;
        } finally {
            r.release();
        }
    }
}
