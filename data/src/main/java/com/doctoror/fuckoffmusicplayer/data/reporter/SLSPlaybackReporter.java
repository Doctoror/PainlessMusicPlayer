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
package com.doctoror.fuckoffmusicplayer.data.reporter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.data.util.Objects;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;

/**
 * Simple Last.fm Scrobbler playback reporter
 * https://github.com/tgwizard/sls/blob/master/Developer's%20API.md
 */
public final class SLSPlaybackReporter implements PlaybackReporter {

    private static final String ACTION = "com.adam.aslfms.notify.playstatechanged";

    private static final int STATE_START = 0;
    private static final int STATE_RESUME = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_COMPLETE = 3;

    private static final String APP_NAME = "app-name";
    private static final String APP_PACKAGE = "app-package";

    private static final String STATE = "state";
    private static final String ARTIST = "artist";
    private static final String ALBUM = "album";
    private static final String TRACK = "track";
    private static final String DURATION = "duration";

    private final Context context;
    private final Settings settings;

    private Media media;

    @NonNull
    private PlaybackState state;

    SLSPlaybackReporter(
            @NonNull final Context context,
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final PlaybackData playbackData,
            @NonNull final Settings settings) {
        this.context = context;
        this.settings = settings;
        media = currentMediaProvider.getCurrentMedia();
        state = playbackData.getPlaybackState();
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media) {
        if (Objects.notEqual(this.media, media)) {
            this.media = media;
            if (state != PlaybackState.STATE_IDLE) {
                report(media, state, state);
            }
        }
    }

    @Override
    public void reportPlaybackStateChanged(
            @NonNull final PlaybackState state,
            @Nullable final CharSequence errorMessage) {
        if (this.state != state) {
            report(media, this.state, state);
            this.state = state;
        }
    }

    private void report(@Nullable final Media media,
                        @NonNull final PlaybackState prevState,
                        @NonNull final PlaybackState state) {
        if (settings.isScrobbleEnabled() && media != null) {
            final Intent intent = new Intent(ACTION);
            intent.putExtra(APP_NAME, "Painless Music Player");
            intent.putExtra(APP_PACKAGE, context.getPackageName());

            intent.putExtra(ARTIST, media.getArtist());
            intent.putExtra(TRACK, media.getTitle());
            intent.putExtra(DURATION, (int) (media.getDuration() / 1000L));
            intent.putExtra(ALBUM, media.getAlbum());

            intent.putExtra(STATE, toSlsState(prevState, state));
            context.sendBroadcast(intent);
        }
    }

    private static int toSlsState(@NonNull final PlaybackState prevState,
                                  @NonNull final PlaybackState playbackState) {
        switch (playbackState) {
            case STATE_IDLE:
                return STATE_PAUSE;

            case STATE_LOADING:
                // Loading after playing means playback completed for prev track
                return prevState == PlaybackState.STATE_PLAYING
                        ? STATE_COMPLETE
                        : STATE_PAUSE;

            case STATE_PLAYING:
                // Playing after loading means new track is started
                return prevState == PlaybackState.STATE_LOADING
                        ? STATE_START
                        : STATE_RESUME;

            case STATE_PAUSED:
                return STATE_PAUSE;

            case STATE_ERROR:
                return STATE_PAUSE;

            default:
                return STATE_PAUSE;
        }
    }
}
