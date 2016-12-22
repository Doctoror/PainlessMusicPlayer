package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

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
    public void reportTrackChanged(@NonNull final Media media, final int positionInPlaylist) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportTrackChanged(media, positionInPlaylist);
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

    @Override
    public void reportPositionChanged(final long mediaId, final long position) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportPositionChanged(mediaId, position);
        }
    }

    @Override
    public void reportPlaylistChanged(@Nullable final List<Media> playlist) {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < mReporters.length; i++) {
            mReporters[i].reportPlaylistChanged(playlist);
        }
    }
}
