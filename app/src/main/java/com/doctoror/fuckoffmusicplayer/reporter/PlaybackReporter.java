package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Media Reporter
 */
public interface PlaybackReporter {

    void reportTrackChanged(@NonNull Media media);

    void reportPlaybackStateChanged(@PlaybackState.State int state,
            @Nullable CharSequence errorMessage);
}
