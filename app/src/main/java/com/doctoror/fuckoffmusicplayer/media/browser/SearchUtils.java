/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.media.browser;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderArtists;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderGenres;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderRandom;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderTracks;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueUtils;
import com.doctoror.fuckoffmusicplayer.util.ObserverAdapter;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Search utils
 */
public final class SearchUtils {

    private static final String TAG = "SearchUtils";

    @NonNull
    private final Context mContext;

    @Inject
    MediaProvider mediaProvider;

    @Inject
    PlaybackData mPlaybackData;

    @Inject
    QueueProviderArtists artistPlaylistFactory;

    @Inject
    QueueProviderAlbums albumPlaylistFactory;

    @Inject
    QueueProviderGenres genrePlaylistFactory;

    @Inject
    QueueProviderTracks tracksQueueProvider;

    @Inject
    QueueProviderRecentlyScanned recentlyScannedPlaylistFactory;

    @Inject
    QueueProviderRandom randomPlaylistFactory;

    public SearchUtils(@NonNull final Context context) {
        mContext = context;
        DaggerHolder.getInstance(context).mainComponent().inject(this);
    }

    @WorkerThread
    public void onPlayFromMediaId(@NonNull final String mediaId) {
        if (MediaBrowserImpl.MEDIA_ID_RANDOM.equals(mediaId)) {
            playFromQueueSource(randomPlaylistFactory.randomQueue());
        } else if (MediaBrowserImpl.MEDIA_ID_RECENT.equals(mediaId)) {
            playFromQueueSource(recentlyScannedPlaylistFactory.recentlyScannedQueue());
        } else if (mediaId.startsWith(MediaBrowserImpl.MEDIA_ID_PREFIX_ALBUM)) {
            playFromQueueSource(queueSourceFromAlbumId(mediaId));
        } else if (mediaId.startsWith(MediaBrowserImpl.MEDIA_ID_PREFIX_GENRE)) {
            playFromQueueSource(queueSourceFromGenreId(mediaId));
        } else {
            long id = -1;
            try {
                id = Long.parseLong(mediaId);
            } catch (NumberFormatException e) {
                Log.w(TAG, "Media id is not a number", e);
            }
            if (id != -1) {
                onPlayFromMediaId(id);
            }
        }
    }

    @NonNull
    private Observable<List<Media>> queueSourceFromAlbumId(@NonNull final String mediaId) {
        final String albumId = mediaId
                .substring(MediaBrowserImpl.MEDIA_ID_PREFIX_ALBUM.length());
        try {
            return albumPlaylistFactory.fromAlbum(Long.parseLong(albumId));
        } catch (NumberFormatException e) {
            return Observable
                    .error(new NumberFormatException("Album id is not a number " + albumId));
        }
    }

    @NonNull
    private Observable<List<Media>> queueSourceFromGenreId(@NonNull final String mediaId) {
        final String genreId = mediaId
                .substring(MediaBrowserImpl.MEDIA_ID_PREFIX_GENRE.length());
        try {
            return genrePlaylistFactory.fromGenre(Long.parseLong(genreId));
        } catch (NumberFormatException e) {
            return Observable
                    .error(new NumberFormatException("Genre id is not a number " + genreId));
        }
    }

    private void onPlayFromMediaId(final long mediaId) {
        int position = -1;
        List<Media> queue = mPlaybackData.getQueue();
        if (queue != null && !queue.isEmpty()) {
            final int size = queue.size();
            for (int i = 0; i < size; i++) {
                if (queue.get(i).getId() == mediaId) {
                    position = i;
                    break;
                }
            }
        }

        if (queue != null && position != -1) {
            play(mContext, queue, position);
        } else {
            playFromQueueSource(mediaProvider.load(mediaId));
        }
    }

    public void onPlayFromSearch(@Nullable final String query,
            @Nullable final Bundle extras) {
        if (TextUtils.isEmpty(query)) {
            PlaybackServiceControl.playAnything(mContext);
            return;
        }

        queueSourceFromSearch(query, extras)
                .take(1)
                .subscribe(new ObserverAdapter<List<Media>>() {
                    @Override
                    public void onNext(final List<Media> queue) {
                        QueueUtils.play(mContext, mPlaybackData, queue);
                    }

                    @Override
                    public void onError(final Throwable e) {
                        final String message = TextUtils.isEmpty(query)
                                ? mContext.getString(R.string.No_media_found)
                                : mContext.getString(R.string.No_media_found_for_s, query);

                        PlaybackServiceControl.stopWithError(mContext, message);
                    }
                });
    }

    @NonNull
    private Observable<List<Media>> queueSourceFromSearch(@NonNull final String query,
            @Nullable final Bundle extras) {
        boolean isArtistFocus = false;
        boolean isAlbumFocus = false;

        String artist = null;
        String album = null;

        String mediaFocus = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_FOCUS);
        if (TextUtils.equals(mediaFocus, MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
            isArtistFocus = true;
            artist = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ARTIST);
        } else if (TextUtils.equals(mediaFocus, MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
            isAlbumFocus = true;
            album = extras == null ? null : extras.getString(MediaStore.EXTRA_MEDIA_ALBUM);
        }

        Observable<List<Media>> source = null;
        if (isArtistFocus) {
            source = artistPlaylistFactory.fromArtistSearch(TextUtils.isEmpty(artist)
                    ? query : artist);
        } else if (isAlbumFocus) {
            source = albumPlaylistFactory.fromAlbumSearch(TextUtils.isEmpty(album)
                    ? query : album);
        }

        if (source == null) {
            source = tracksQueueProvider.fromTracksSearch(query);
        } else {
            source = source.flatMap(queue -> queue.isEmpty()
                    ? tracksQueueProvider.fromTracksSearch(query)
                    : Observable.just(queue));
        }

        return source;
    }

    private void playFromQueueSource(@NonNull final Observable<List<Media>> source) {
        source.take(1)
                .subscribe(new ObserverAdapter<List<Media>>() {
                    @Override
                    public void onNext(final List<Media> queue) {
                        play(mContext, queue, 0);
                    }

                    @Override
                    public void onError(final Throwable e) {
                        PlaybackServiceControl.stopWithError(mContext,
                                mContext.getString(R.string.No_media_found));
                    }
                });
    }

    private void play(@NonNull final Context context,
            @NonNull final List<Media> queue,
            final int position) {
        if (!queue.isEmpty()) {
            QueueUtils.play(context, mPlaybackData, queue, position);
        } else {
            PlaybackServiceControl
                    .stopWithError(context, context.getString(R.string.No_media_found));
        }
    }

}
