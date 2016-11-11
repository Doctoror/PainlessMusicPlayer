package com.doctoror.fuckoffmusicplayer.appwidget;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.LibraryActivity;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistHolder;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.RemoteViews;

/**
 * Created by Yaroslav Mytkalyk on 11.11.16.
 */

public final class SingleRowAppWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "MediumAppWidgetProvider";

    private static void requestServiceStateUpdate(final Context context) {
        PlaybackService.resendState(context);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (PlaybackService.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            @PlaybackService.State
            final int state = intent.getIntExtra(PlaybackService.EXTRA_STATE,
                    PlaybackService.STATE_IDLE);
            onStateChanged(context, state);
        } else {
            // Handle AppWidgetProvider broadcast
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(final Context context,
            final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds) {

        bindViews(context, appWidgetManager, appWidgetIds, PlaybackService.STATE_IDLE);
        requestServiceStateUpdate(context);
    }

    private static void onStateChanged(final Context context,
            final int state) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, SingleRowAppWidgetProvider.class));

        bindViews(context, appWidgetManager, appWidgetIds, state);
    }

    private static void bindViews(final Context context,
            final AppWidgetManager appWidgetManager,
            final int[] appWidgetIds,
            @PlaybackService.State final int state) {
        final PlaylistHolder holder = PlaylistHolder.getInstance(context);

        final RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_single_row);

        final Media media = holder.getMedia();
        final boolean hasMedia = media != null;
        views.setBoolean(R.id.appwidget_btn_prev, "setEnabled", hasMedia);
        views.setBoolean(R.id.appwidget_btn_next, "setEnabled", hasMedia);
        views.setBoolean(R.id.appwidget_btn_play_pause, "setEnabled", hasMedia);

        views.setImageViewResource(R.id.appwidget_btn_play_pause,
                state == PlaybackService.STATE_PLAYING
                        ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp);

        if (hasMedia) {
            setPlayPauseButtonAction(context, views);
            setPrevButtonAction(context, views);
            setNextButtonAction(context, views);
        }
        setCoverClickAction(context, views, hasMedia);

        CharSequence artist = media != null ? media.getArtist() : null;
        CharSequence title = media != null ? media.getTitle() : null;

        if (TextUtils.isEmpty(artist)) {
            artist = context.getText(R.string.Unknown_artist);
        }
        if (TextUtils.isEmpty(title)) {
            title = context.getText(R.string.Untitled);
        }

        views.setTextViewText(R.id.appwidget_text_artist, artist);
        views.setTextViewText(R.id.appwidget_text_title, title);

        final Bitmap thumb = AlbumThumbHolder.getInstance(context).getAlbumThumb();
        if (thumb != null) {
            views.setImageViewBitmap(R.id.appwidget_img_albumart, thumb);
        } else {
            views.setImageViewResource(R.id.appwidget_img_albumart,
                    R.drawable.album_art_placeholder);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private static void setPlayPauseButtonAction(final Context context, final RemoteViews views) {
        final Intent playPauseIntent = PlaybackService.playPauseIntent(context);
        views.setOnClickPendingIntent(R.id.appwidget_btn_play_pause, PendingIntent.getService(
                context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private static void setPrevButtonAction(final Context context, final RemoteViews views) {
        final Intent playPauseIntent = PlaybackService.prevIntent(context);
        views.setOnClickPendingIntent(R.id.appwidget_btn_prev, PendingIntent.getService(
                context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private static void setNextButtonAction(final Context context, final RemoteViews views) {
        final Intent playPauseIntent = PlaybackService.nextIntent(context);
        views.setOnClickPendingIntent(R.id.appwidget_btn_next, PendingIntent.getService(
                context, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private static void setCoverClickAction(final Context context, final RemoteViews views,
            final boolean hasMedia) {
        final Intent coverIntent = new Intent();
        if (hasMedia) {
            coverIntent.setClass(context, NowPlayingActivity.class);
        } else {
            coverIntent.setClass(context, LibraryActivity.class);
        }
        views.setOnClickPendingIntent(R.id.appwidget_img_albumart, PendingIntent.getActivity(
                context, 0, coverIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
