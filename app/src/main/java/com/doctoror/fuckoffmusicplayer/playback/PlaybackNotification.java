/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.playback;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;

import java.util.concurrent.ExecutionException;

/**
 * "Now Playing" notification
 */
final class PlaybackNotification {

    private static final String TAG = "PlaybackNotification";

    private PlaybackNotification() {

    }

    @NonNull
    public static Notification create(@NonNull final Context context,
            @NonNull final RequestManager glide,
            @NonNull final Media media,
            @PlaybackState.State final int state,
            @NonNull final MediaSessionCompat mediaSession) {
        Bitmap art = null;
        final String artLocation = media.getAlbumArt();
        if (!TextUtils.isEmpty(artLocation)) {
            final int dp128 = (int) (context.getResources().getDisplayMetrics().density * 128);
            try {
                art = glide.load(artLocation)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(dp128, dp128)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, e);
            }
        }
        if (art == null) {
            art = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.album_art_placeholder);
        }

        final Intent contentIntent = Henson.with(context)
                .gotoNowPlayingActivity()
                .hasCoverTransition(false)
                .hasListViewTransition(false)
                .build();

        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final NotificationCompat.Style style = new NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(1, 2);

        final android.support.v4.app.NotificationCompat.Builder b
                = new NotificationCompat.Builder(context)
                .setStyle(style)
                .setShowWhen(false)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(media.getTitle())
                .setContentText(media.getArtist())
                .setContentIntent(PendingIntent.getActivity(context, 4, contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(state == PlaybackState.STATE_PLAYING ? R.drawable.ic_stat_play
                        : R.drawable.ic_stat_pause)
                .setLargeIcon(art);

        addAction1(context, b);
        addAction2(context, b, state);
        addAction3(context, b);

        return b.build();
    }

    private static void addAction1(@NonNull final Context context,
            @NonNull final android.support.v4.app.NotificationCompat.Builder b) {
        final int direction = context.getResources().getInteger(R.integer.layoutDirection);
        switch (direction) {
            case View.LAYOUT_DIRECTION_RTL:
                addActionNext(context, b);
                break;

            case View.LAYOUT_DIRECTION_LTR:
            default:
                addActionPrev(context, b);
                break;
        }
    }

    private static void addAction2(@NonNull final Context context,
            @NonNull final android.support.v4.app.NotificationCompat.Builder b,
            @PlaybackState.State final int state) {
        final PendingIntent middleActionIntent = PendingIntent.getService(context, 3,
                PlaybackServiceIntentFactory.intentPlayPause(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        if (state == PlaybackState.STATE_PLAYING) {
            b.addAction(R.drawable.ic_pause_white_24dp, context.getText(R.string.Pause),
                    middleActionIntent);
        } else {
            b.addAction(R.drawable.ic_play_arrow_white_24dp, context.getText(R.string.Play),
                    middleActionIntent);
        }
    }

    private static void addAction3(@NonNull final Context context,
            @NonNull final android.support.v4.app.NotificationCompat.Builder b) {
        final int direction = context.getResources().getInteger(R.integer.layoutDirection);
        switch (direction) {
            case View.LAYOUT_DIRECTION_RTL:
                addActionPrev(context, b);
                break;

            case View.LAYOUT_DIRECTION_LTR:
            default:
                addActionNext(context, b);
                break;
        }
    }

    private static void addActionPrev(@NonNull final Context context,
            @NonNull final android.support.v4.app.NotificationCompat.Builder b) {
        final PendingIntent prevIntent = PendingIntent.getService(context, 1,
                PlaybackServiceIntentFactory.intentPrev(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        b.addAction(R.drawable.ic_fast_rewind_white_24dp,
                context.getText(R.string.Previous), prevIntent);
    }

    private static void addActionNext(@NonNull final Context context,
            @NonNull final android.support.v4.app.NotificationCompat.Builder b) {
        final PendingIntent nextIntent = PendingIntent.getService(context, 2,
                PlaybackServiceIntentFactory.intentNext(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        b.addAction(R.drawable.ic_fast_forward_white_24dp, context.getText(R.string.Next),
                nextIntent);
    }

}
