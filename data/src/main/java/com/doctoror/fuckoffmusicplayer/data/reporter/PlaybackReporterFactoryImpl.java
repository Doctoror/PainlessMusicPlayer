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
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.data.albums.AlbumArtFetcherImpl;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;

/**
 * {@link PlaybackReporter} factory implementation.
 */
public final class PlaybackReporterFactoryImpl implements PlaybackReporterFactory {

    private final Context context;
    private final AlbumArtFetcher albumArtFetcher;
    private final AlbumThumbHolder albumThumbHolder;
    private final CurrentMediaProvider currentMediaProvider;
    private final PlaybackData playbackData;
    private final Settings settings;

    public PlaybackReporterFactoryImpl(
            @NonNull final Context context,
            @NonNull final AlbumArtFetcher albumArtFetcher,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final Settings settings,
            @NonNull final PlaybackData playbackData) {
        this.context = context;
        this.albumArtFetcher = albumArtFetcher;
        this.albumThumbHolder = albumThumbHolder;
        this.currentMediaProvider = currentMediaProvider;
        this.settings = settings;
        this.playbackData = playbackData;
    }

    @NonNull
    @Override
    public PlaybackReporter newUniversalReporter(
            @NonNull final MediaSessionCompat mediaSession) {
        return new PlaybackReporterSet(
                new MediaSessionPlaybackReporter(
                        context, albumArtFetcher, albumThumbHolder, mediaSession),
                new AppWidgetPlaybackStateReporter(context),
                new SLSPlaybackReporter(context, currentMediaProvider, playbackData, settings),
                new LastFmPlaybackReporter(context, currentMediaProvider, playbackData, settings));
    }

    @NonNull
    @Override
    public PlaybackReporter newMediaSessionReporter(
            @NonNull final MediaSessionCompat mediaSession) {
        return new MediaSessionPlaybackReporter(
                context, albumArtFetcher, albumThumbHolder, mediaSession);
    }
}
