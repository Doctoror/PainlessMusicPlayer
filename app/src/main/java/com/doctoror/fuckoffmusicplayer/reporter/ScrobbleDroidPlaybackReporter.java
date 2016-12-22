package com.doctoror.fuckoffmusicplayer.reporter;

import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackService;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * {@link PlaybackReporter} for ScrobbleDroid
 * https://github.com/JJC1138/scrobbledroid/wiki/Developer-API
 */
final class ScrobbleDroidPlaybackReporter implements PlaybackReporter {

    private static final String ACTION = "net.jjc1138.android.scrobbler.action.MUSIC_STATUS";
    private static final String PLAYING = "playing";
    private static final String ID = "id";
    private static final String ARTIST = "artist";
    private static final String TRACK = "track";
    private static final String SECS = "secs";
    private static final String ALBUM = "album";

    @NonNull
    private final Context mContext;

    private Media mMedia;
    private boolean mIsPlaying;

    ScrobbleDroidPlaybackReporter(@NonNull final Context context) {
        mContext = context;
        mMedia = CurrentPlaylist.getInstance(context).getMedia();
        mIsPlaying = PlaybackService.getLastKnownState() == PlaybackState.STATE_PLAYING;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media) {
        mMedia = media;
        report(media, mIsPlaying);
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        mIsPlaying = state == PlaybackState.STATE_PLAYING;
        report(mMedia, mIsPlaying);
    }

    private void report(@Nullable final Media media, final boolean isPlaying) {
        if (media != null) {
            final Intent intent = new Intent(ACTION);
            intent.putExtra(PLAYING, isPlaying);
            final long mediaId = media.getId();
            if (mediaId == 0L || mediaId > Integer.MAX_VALUE) {
                // 0 means not from MediaStore.
                // More than MAX_VALUE will not work for current Simple Last.fm Scrollber
                intent.putExtra(ARTIST, media.getArtist());
                intent.putExtra(TRACK, media.getTitle());
                intent.putExtra(SECS, (int) (media.getDuration() / 1000L));
                intent.putExtra(ALBUM, media.getAlbum());
            } else {
                // From MediaStore
                intent.putExtra(ID, (int) media.getId());
            }
            mContext.sendBroadcast(intent);
        }
    }
}
