package com.doctoror.fuckoffmusicplayer.media.browser;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.genres.GenresProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.RecentPlaylistsManager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat.BrowserRoot;
import android.support.v4.media.MediaBrowserServiceCompat.Result;
import android.support.v4.media.MediaDescriptionCompat;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

/**
 * Media browser implementation
 */
public final class MediaBrowserImpl {

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

    @NonNull
    private final Set<String> mMediaBrowserCallerPackageNames = new HashSet<>();

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Inject
    GenresProvider mGenresProvider;

    @Inject
    RecentPlaylistsManager mRecentPlaylistsManager;

    MediaBrowserImpl(@NonNull final Context context) {
        mContext = context;
        DaggerHolder.getInstance(context).mainComponent().inject(this);
    }

    BrowserRoot getRoot(@NonNull final String clientPackageName) {
        mMediaBrowserCallerPackageNames.add(clientPackageName);
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
                final long[] recentlyPlayedAlbums = mRecentPlaylistsManager.getRecentAlbums();
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

        mGenresProvider.loadOnce().subscribe((c) -> {
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
        mAlbumsProvider.loadRecentlyPlayedAlbumsOnce().subscribe((c) -> {
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
                .setMediaId(MEDIA_ID_PREFIX_GENRE + c.getString(GenresProvider.COLUMN_ID))
                .setTitle(c.getString(GenresProvider.COLUMN_NAME))
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    @NonNull
    private MediaItem createMediaItemAlbum(@NonNull final Cursor c) {
        final MediaDescriptionCompat.Builder description = new MediaDescriptionCompat.Builder()
                .setMediaId(MEDIA_ID_PREFIX_ALBUM + c.getString(AlbumsProvider.COLUMN_ID))
                .setTitle(c.getString(AlbumsProvider.COLUMN_ALBUM));
        final String art = c.getString(AlbumsProvider.COLUMN_ALBUM_ART);
        if (!TextUtils.isEmpty(art)) {
            final Uri uri = FileProvider.getUriForFile(mContext,
                    mContext.getPackageName().concat(".provider.album_thumbs"), new File(art));
            for (final String p : mMediaBrowserCallerPackageNames) {
                mContext.grantUriPermission(p, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            description.setIconUri(uri);
        }
        return new MediaItem(description.build(), MediaItem.FLAG_PLAYABLE);
    }
}
