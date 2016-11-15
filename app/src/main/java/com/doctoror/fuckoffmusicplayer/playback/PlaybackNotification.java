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
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.Log;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;

import java.util.concurrent.ExecutionException;

/**
 * Created by Yaroslav Mytkalyk on 22.10.16.
 */

final class PlaybackNotification {

    private static final String TAG = "PlaybackNotification";

    private PlaybackNotification() {

    }

    @NonNull
    public static Notification create(@NonNull final Context context,
            @NonNull final RequestManager glide,
            @NonNull final Media media,
            @PlaybackService.State final int state,
            @NonNull final MediaSessionCompat mediaSession) {
        final PendingIntent prevIntent = PendingIntent.getService(context, 1,
                PlaybackService.prevIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        final PendingIntent nextIntent = PendingIntent.getService(context, 2,
                PlaybackService.nextIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        final PendingIntent middleActionIntent = PendingIntent.getService(context, 3,
                state == PlaybackService.STATE_PLAYING
                        ? PlaybackService.pauseIntent(context)
                        : PlaybackService.playIntent(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

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

        final Intent contentIntent = new Intent(context, NowPlayingActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final NotificationCompat.Style style = new NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(1);

        final android.support.v4.app.NotificationCompat.Builder b
                = new NotificationCompat.Builder(context)
                .setStyle(style)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(media.getTitle())
                .setContentText(media.getArtist())
                .setContentIntent(PendingIntent.getActivity(context, 4, contentIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(state == PlaybackService.STATE_PLAYING ? R.drawable.ic_stat_play
                        : R.drawable.ic_stat_pause)
                .setLargeIcon(art)
                .addAction(R.drawable.ic_fast_rewind_white_24dp,
                        context.getText(R.string.Previous), prevIntent);

        if (state == PlaybackService.STATE_PLAYING) {
            b.addAction(R.drawable.ic_pause_white_24dp, context.getText(R.string.Pause),
                    middleActionIntent);
        } else {
            b.addAction(R.drawable.ic_play_arrow_white_24dp, context.getText(R.string.Play),
                    middleActionIntent);
        }

        b.addAction(R.drawable.ic_fast_forward_white_24dp, context.getText(R.string.Next),
                nextIntent);

        return b.build();
    }

}
