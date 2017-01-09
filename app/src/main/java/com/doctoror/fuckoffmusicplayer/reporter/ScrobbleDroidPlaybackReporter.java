package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.util.Objects;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * {@link PlaybackReporter} for ScrobbleDroid
 * https://github.com/JJC1138/scrobbledroid/wiki/Developer-API
 */
public final class ScrobbleDroidPlaybackReporter implements PlaybackReporter {

    private static final String ACTION = "net.jjc1138.android.scrobbler.action.MUSIC_STATUS";
    private static final String PLAYING = "playing";
    private static final String ARTIST = "artist";
    private static final String TRACK = "track";
    private static final String SECS = "secs";
    private static final String ALBUM = "album";

    @NonNull
    private final Context mContext;

    private Media mMedia;
    private boolean mIsPlaying;

    ScrobbleDroidPlaybackReporter(@NonNull final Context context,
            @Nullable final Media currentMedia) {
        mContext = context;
        mMedia = currentMedia;
        mIsPlaying = PlaybackService.getLastKnownState() == PlaybackState.STATE_PLAYING;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media, final int positionInQueue) {
        if (!Objects.equals(mMedia, media)) {
            mMedia = media;
            if (mIsPlaying) {
                report(media, true);
            }
        }
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        final boolean isPlaying = state == PlaybackState.STATE_PLAYING;
        if (mIsPlaying != isPlaying) {
            mIsPlaying = isPlaying;
            report(mMedia, isPlaying);
        }
    }

    private void report(@Nullable final Media media, final boolean isPlaying) {
        if (media != null) {
            final Intent intent = new Intent(ACTION);
            intent.putExtra(PLAYING, isPlaying);
            intent.putExtra(ARTIST, media.getArtist());
            intent.putExtra(TRACK, media.getTitle());
            intent.putExtra(SECS, (int) (media.getDuration() / 1000L));
            intent.putExtra(ALBUM, media.getAlbum());
            mContext.sendBroadcast(intent);
        }
    }

    @Override
    public void reportPositionChanged(final long mediaId, final long position) {
        // Not supported
    }

    @Override
    public void reportQueueChanged(@Nullable final List<Media> queue) {
        // Not supported
    }

    @Override
    public void onDestroy() {
        // Don't care
    }
}