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
import com.doctoror.fuckoffmusicplayer.data.util.CollectionUtils;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;

import javax.inject.Inject;

/**
 * {@link MediaSessionCompat} holder
 */
public final class MediaSessionHolder {

    private static final String TAG = "MediaSessionHolder";

    @SuppressLint("StaticFieldLeak") // Is not a leak for application context
    private static MediaSessionHolder instance;

    @NonNull
    public static MediaSessionHolder getInstance(@NonNull final Context context) {
        if (instance == null) {
            synchronized (MediaSessionHolder.class) {
                if (instance == null) {
                    instance = new MediaSessionHolder(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @NonNull
    private final Context context;

    private volatile int openCount;

    private MediaSessionCompat mediaSession;

    @Inject
    PlaybackData playbackData;

    @Inject
    PlaybackReporterFactory playbackReporterFactory;

    @Inject
    PlaybackServiceControl mPlaybackServiceControl;

    private MediaSessionHolder(@NonNull final Context context) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        this.context = context;
    }

    public void openSession() {
        synchronized (MediaSessionHolder.class) {
            openCount++;
            if (openCount == 1) {
                doOpenSession();
            }
        }
    }

    public void closeSession() {
        synchronized (MediaSessionHolder.class) {
            openCount--;
            if (openCount == 0) {
                doCloseSession();
            }
        }
    }

    private void doOpenSession() {
        final ComponentName mediaButtonReceiver = new ComponentName(context,
                MediaButtonReceiver.class);

        final PendingIntent broadcastIntent = PendingIntent
                .getBroadcast(context, 1, new Intent(context, MediaButtonReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        final MediaSessionCompat mediaSession = new MediaSessionCompat(context, TAG,
                mediaButtonReceiver, broadcastIntent);

        mediaSession.setCallback(new MediaSessionCallback(context, mPlaybackServiceControl));
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
        mediaSession.setSessionActivity(PendingIntent.getActivity(context, 1,
                new Intent(context, NowPlayingActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        this.mediaSession = mediaSession;

        Handlers.runOnIoThread(() -> reportMediaAndState(mediaSession));
    }

    private void doCloseSession() {
        if (mediaSession != null) {
            mediaSession.setActive(false);
            mediaSession.release();
            mediaSession = null;
        }
    }

    @Nullable
    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    @WorkerThread
    private void reportMediaAndState(@NonNull final MediaSessionCompat mediaSession) {
        final PlaybackReporter playbackReporter = playbackReporterFactory
                .newMediaSessionReporter(mediaSession);

        final int position = playbackData.getQueuePosition();
        final Media current = CollectionUtils.getItemSafe(playbackData.getQueue(), position);
        if (current != null) {
            playbackReporter.reportTrackChanged(current, position);
        }
        playbackReporter.reportPlaybackStateChanged(playbackData.getPlaybackState(), null);
    }
}
