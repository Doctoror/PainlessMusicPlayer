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
package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.settings.Settings;
import com.doctoror.fuckoffmusicplayer.util.Objects;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Simple Last.fm Scrobbler playback reporter
 * https://github.com/tgwizard/sls/blob/master/Developer's%20API.md
 */
final class SLSPlaybackReporter implements PlaybackReporter {

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

    @NonNull
    private final Context mContext;

    @NonNull
    private final Settings mSettings;

    private Media mMedia;

    @PlaybackState.State
    private int mState;

    SLSPlaybackReporter(@NonNull final Context context,
            @Nullable final Media currentMedia) {
        mContext = context;
        mSettings = Settings.getInstance(context);
        mMedia = currentMedia;
        mState = PlaybackService.getLastKnownState();
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media, final int positionInQueue) {
        if (!Objects.equals(mMedia, media)) {
            mMedia = media;
            if (mState != PlaybackState.STATE_IDLE) {
                report(media, mState, mState);
            }
        }
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        if (mState != state) {
            report(mMedia, mState, state);
            mState = state;
        }
    }

    private void report(@Nullable final Media media,
            @PlaybackState.State final int prevState,
            @PlaybackState.State final int state) {
        if (mSettings.isScrobbleEnabled() && media != null) {
            final Intent intent = new Intent(ACTION);
            intent.putExtra(APP_NAME, mContext.getString(R.string.app_name));
            intent.putExtra(APP_PACKAGE, mContext.getPackageName());

            intent.putExtra(ARTIST, media.getArtist());
            intent.putExtra(TRACK, media.getTitle());
            intent.putExtra(DURATION, (int) (media.getDuration() / 1000L));
            intent.putExtra(ALBUM, media.getAlbum());

            intent.putExtra(STATE, toSlsState(prevState, state));
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void reportPositionChanged(final long mediaId, final long position) {
        // Not supported
    }

    @Override
    public void reportQueueChanged(@Nullable final List<Media> queue) {
        // Not supported
    }

    @Override
    public void onDestroy() {
        // Don't care
    }

    private static int toSlsState(@PlaybackState.State final int prevState,
            @PlaybackState.State final int playbackState) {
        switch (playbackState) {
            case PlaybackState.STATE_IDLE:
                return STATE_PAUSE;

            case PlaybackState.STATE_LOADING:
                // Loading after playing means playback completed for prev track
                return prevState == PlaybackState.STATE_PLAYING
                        ? STATE_COMPLETE
                        : STATE_PAUSE;

            case PlaybackState.STATE_PLAYING:
                // Playing after loading means new track is started
                return prevState == PlaybackState.STATE_LOADING
                        ? STATE_START
                        : STATE_RESUME;

            case PlaybackState.STATE_PAUSED:
                return STATE_PAUSE;

            case PlaybackState.STATE_ERROR:
                return STATE_PAUSE;

            default:
                return STATE_PAUSE;
        }
    }
}
