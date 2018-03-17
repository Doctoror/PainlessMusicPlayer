/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.data.media.session;

import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionFactory;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;

public final class MediaSessionFactoryImpl implements MediaSessionFactory {

    private static final String TAG_MEDIA_SESSION = "Default";

    private final Context context;
    private final Class<? extends Activity> sessionActivityClass;
    private final MediaSessionCallback mediaSessionCallback;

    private final ComponentName mediaButtonReceiver;
    private final PendingIntent mediaButtonIntent;

    public MediaSessionFactoryImpl(
            @NonNull final Context context,
            @NonNull final Class<? extends Activity> sessionActivityClass,
            @NonNull final MediaSessionCallback mediaSessionCallback) {
        this.context = context;
        this.sessionActivityClass = sessionActivityClass;
        this.mediaSessionCallback = mediaSessionCallback;

        this.mediaButtonReceiver = new ComponentName(context, MediaButtonReceiver.class);
        this.mediaButtonIntent = PendingIntent.getBroadcast(
                context,
                1,
                new Intent(context, MediaButtonReceiver.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    @Override
    public MediaSessionCompat newMediaSession() {
        final MediaSessionCompat mediaSession = new MediaSessionCompat(
                context, TAG_MEDIA_SESSION, mediaButtonReceiver, mediaButtonIntent);
        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setSessionActivity(PendingIntent.getActivity(context, 1,
                new Intent(context, sessionActivityClass), PendingIntent.FLAG_UPDATE_CURRENT));
        mediaSession.setActive(true);
        return mediaSession;
    }
}
