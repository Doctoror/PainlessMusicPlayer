package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Reports to multiple {@link PlaybackReporter}s
 */
final class PlaybackReporterSet implements PlaybackReporter {

    @NonNull
    private final PlaybackReporter[] mReporters;

    PlaybackReporterSet(@NonNull final PlaybackReporter... reporters) {
        mReporters = reporters;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportTrackChanged(media);
        }
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportPlaybackStateChanged(state, errorMessage);
        }
    }
}
