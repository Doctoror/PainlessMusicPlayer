package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.fuckoffmusicplayer.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.settings.Settings;
import com.doctoror.fuckoffmusicplayer.util.Objects;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

public final class LastFmPlaybackReporter implements PlaybackReporter {

    private static final String ACTION_META_CHANGED = "fm.last.android.metachanged";

    private static final String ID = "id";
    private static final String PLAYING = "playing";
    private static final String ARTIST = "artist";
    private static final String TRACK = "track";
    private static final String DURATION = "duration";
    private static final String ALBUM = "album";

    @NonNull
    private final Context mContext;

    private Media mMedia;
    private boolean mIsPlaying;

    @Inject
    Settings mSettings;

    LastFmPlaybackReporter(@NonNull final Context context,
            @Nullable final Media currentMedia) {
        DaggerHolder.getInstance(context).mainComponent().inject(this);
        mContext = context;
        mMedia = currentMedia;
        mIsPlaying = PlaybackService.getLastKnownState() == PlaybackState.STATE_PLAYING;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media, final int positionInQueue) {
        if (!Objects.equals(mMedia, media)) {
            mMedia = media;
            if (mIsPlaying) {
                reportMetadata(media);
            }
        }
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        final boolean isPlaying = state == PlaybackState.STATE_PLAYING;
        if (mIsPlaying != isPlaying) {
            mIsPlaying = isPlaying;
            reportMetadata(mMedia);
        }
    }

    private void reportMetadata(@Nullable final Media media) {
        if (mSettings.isScrobbleEnabled() && media != null) {
            final Intent intent = new Intent(ACTION_META_CHANGED);
            intent.putExtra(ID, String.valueOf(media.getId()));
            intent.putExtra(ARTIST, media.getArtist());
            intent.putExtra(TRACK, media.getTitle());
            intent.putExtra(DURATION, media.getDuration());
            intent.putExtra(ALBUM, media.getAlbum());
            intent.putExtra(PLAYING, mIsPlaying);
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
