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

import com.doctoror.fuckoffmusicplayer.Handlers;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.util.CollectionUtils;

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

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * {@link MediaSessionCompat} holder
 */
public final class MediaSessionHolder {

    private static final String TAG = "MediaSessionHolder";

    @SuppressLint("StaticFieldLeak") // Is not a leak for application context
    private static MediaSessionHolder sInstance;

    @NonNull
    public static MediaSessionHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (MediaSessionHolder.class) {
                if (sInstance == null) {
                    sInstance = new MediaSessionHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final Context mContext;

    private volatile int mOpenCount;

    private MediaSessionCompat mMediaSession;

    @Inject
    PlaybackData mPlaybackData;

    private MediaSessionHolder(@NonNull final Context context) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        mContext = context;
    }

    public void openSession() {
        synchronized (MediaSessionHolder.class) {
            mOpenCount++;
            if (mOpenCount == 1) {
                doOpenSession();
            }
        }
    }

    public void closeSession() {
        synchronized (MediaSessionHolder.class) {
            mOpenCount--;
            if (mOpenCount == 0) {
                doCloseSession();
            }
        }
    }

    private void doOpenSession() {
        final ComponentName mediaButtonReceiver = new ComponentName(mContext,
                MediaButtonReceiver.class);

        final PendingIntent broadcastIntent = PendingIntent
                .getBroadcast(mContext, 1, new Intent(mContext, MediaButtonReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        final MediaSessionCompat mediaSession = new MediaSessionCompat(mContext, TAG,
                mediaButtonReceiver, broadcastIntent);

        mediaSession.setCallback(new MediaSessionCallback(mContext));
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);
        mediaSession.setSessionActivity(PendingIntent.getActivity(mContext, 1,
                new Intent(mContext, NowPlayingActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));

        mMediaSession = mediaSession;

        Handlers.runOnIoThread(() -> reportMediaAndState(mediaSession));
    }

    private void doCloseSession() {
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
    }

    @Nullable
    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }

    @WorkerThread
    private void reportMediaAndState(@NonNull final MediaSessionCompat mediaSession) {
        final PlaybackReporter playbackReporter = PlaybackReporterFactory
                .newMediaSessionReporter(mContext, mediaSession);

        final int position = mPlaybackData.getQueuePosition();
        final Media current = CollectionUtils.getItemSafe(mPlaybackData.getQueue(), position);
        if (current != null) {
            playbackReporter.reportTrackChanged(current, position);
        }
        playbackReporter.reportPlaybackStateChanged(PlaybackService.getLastKnownState(), null);
    }
}
