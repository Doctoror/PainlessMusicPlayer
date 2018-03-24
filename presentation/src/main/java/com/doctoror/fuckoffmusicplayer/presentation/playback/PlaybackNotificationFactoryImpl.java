package com.doctoror.fuckoffmusicplayer.presentation.playback;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.doctoror.fuckoffmusicplayer.presentation.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import java.util.List;
import java.util.concurrent.ExecutionException;

public final class PlaybackNotificationFactoryImpl implements PlaybackNotificationFactory {

    private static final String TAG = "PlaybackNotification";
    private static final String CHANNEL_ID = "NowPlaying";

    private final RequestOptions requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE);

    @NonNull
    @Override
    public Notification create(
            @NonNull final Context context,
            @NonNull final Media media,
            @PlaybackState final int state,
            @NonNull final MediaSessionCompat mediaSession) {
        ensureChannelExists(context);

        final Bitmap art = loadAlbumArt(context, media);
        final PendingIntent contentIntent = createContentIntent(context);
        final NotificationCompat.Style style = createNotificationStyle(mediaSession);

        final NotificationCompat.Builder b
                = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setStyle(style)
                .setShowWhen(false)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setContentTitle(media.getTitle())
                .setContentText(media.getArtist())
                .setContentIntent(contentIntent)
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

    @NonNull
    private Bitmap loadAlbumArt(
            @NonNull final Context context,
            @NonNull final Media media) {
        Bitmap art = null;
        final String artLocation = media.getAlbumArt();
        if (!TextUtils.isEmpty(artLocation)) {
            final int dp128 = (int) (context.getResources().getDisplayMetrics().density * 128);
            try {
                art = Glide.with(context)
                        .asBitmap()
                        .apply(requestOptions)
                        .load(artLocation)
                        .submit(dp128, dp128)
                        .get();
            } catch (InterruptedException | ExecutionException e) {
                Log.w(TAG, e);
            }
        }
        if (art == null) {
            art = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.album_art_placeholder);
        }
        return art;
    }

    @NonNull
    private static PendingIntent createContentIntent(@NonNull final Context context) {
        final Intent contentIntent = Henson.with(context)
                .gotoNowPlayingActivity()
                .hasCoverTransition(false)
                .hasListViewTransition(false)
                .build();

        return PendingIntent.getActivity(context, 4, contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    private static NotificationCompat.Style createNotificationStyle(
            @NonNull final MediaSessionCompat mediaSession) {
        return new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(mediaSession.getSessionToken())
                .setShowActionsInCompactView(1, 2);
    }

    private static void addAction1(
            @NonNull final Context context,
            @NonNull final NotificationCompat.Builder b) {
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
                                   @NonNull final NotificationCompat.Builder b,
                                   @PlaybackState final int state) {
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
                                   @NonNull final NotificationCompat.Builder b) {
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
                                      @NonNull final NotificationCompat.Builder b) {
        final PendingIntent prevIntent = PendingIntent.getService(context, 1,
                PlaybackServiceIntentFactory.intentPrev(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        b.addAction(R.drawable.ic_fast_rewind_white_24dp,
                context.getText(R.string.Previous), prevIntent);
    }

    private static void addActionNext(@NonNull final Context context,
                                      @NonNull final NotificationCompat.Builder b) {
        final PendingIntent nextIntent = PendingIntent.getService(context, 2,
                PlaybackServiceIntentFactory.intentNext(context),
                PendingIntent.FLAG_UPDATE_CURRENT);

        b.addAction(R.drawable.ic_fast_forward_white_24dp, context.getText(R.string.Next),
                nextIntent);
    }

    private static void ensureChannelExists(@NonNull final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ensureChannelExistsV26(context);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static void ensureChannelExistsV26(@NonNull final Context context) {
        final NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            ensureChannelExists(context, notificationManager);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static void ensureChannelExists(
            @NonNull final Context context,
            @NonNull final NotificationManager notificationManager) {
        if (!hasChannels(notificationManager)) {
            final NotificationChannel channel = createChannel(context);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static NotificationChannel createChannel(
            @NonNull final Context context) {
        final NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.Now_Playing),
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        channel.setShowBadge(true);
        channel.enableVibration(false);
        channel.setSound(null, null);
        return channel;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private static boolean hasChannels(@NonNull final NotificationManager notificationManager) {
        final List<NotificationChannel> channels = notificationManager
                .getNotificationChannels();
        return channels != null && !channels.isEmpty();
    }
}
