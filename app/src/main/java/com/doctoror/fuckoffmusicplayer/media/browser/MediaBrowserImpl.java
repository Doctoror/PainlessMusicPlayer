package com.doctoror.fuckoffmusicplayer.media.browser;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsQuery;
import com.doctoror.fuckoffmusicplayer.library.genres.GenresQuery;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;
import com.doctoror.fuckoffmusicplayer.util.SelectionUtils;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat.BrowserRoot;
import android.support.v4.media.MediaBrowserServiceCompat.Result;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Media browser implementation
 */
final class MediaBrowserImpl {

    private static final String MEDIA_ID_ROOT = "ROOT";

    private static final String MEDIA_ID_CURENT_QUEUE = "CURRENT_QUEUE";
    private static final String MEDIA_ID_RECENT_ALBUMS = "RECENT_ALBUMS";
    private static final String MEDIA_ID_GENRES = "GENRES";

    static final String MEDIA_ID_PREFIX_GENRE = "GENRE/";
    static final String MEDIA_ID_PREFIX_ALBUM = "ALBUM/";

    static final String MEDIA_ID_RANDOM = "RANDOM";
    static final String MEDIA_ID_RECENT = "RECENT";

    @NonNull
    private final Context mContext;

    MediaBrowserImpl(@NonNull final Context context) {
        mContext = context;
    }

    BrowserRoot getRoot() {
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    void onLoadChildren(@NonNull final String parentId,
            @NonNull final Result<List<MediaItem>> result) {
        switch (parentId) {
            case MEDIA_ID_ROOT: {
                final List<MediaItem> mediaItems = new ArrayList<>(5);
                final List<Media> playlist = CurrentPlaylist.getInstance(mContext).getPlaylist();
                if (playlist != null && !playlist.isEmpty()) {
                    mediaItems.add(createBrowsableMediaItemCurrentQueue());
                }
                final long[] recentlyPlayedAlbums = RecentPlaylistsManager.getInstance(mContext)
                        .getRecentAlbums();
                if (recentlyPlayedAlbums.length != 0) {
                    mediaItems.add(createBrowsableMediaItemRecentAlbums());
                }
                mediaItems.add(createBrowsableMediaItemGenres());
                mediaItems.add(createMediaItemRandom());
                mediaItems.add(createMediaItemRecent());
                result.sendResult(mediaItems);
                break;
            }

            case MEDIA_ID_CURENT_QUEUE: {
                List<MediaItem> mediaItems = null;
                final List<Media> playlist = CurrentPlaylist.getInstance(mContext).getPlaylist();
                if (playlist != null && !playlist.isEmpty()) {
                    final int size = playlist.size();
                    mediaItems = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        mediaItems.add(createMediaItemMedia(playlist.get(i)));
                    }
                }

                if (mediaItems == null) {
                    mediaItems = new ArrayList<>();
                }

                result.sendResult(mediaItems);
                break;
            }

            case MEDIA_ID_RECENT_ALBUMS:
                loadChildrenRecentAlbums(result);
                break;

            case MEDIA_ID_GENRES:
                loadChildrenGenres(result);
                break;
        }

    }


    @NonNull
    private MediaItem createBrowsableMediaItemCurrentQueue() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_CURENT_QUEUE)
                .setTitle(mContext.getText(R.string.Now_Playing))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemRecentAlbums() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_RECENT_ALBUMS)
                .setTitle(mContext.getText(R.string.Recently_played_albums))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createBrowsableMediaItemGenres() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_GENRES)
                .setTitle(mContext.getText(R.string.Genres))
                .build();
        return new MediaItem(description, MediaItem.FLAG_BROWSABLE);
    }

    @NonNull
    private MediaItem createMediaItemRandom() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_RANDOM)
                .setTitle(mContext.getText(R.string.Random_playlist))
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    @NonNull
    private MediaItem createMediaItemRecent() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_RECENT)
                .setTitle(mContext.getText(R.string.Recently_scanned))
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    @NonNull
    private MediaItem createMediaItemMedia(@NonNull final Media media) {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(Long.toString(media.getId()))
                .setTitle(media.getTitle())
                .setSubtitle(media.getArtist())
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    private void loadChildrenGenres(@NonNull final Result<List<MediaItem>> result) {
        result.detach();

        final RxCursorLoader.Query query = GenresQuery.newParams(null);
        RxCursorLoader.single(mContext.getContentResolver(), query)
                .subscribe((c) -> {
                    if (c != null) {
                        final List<MediaItem> mediaItems = new ArrayList<>(c.getCount());
                        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                            mediaItems.add(createMediaItemGenre(c));
                        }
                        result.sendResult(mediaItems);
                        c.close();
                    }
                });
    }

    private void loadChildrenRecentAlbums(@NonNull final Result<List<MediaItem>> result) {
        result.detach();

        final long[] recentlyPlayedAlbums = RecentPlaylistsManager.getInstance(mContext)
                .getRecentAlbums();
        final RxCursorLoader.Query.Builder query = AlbumsQuery.newParamsBuilder();
        query
                .setSelection(SelectionUtils.inSelectionLong(MediaStore.Audio.Albums._ID,
                        recentlyPlayedAlbums))

                .setSortOrder(SelectionUtils.orderByLongField(MediaStore.Audio.Albums._ID,
                        recentlyPlayedAlbums));

        RxCursorLoader.single(mContext.getContentResolver(), query.create())
                .subscribe((c) -> {
                    if (c != null) {
                        final List<MediaItem> mediaItems = new ArrayList<>(c.getCount());
                        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                            mediaItems.add(createMediaItemAlbum(c));
                        }
                        result.sendResult(mediaItems);
                        c.close();
                    }
                });
    }

    @NonNull
    private MediaItem createMediaItemGenre(@NonNull final Cursor c) {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_PREFIX_GENRE + c.getString(GenresQuery.COLUMN_ID))
                .setTitle(c.getString(GenresQuery.COLUMN_NAME))
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    @NonNull
    private MediaItem createMediaItemAlbum(@NonNull final Cursor c) {
        final MediaDescriptionCompat.Builder description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_PREFIX_ALBUM + c.getString(AlbumsQuery.COLUMN_ID))
                .setTitle(c.getString(AlbumsQuery.COLUMN_ALBUM));
        final String art = c.getString(AlbumsQuery.COLUMN_ALBUM_ART);
        if (!TextUtils.isEmpty(art)) {
            description.setIconUri(Uri.parse(new File(art).toURI().toString()));
        }
        return new MediaItem(description.build(), MediaItem.FLAG_PLAYABLE);
    }
}
