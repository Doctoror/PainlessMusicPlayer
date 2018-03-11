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
package com.doctoror.fuckoffmusicplayer.media.session;

import com.doctoror.fuckoffmusicplayer.data.concurrent.Handlers;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.QueueFromSearchProvider;
import com.doctoror.fuckoffmusicplayer.media.browser.SearchUtils;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

/**
 * {@link MediaSessionCompat.Callback} implementation
 */
final class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final SearchUtils mSearchUtils;
    
    private final PlaybackServiceControl mPlaybackServiceControl;

    // TODO use it
    private final QueueFromSearchProvider mQueueFromSearchProvider;

    MediaSessionCallback(
            @NonNull final Context context,
            @NonNull final PlaybackServiceControl playbackServiceControl,
            @NonNull final QueueFromSearchProvider queueFromSearchProvider) {
        mSearchUtils = new SearchUtils(context);
        mPlaybackServiceControl = playbackServiceControl;
        mQueueFromSearchProvider = queueFromSearchProvider;
    }

    @Override
    public void onPlay() {
        mPlaybackServiceControl.play();
    }

    @Override
    public void onStop() {
        mPlaybackServiceControl.stop();
    }

    @Override
    public void onPause() {
        mPlaybackServiceControl.pause();
    }

    @Override
    public void onSkipToPrevious() {
        mPlaybackServiceControl.prev();
    }

    @Override
    public void onSkipToNext() {
        mPlaybackServiceControl.next();
    }

    @Override
    public void onPlayFromSearch(final String query, final Bundle extras) {
        Handlers.runOnIoThread(() -> mSearchUtils.onPlayFromSearch(query, extras));
    }

    @Override
    public void onPlayFromMediaId(final String mediaId, final Bundle extras) {
        Handlers.runOnIoThread(() -> mSearchUtils.onPlayFromMediaId(mediaId));
    }
}
