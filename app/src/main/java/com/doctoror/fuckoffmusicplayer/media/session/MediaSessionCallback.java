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

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.concurrent.Handlers;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.QueueFromSearchProvider;
import com.doctoror.fuckoffmusicplayer.media.browser.SearchUtils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import java.util.List;

import io.reactivex.schedulers.Schedulers;

/**
 * {@link MediaSessionCompat.Callback} implementation
 */
final class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final Resources resources;
    private final SearchUtils searchUtils;
    private final PlaybackServiceControl playbackServiceControl;
    private final QueueFromSearchProvider queueFromSearchProvider;
    private final PlaybackInitializer playbackInitializer;

    MediaSessionCallback(
            @NonNull final Context context,
            @NonNull final PlaybackServiceControl playbackServiceControl,
            @NonNull final QueueFromSearchProvider queueFromSearchProvider,
            @NonNull final PlaybackInitializer playbackInitializer) {
        this.resources = context.getResources();
        searchUtils = new SearchUtils(context);
        this.playbackServiceControl = playbackServiceControl;
        this.queueFromSearchProvider = queueFromSearchProvider;
        this.playbackInitializer = playbackInitializer;
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
    public void onPlayFromSearch(final String query, final Bundle extras) {
        queueFromSearchProvider.queueSourceFromSearch(query, extras)
                .subscribeOn(Schedulers.io())
                .subscribe(this::onQueueLoaded, (t) -> onQueueLoadFailed(query));
    }

    @Override
    public void onPlayFromMediaId(final String mediaId, final Bundle extras) {
        Handlers.runOnIoThread(() -> searchUtils.onPlayFromMediaId(mediaId));
    }

    private void onQueueLoaded(@NonNull final List<Media> queue) {
        playbackInitializer.setQueueAndPlay(queue, 0);
    }

    private void onQueueLoadFailed(@NonNull final String query) {
        final CharSequence message = TextUtils.isEmpty(query)
                ? resources.getText(R.string.No_media_found)
                : resources.getString(R.string.No_media_found_for_s, query);

        playbackServiceControl.stopWithError(message);
    }
}
