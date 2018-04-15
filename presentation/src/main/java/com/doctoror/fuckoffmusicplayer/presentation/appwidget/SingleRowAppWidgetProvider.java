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
package com.doctoror.fuckoffmusicplayer.presentation.appwidget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.reporter.AppWidgetPlaybackStateReporter;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Launcher {@link AppWidgetProvider}
 */
public final class SingleRowAppWidgetProvider extends AppWidgetProvider {

    @Inject
    PlaybackData playbackData;

    @Inject
    PlaybackServiceControl playbackServiceControl;

    @Inject
    SingleRowAppWidgetPresenter presenter;

    @Inject
    SingleRowAppWidgetViewBinder viewBinder;

    @Inject
    SingleRowAppWidgetViewModel viewModel;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        AndroidInjection.inject(this, context);
        final String action = intent.getAction();
        if (action != null) {
            switch (action) {
                case AppWidgetPlaybackStateReporter.ACTION_MEDIA_CHANGED:
                    onStateChanged(context, playbackData.getPlaybackState());
                    break;

                case AppWidgetPlaybackStateReporter.ACTION_STATE_CHANGED:
                    onStateChanged(context, fromIntentExtra(intent));
                    break;

                default:
                    // Handle AppWidgetProvider broadcast
                    super.onReceive(context, intent);
                    break;
            }
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
        bindViews(context, appWidgetManager, appWidgetIds, playbackData.getPlaybackState());
    }

    private void onStateChanged(
            @NonNull final Context context,
            @NonNull final PlaybackState playbackState) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, SingleRowAppWidgetProvider.class));

        bindViews(context, appWidgetManager, appWidgetIds, playbackState);
    }

    private void bindViews(
            @NonNull final Context context,
            @NonNull final AppWidgetManager appWidgetManager,
            @NonNull final int[] appWidgetIds,
            @NonNull final PlaybackState state) {

        final RemoteViews view = new RemoteViews(context.getPackageName(),
                R.layout.appwidget_single_row);

        presenter.bindState(context, state);
        viewBinder.bind(view, viewModel);

        appWidgetManager.updateAppWidget(appWidgetIds, view);
    }

    @NonNull
    private PlaybackState fromIntentExtra(@NonNull final Intent intent) {
        final int stateIndex = intent.getIntExtra(
                AppWidgetPlaybackStateReporter.EXTRA_STATE, PlaybackState.STATE_IDLE.ordinal());
        try {
            return PlaybackState.values()[stateIndex];
        } catch (final IllegalArgumentException e) {
            return PlaybackState.STATE_IDLE;
        }
    }
}
