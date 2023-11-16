package com.doctoror.fuckoffmusicplayer.presentation.playback;

import android.app.Service;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher;
import com.doctoror.fuckoffmusicplayer.domain.media.session.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceView;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

public class PlaybackServiceViewImpl implements PlaybackServiceView {

    private static final int NOTIFICATION_ID = 666;

    private final MediaSessionHolder mediaSessionHolder;
    private final PlaybackNotificationFactory playbackNotificationFactory;
    private final Service service;

    public PlaybackServiceViewImpl(
            @NonNull final MediaSessionHolder mediaSessionHolder,
            @NonNull final PlaybackNotificationFactory playbackNotificationFactory,
            @NonNull final Service service) {
        this.mediaSessionHolder = mediaSessionHolder;
        this.playbackNotificationFactory = playbackNotificationFactory;
        this.service = service;
    }

    @Override
    public void startForeground(
            @NonNull final AlbumArtFetcher albumArtFetcher,
            @NonNull final Media media,
            @NonNull final PlaybackState state
    ) {
        final MediaSessionCompat mediaSession = mediaSessionHolder.getMediaSession();
        if (mediaSession != null) {
            service.startForeground(NOTIFICATION_ID, playbackNotificationFactory.create(
                    service, albumArtFetcher, media, state, mediaSession));
        }
    }

    @NonNull
    @Override
    public CharSequence showPlaybackFailedError(@Nullable final Exception error) {
        final CharSequence message = makePlaybackErrorMessage(error);
        // TODO show a notification instead
        Toast.makeText(service.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        return message;
    }

    @NonNull
    private CharSequence makePlaybackErrorMessage(@Nullable final Exception error) {
        final String message = error != null ? error.getMessage() : null;
        if (TextUtils.isEmpty(message)) {
            return service.getText(R.string.Failed_to_start_playback);
        }
        return message;
    }
}
