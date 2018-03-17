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
package com.doctoror.fuckoffmusicplayer.data.media.session;

import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.MediaIdPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.SearchPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.MediaBrowserQueueProvider;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import io.reactivex.schedulers.Schedulers;

/**
 * {@link MediaSessionCompat.Callback} implementation
 */
public final class MediaSessionCallback extends MediaSessionCompat.Callback {

    private static final String TAG = "MediaSessionCallback";

    private final MediaBrowserQueueProvider mediaBrowserQueueProvider;
    private final MediaIdPlaybackInitializer mediaIdPlaybackInitializer;

    private final PlaybackInitializer playbackInitializer;
    private final PlaybackServiceControl playbackServiceControl;

    private final SearchPlaybackInitializer searchPlaybackInitializer;

    public MediaSessionCallback(
            @NonNull final MediaBrowserQueueProvider mediaBrowserQueueProvider,
            @NonNull final MediaIdPlaybackInitializer mediaIdPlaybackInitializer,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackServiceControl playbackServiceControl,
            @NonNull final SearchPlaybackInitializer searchPlaybackInitializer) {
        this.mediaBrowserQueueProvider = mediaBrowserQueueProvider;
        this.mediaIdPlaybackInitializer = mediaIdPlaybackInitializer;
        this.playbackInitializer = playbackInitializer;
        this.playbackServiceControl = playbackServiceControl;
        this.searchPlaybackInitializer = searchPlaybackInitializer;
    }

    @Override
    public void onPlay() {
        playbackServiceControl.play();
    }

    @Override
    public void onStop() {
        playbackServiceControl.stop();
    }

    @Override
    public void onPause() {
        playbackServiceControl.pause();
    }

    @Override
    public void onSkipToPrevious() {
        playbackServiceControl.prev();
    }

    @Override
    public void onSkipToNext() {
        playbackServiceControl.next();
    }

    @Override
    public void onPlayFromSearch(@Nullable final String query, @Nullable final Bundle extras) {
        searchPlaybackInitializer.playFromSearch(query, extras);
    }

    @Override
    public void onPlayFromMediaId(@Nullable final String mediaId, @Nullable final Bundle extras) {
        if (TextUtils.isEmpty(mediaId)) {
            Log.w(TAG, "Media ID is null or empty");
            return;
        }

        mediaBrowserQueueProvider.fromMediaBrowserId(mediaId)
                .take(1)
                .subscribeOn(Schedulers.io())
                .subscribe((queue) -> {
                    if (queue.isEmpty()) {
                        // Normal case when media id may not resemble album or genre.
                        mediaIdPlaybackInitializer.playFromMediaId(mediaIdToLong(mediaId));
                    } else {
                        playbackInitializer.setQueueAndPlay(queue, 0);
                    }
                }, (t) -> Log.w(TAG, "Failed to load queue for media id = " + mediaId));
    }

    private long mediaIdToLong(@NonNull final String mediaId) {
        try {
            return Long.parseLong(mediaId);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Media id is not a number, is " + mediaId);
        }
    }
}
