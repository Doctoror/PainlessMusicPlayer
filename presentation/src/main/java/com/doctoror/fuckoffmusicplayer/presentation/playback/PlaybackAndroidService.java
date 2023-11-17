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
package com.doctoror.fuckoffmusicplayer.presentation.playback;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryPermissionsChecker;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * Media playback Service
 */
public final class PlaybackAndroidService extends Service {

    static final String ACTION_RESEND_STATE = "ACTION_RESEND_STATE";

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static final String ACTION_PLAY_ANYTHING = "ACTION_PLAY_ANYTHING";

    static final String ACTION_PLAY = "ACTION_PLAY";
    static final String ACTION_PAUSE = "ACTION_PAUSE";
    static final String ACTION_STOP = "ACTION_STOP";
    static final String ACTION_STOP_WITH_ERROR = "ACTION_STOP_WITH_ERROR";

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static final String ACTION_PREV = "ACTION_PREV";

    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    public static final String ACTION_NEXT = "ACTION_NEXT";

    static final String ACTION_SEEK = "ACTION_SEEK";
    static final String EXTRA_ERROR_MESSAGE = "EXTRA_ERROR_MESSAGE";
    static final String EXTRA_POSITION_PERCENT = "EXTRA_POSITION_PERCENT";

    static final String EXTRA_QUEUE = "EXTRA_QUEUE";
    static final String EXTRA_POSITION = "EXTRA_POSITION";

    private final ResendStateReceiver resendStateReceiver = new ResendStateReceiver();

    @Inject
    CurrentMediaProvider currentMediaProvider;

    @Inject
    LibraryPermissionsChecker libraryPermissionsChecker;

    @Inject
    PlaybackData playbackData;

    @Inject
    PlaybackService service;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
        registerReceiver(resendStateReceiver, new IntentFilter(ACTION_RESEND_STATE));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        unregisterReceiver(resendStateReceiver);
        service.destroy();
    }

    public void restart() {
        service.restart();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (!libraryPermissionsChecker.permissionsGranted()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (intent == null) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        final String action = intent.getAction();
        if (action == null) {
            stopSelf(startId);
            return START_NOT_STICKY;
        }

        switch (intent.getAction()) {
            case ACTION_PLAY_PAUSE:
                onActionPlayPause();
                break;

            case ACTION_PLAY:
                onActionPlay(intent);
                break;

            case ACTION_PLAY_ANYTHING:
                onActionPlayAnything();
                break;

            case ACTION_PAUSE:
                onActionPause();
                break;

            case ACTION_STOP:
                onActionStop();
                break;

            case ACTION_STOP_WITH_ERROR:
                onActionStopWithError(intent.getCharSequenceExtra(EXTRA_ERROR_MESSAGE));
                break;

            case ACTION_PREV:
                onActionPrev();
                break;

            case ACTION_NEXT:
                onActionNext();
                break;

            case ACTION_SEEK:
                onActionSeek(intent);
                break;

            case Intent.ACTION_MEDIA_BUTTON:
                onActionMediaButton(intent);
                break;

            default:
                stopSelf(startId);
                return START_NOT_STICKY;
        }
        return START_STICKY;
    }

    private void onActionMediaButton(@NonNull final Intent intent) {
        final KeyEvent keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    onActionPlayPause();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    onActionPause();
                    break;

                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    onActionNext();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    onActionPrev();
                    break;

                case KeyEvent.KEYCODE_MEDIA_STOP:
                    onActionStop();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    onActionPlayPause();
                    break;
            }
        }
    }

    private void onActionPlay(@NonNull final Intent intent) {
        final List<Media> queue = intent.getParcelableArrayListExtra(EXTRA_QUEUE);
        final int position = intent.getIntExtra(EXTRA_POSITION, 0);
        service.play(queue, position);
    }

    private void onActionPlayAnything() {
        service.playAnything();
    }

    private void onActionPause() {
        service.pause();
    }

    private void onActionStop() {
        service.stop();
    }

    private void onActionStopWithError(@Nullable final CharSequence errorMessage) {
        service.stopWithError(errorMessage);
    }

    private void onActionPrev() {
        service.playPrev();
    }

    private void onActionNext() {
        service.playNext();
    }

    private void onActionPlayPause() {
        service.playPause();
    }

    private void onActionSeek(final Intent intent) {
        if (intent.hasExtra(EXTRA_POSITION_PERCENT)) {
            onActionSeek(intent.getFloatExtra(EXTRA_POSITION_PERCENT, 0f));
        }
    }

    private void onActionSeek(final float positionPercent) {
        final Media media = currentMediaProvider.getCurrentMedia();
        if (media != null) {
            final long duration = media.getDuration();
            if (duration > 0) {
                final int position = (int) ((float) duration * positionPercent);
                playbackData.setMediaPosition(position);
                service.seek(position);
            }
        }
    }

    private final class ResendStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            service.notifyState();
        }
    }
}
