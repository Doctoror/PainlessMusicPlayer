package com.doctoror.fuckoffmusicplayer.data.reporter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.data.util.Objects;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;
import com.doctoror.fuckoffmusicplayer.domain.settings.Settings;

public final class LastFmPlaybackReporter implements PlaybackReporter {

    private static final String ACTION_META_CHANGED = "fm.last.android.metachanged";

    private static final String ID = "id";
    private static final String PLAYING = "playing";
    private static final String ARTIST = "artist";
    private static final String TRACK = "track";
    private static final String DURATION = "duration";
    private static final String ALBUM = "album";

    private final Context context;
    private final Settings settings;

    private Media media;
    private boolean isPlaying;

    LastFmPlaybackReporter(
            @NonNull final Context context,
            @NonNull final CurrentMediaProvider currentMediaProvider,
            @NonNull final PlaybackData playbackData,
            @NonNull final Settings settings) {
        this.context = context;
        this.settings = settings;
        media = currentMediaProvider.getCurrentMedia();
        isPlaying = playbackData.getPlaybackState() == PlaybackState.STATE_PLAYING;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media) {
        if (Objects.notEqual(this.media, media)) {
            this.media = media;
            if (isPlaying) {
                reportMetadata(media);
            }
        }
    }

    @Override
    public void reportPlaybackStateChanged(
            @NonNull final PlaybackState state,
            @Nullable final CharSequence errorMessage) {
        final boolean isPlaying = state == PlaybackState.STATE_PLAYING;
        if (this.isPlaying != isPlaying) {
            this.isPlaying = isPlaying;
            reportMetadata(media);
        }
    }

    private void reportMetadata(@Nullable final Media media) {
        if (settings.isScrobbleEnabled() && media != null) {
            final Intent intent = new Intent(ACTION_META_CHANGED);
            intent.putExtra(ID, String.valueOf(media.getId()));
            intent.putExtra(ARTIST, media.getArtist());
            intent.putExtra(TRACK, media.getTitle());
            intent.putExtra(DURATION, media.getDuration());
            intent.putExtra(ALBUM, media.getAlbum());
            intent.putExtra(PLAYING, isPlaying);
            context.sendBroadcast(intent);
        }
    }
}
