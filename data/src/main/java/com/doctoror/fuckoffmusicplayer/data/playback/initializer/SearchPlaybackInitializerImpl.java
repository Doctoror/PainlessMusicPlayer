/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.data.playback.initializer;

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.SearchPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.provider.QueueFromSearchProvider;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;

import io.reactivex.schedulers.Schedulers;

public final class SearchPlaybackInitializerImpl implements SearchPlaybackInitializer {

    private final CharSequence noMediaFoundText;
    private final String noMediaFoundNonFormattedText;

    private final PlaybackInitializer playbackInitializer;
    private final PlaybackServiceControl playbackServiceControl;
    private final QueueFromSearchProvider queueFromSearchProvider;

    public SearchPlaybackInitializerImpl(
            @NonNull final CharSequence noMediaFoundText,
            @NonNull final String noMediaFoundNonFormattedText,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackServiceControl playbackServiceControl,
            @NonNull final QueueFromSearchProvider queueFromSearchProvider) {
        this.noMediaFoundText = noMediaFoundText;
        this.noMediaFoundNonFormattedText = noMediaFoundNonFormattedText;
        this.playbackInitializer = playbackInitializer;
        this.playbackServiceControl = playbackServiceControl;
        this.queueFromSearchProvider = queueFromSearchProvider;
    }

    @Override
    public void playFromSearch(@Nullable final String query, @Nullable final Bundle extras) {
        if (TextUtils.isEmpty(query)) {
            playbackServiceControl.playAnything();
            return;
        }

        queueFromSearchProvider.queueSourceFromSearch(query, extras)
                .take(1)
                .subscribeOn(Schedulers.io())
                .subscribe(this::setQueueAndPlay, (t) -> onQueueLoadFailed(query));
    }

    private void setQueueAndPlay(@NonNull final List<Media> queue) {
        playbackInitializer.setQueueAndPlay(queue, 0);
    }

    private void onQueueLoadFailed(@NonNull final String query) {
        final CharSequence message = TextUtils.isEmpty(query)
                ? noMediaFoundText
                : String.format(noMediaFoundNonFormattedText, query);

        playbackServiceControl.stopWithError(message);
    }
}
