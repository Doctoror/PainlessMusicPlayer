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
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetchException;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.presentation.Henson;

import java.util.List;

public final class PlaybackNotificationFactoryImpl implements PlaybackNotificationFactory {

    private static final String TAG = "PlaybackNotification";
    private static final String CHANNEL_ID = "NowPlaying";

    @NonNull
    @Override
    public Notification create(
            @NonNull final Context context,
            @NonNull final AlbumArtFetcher albumArtFetcher,
            @NonNull final Media media,
            @NonNull final PlaybackState state,
            @NonNull final MediaSessionCompat mediaSession) {
        ensureChannelExists(context);

        final Bitmap art = loadAlbumArt(context, albumArtFetcher, media);
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
            @NonNull final AlbumArtFetcher albumArtFetcher,
            @NonNull final Media media) {
        Bitmap art = null;
        final String artLocation = media.getAlbumArt();
        if (!TextUtils.isEmpty(artLocation)) {
            final int dp128 = (int) (context.getResources().getDisplayMetrics().density * 128);
            try {
                art = albumArtFetcher.fetch(
                        artLocation,
                        dp128,
                        dp128
                );
            } catch (AlbumArtFetchException e) {
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
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @NonNull
    private static NotificationCompat.Style createNotificationStyle(
            @NonNull final MediaSessionCompat mediaSession) {
        return new androidx.media.app.NotificationCompat
                .MediaStyle()
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
                                   @NonNull final PlaybackState state) {
        final PendingIntent middleActionIntent = PendingIntent.getService(context, 3,
                PlaybackServiceIntentFactory.intentPlayPause(context),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        b.addAction(R.drawable.ic_fast_rewind_white_24dp,
                context.getText(R.string.Previous), prevIntent);
    }

    private static void addActionNext(@NonNull final Context context,
                                      @NonNull final NotificationCompat.Builder b) {
        final PendingIntent nextIntent = PendingIntent.getService(context, 2,
                PlaybackServiceIntentFactory.intentNext(context),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

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
