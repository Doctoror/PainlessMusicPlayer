package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

/**
 * Media Reporter
 */
public interface PlaybackReporter {

    @WorkerThread
    void reportTrackChanged(@NonNull Media media, int positionInQueue);

    @WorkerThread
    void reportPlaybackStateChanged(
            @PlaybackState.State int state,
            @Nullable CharSequence errorMessage);

    @WorkerThread
    void reportPositionChanged(long mediaId, long position);

    @WorkerThread
    void reportQueueChanged(@Nullable List<Media> queue);
}
