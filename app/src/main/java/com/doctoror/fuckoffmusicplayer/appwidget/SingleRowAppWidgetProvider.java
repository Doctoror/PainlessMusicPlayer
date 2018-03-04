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
package com.doctoror.fuckoffmusicplayer.appwidget;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.playback.PlaybackDataUtils;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.home.HomeActivity;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceIntentFactory;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.RemoteViews;

import javax.inject.Inject;

/**
 * Launcher {@link AppWidgetProvider}
 */
public final class SingleRowAppWidgetProvider extends AppWidgetProvider {

    @Inject
    AlbumThumbHolder albumThumbHolder;

    @Inject
    PlaybackData playbackData;

    private static void requestServiceStateUpdate(final Context context) {
        PlaybackServiceControl.resendState(context);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        if (PlaybackService.ACTION_STATE_CHANGED.equals(intent.getAction())) {
            @PlaybackState.State final int state = intent.getIntExtra(
                    PlaybackService.EXTRA_STATE, PlaybackState.STATE_IDLE);
            onStateChanged(context, albumThumbHolder, playbackData, state);
        } else {
            // Handle AppWidgetProvider broadcast
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onUpdate(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] appWidgetIds) {
        bindViews(
                context,
                albumThumbHolder,
                playbackData,
                appWidgetManager,
                appWidgetIds,
                PlaybackState.STATE_IDLE);
        requestServiceStateUpdate(context);
    }

    private static void onStateChanged(
            @NonNull final Context context,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final PlaybackData playbackData,
            final int state) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, SingleRowAppWidgetProvider.class));

        bindViews(context, albumThumbHolder, playbackData, appWidgetManager, appWidgetIds, state);
    }

    private static void bindViews(
            @NonNull final Context context,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final PlaybackData playbackData,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] appWidgetIds,
            @PlaybackState.State final int state) {
        final RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_single_row);

        final Media media = PlaybackDataUtils.getCurrentMedia(playbackData);
        final boolean hasMedia = media != null;

        views.setImageViewResource(R.id.appwidget_btn_play_pause,
                state == PlaybackState.STATE_PLAYING
                        ? R.drawable.ic_pause_white_24dp
                        : R.drawable.ic_play_arrow_white_24dp);

        if (hasMedia) {
            setPlayPauseButtonAction(context, views);
            setPrevButtonAction(context, views);
            setNextButtonAction(context, views);
        } else {
            final PendingIntent playAnything = generatePlayAnythingIntent(context);
            setButtonAction(views, R.id.appwidget_btn_play_pause, playAnything);
            setButtonAction(views, R.id.appwidget_btn_prev, playAnything);
            setButtonAction(views, R.id.appwidget_btn_next, playAnything);
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

        final Bitmap thumb = albumThumbHolder.getAlbumThumb();
        if (thumb != null) {
            views.setImageViewBitmap(R.id.appwidget_img_albumart, thumb);
        } else {
            views.setImageViewResource(R.id.appwidget_img_albumart,
                    R.drawable.album_art_placeholder);
        }

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    @NonNull
    private static PendingIntent generatePlayAnythingIntent(@NonNull final Context context) {
        final Intent intent = PlaybackServiceIntentFactory.intentPlayAnything(context);
        return serviceIntent(context, intent);
    }

    @NonNull
    private static PendingIntent generatePlayPauseIntent(@NonNull final Context context) {
        final Intent intent = PlaybackServiceIntentFactory.intentPlayPause(context);
        return serviceIntent(context, intent);
    }

    @NonNull
    private static PendingIntent generatePrevIntent(@NonNull final Context context) {
        final Intent intent = PlaybackServiceIntentFactory.intentPrev(context);
        return serviceIntent(context, intent);
    }

    @NonNull
    private static PendingIntent generateNextIntent(@NonNull final Context context) {
        final Intent intent = PlaybackServiceIntentFactory.intentNext(context);
        return serviceIntent(context, intent);
    }

    @NonNull
    private static PendingIntent serviceIntent(@NonNull final Context context,
            @NonNull final Intent intent) {
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static void setPlayPauseButtonAction(final Context context, final RemoteViews views) {
        setButtonAction(views, R.id.appwidget_btn_play_pause, generatePlayPauseIntent(context));
    }

    private static void setPrevButtonAction(final Context context, final RemoteViews views) {
        setButtonAction(views, R.id.appwidget_btn_prev, generatePrevIntent(context));
    }

    private static void setNextButtonAction(final Context context, final RemoteViews views) {
        setButtonAction(views, R.id.appwidget_btn_next, generateNextIntent(context));
    }

    private static void setButtonAction(@NonNull final RemoteViews views,
            @IdRes final int buttonId,
            @NonNull final PendingIntent action) {
        views.setOnClickPendingIntent(buttonId, action);
    }

    private static void setCoverClickAction(final Context context, final RemoteViews views,
            final boolean hasMedia) {
        final Intent coverIntent;
        if (hasMedia) {
            coverIntent = Henson.with(context)
                    .gotoNowPlayingActivity()
                    .hasCoverTransition(true)
                    .hasListViewTransition(false)
                    .build();
        } else {
            coverIntent = new Intent(context, HomeActivity.class);
        }
        views.setOnClickPendingIntent(R.id.appwidget_img_albumart, PendingIntent.getActivity(
                context, 0, coverIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
}
