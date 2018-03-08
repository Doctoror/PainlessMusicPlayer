package com.doctoror.fuckoffmusicplayer.domain.playback;

import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;

public interface PlaybackNotificationFactory {

    @NonNull
    Notification create(
            @NonNull Context context,
            @NonNull Media media,
            @PlaybackState.State int state,
            @NonNull MediaSessionCompat mediaSession);
}
