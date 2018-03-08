package com.doctoror.fuckoffmusicplayer.domain.playback;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

public interface PlaybackServicePresenter {

    void startForeground(@NonNull Media media, @PlaybackState int state);

    /**
     * Shows a playback error and returns the shown error message.
     *
     * @param error the playback error that occurred.
     * @return shown error message
     */
    @NonNull
    CharSequence showPlaybackFailedError(@Nullable Exception error);
}
