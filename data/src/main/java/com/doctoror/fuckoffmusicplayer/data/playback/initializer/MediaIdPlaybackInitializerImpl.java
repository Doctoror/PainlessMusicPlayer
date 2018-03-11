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

import com.doctoror.fuckoffmusicplayer.domain.media.MediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.MediaIdPlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.support.annotation.NonNull;

import java.util.List;

import io.reactivex.Observable;

public final class MediaIdPlaybackInitializerImpl implements MediaIdPlaybackInitializer {

    private final CharSequence noMediaFoundMessage;
    private final MediaProvider mediaProvider;

    private final PlaybackInitializer playbackInitializer;
    private final PlaybackData playbackData;
    private final PlaybackServiceControl playbackServiceControl;

    public MediaIdPlaybackInitializerImpl(
            @NonNull final CharSequence noMediaFoundMessage,
            @NonNull final MediaProvider mediaProvider,
            @NonNull final PlaybackInitializer playbackInitializer,
            @NonNull final PlaybackData playbackData,
            @NonNull final PlaybackServiceControl playbackServiceControl) {
        this.noMediaFoundMessage = noMediaFoundMessage;
        this.mediaProvider = mediaProvider;
        this.playbackInitializer = playbackInitializer;
        this.playbackData = playbackData;
        this.playbackServiceControl = playbackServiceControl;
    }

    @Override
    public void playFromMediaId(final long mediaId) {
        int position = -1;
        List<Media> queue = playbackData.getQueue();
        if (queue != null && !queue.isEmpty()) {
            int loopPos = 0;
            for (final Media item : queue) {
                if (item.getId() == mediaId) {
                    position = loopPos;
                    break;
                }
                loopPos++;
            }
        }

        if (queue != null && position != -1) {
            play(queue, position);
        } else {
            playFromQueueSource(mediaProvider.load(mediaId));
        }
    }

    private void playFromQueueSource(@NonNull final Observable<List<Media>> source) {
        source.take(1).subscribe(
                q -> play(q, 0),
                t -> playbackServiceControl.stopWithError(noMediaFoundMessage));
    }

    private void play(@NonNull final List<Media> queue, final int position) {
        if (!queue.isEmpty()) {
            playbackInitializer.setQueueAndPlay(queue, position);
        } else {
            playbackServiceControl.stopWithError(noMediaFoundMessage);
        }
    }
}
