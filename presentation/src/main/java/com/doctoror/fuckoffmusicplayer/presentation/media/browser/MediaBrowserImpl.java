/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.presentation.media.browser;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.media.browser.MediaBrowserConstants;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.domain.genres.GenresProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playlist.RecentActivityManager;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Media browser implementation
 */
public final class MediaBrowserImpl {

    private static final String MEDIA_ID_ROOT = "ROOT";

    private static final String MEDIA_ID_CURENT_QUEUE = "CURRENT_QUEUE";
    private static final String MEDIA_ID_RECENT_ALBUMS = "RECENT_ALBUMS";
    private static final String MEDIA_ID_GENRES = "GENRES";

    @NonNull
    private final Context mContext;

    @NonNull
    private final Set<String> mMediaBrowserCallerPackageNames = new HashSet<>();

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Inject
    GenresProvider mGenresProvider;

    @Inject
    RecentActivityManager mRecentActivityManager;

    @Inject
    PlaybackData mPlaybackData;

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
                final List<Media> queue = mPlaybackData.getQueue();
                if (queue != null && !queue.isEmpty()) {
                    mediaItems.add(createBrowsableMediaItemCurrentQueue());
                }
                final long[] recentlyPlayedAlbums = mRecentActivityManager.getRecentlyPlayedAlbums();
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
                final List<Media> queue = mPlaybackData.getQueue();
                if (queue != null && !queue.isEmpty()) {
                    mediaItems = new ArrayList<>(queue.size());
                    for (final Media item : queue) {
                        mediaItems.add(createMediaItemMedia(item));
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
                .setMediaId(MediaBrowserConstants.MEDIA_ID_RANDOM)
                .setTitle(mContext.getText(R.string.Random_playlist))
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    @NonNull
    private MediaItem createMediaItemRecent() {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaBrowserConstants.MEDIA_ID_RECENT)
                .setTitle(mContext.getText(R.string.Recently_added))
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
        mGenresProvider.load()
                .take(1)
                .onErrorReturn(t -> null)
                .map(this::mediaItemsFromGenresCursor)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::sendResult);
    }

    @Nullable
    private List<MediaItem> mediaItemsFromGenresCursor(@Nullable final Cursor c) {
        List<MediaItem> mediaItems = null;
        if (c != null) {
            mediaItems = new ArrayList<>(c.getCount());
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                mediaItems.add(createMediaItemGenre(c));
            }
            c.close();
        }
        return mediaItems;
    }

    private void loadChildrenRecentAlbums(@NonNull final Result<List<MediaItem>> result) {
        result.detach();
        mAlbumsProvider.loadRecentlyPlayedAlbums().take(1)
                .onErrorReturn(t -> null)
                .map(this::recentAlbumsFromCursor)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result::sendResult);
    }

    @Nullable
    private List<MediaItem> recentAlbumsFromCursor(@Nullable final Cursor c) {
        List<MediaItem> mediaItems = null;
        if (c != null) {
            mediaItems = new ArrayList<>(c.getCount());
            for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                mediaItems.add(createMediaItemAlbum(c));
            }
            c.close();
        }
        return mediaItems;
    }

    @NonNull
    private MediaItem createMediaItemGenre(@NonNull final Cursor c) {
        final MediaDescriptionCompat description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaBrowserConstants.MEDIA_ID_PREFIX_GENRE.concat(c.getString(GenresProvider.COLUMN_ID)))
                .setTitle(c.getString(GenresProvider.COLUMN_NAME))
                .build();
        return new MediaItem(description, MediaItem.FLAG_PLAYABLE);
    }

    @NonNull
    private MediaItem createMediaItemAlbum(@NonNull final Cursor c) {
        final MediaDescriptionCompat.Builder description = new MediaDescriptionCompat.Builder()
                .setMediaId(MediaBrowserConstants.MEDIA_ID_PREFIX_ALBUM.concat(c.getString(AlbumsProvider.COLUMN_ID)))
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
