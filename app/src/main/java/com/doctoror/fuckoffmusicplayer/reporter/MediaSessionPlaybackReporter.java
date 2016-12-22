package com.doctoror.fuckoffmusicplayer.reporter;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.commons.playback.PlaybackState;
import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.appwidget.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.concurrent.ExecutionException;

/**
 * {@link PlaybackReporter} that reports to {@link MediaSessionCompat}
 */
final class MediaSessionPlaybackReporter implements PlaybackReporter {

    private static final String TAG = "MediaSessionPlaybackReporter";

    @NonNull
    private final DisplayMetrics mDisplayMetrics;

    @NonNull
    private final AlbumThumbHolder mAlbumThumbHolder;

    @NonNull
    private final MediaSessionCompat mMediaSession;

    @NonNull
    private final RequestManager mGlide;

    MediaSessionPlaybackReporter(@NonNull final Context context,
            @NonNull final MediaSessionCompat mediaSession,
            @NonNull final RequestManager glide) {
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mAlbumThumbHolder = AlbumThumbHolder.getInstance(context);
        mMediaSession = mediaSession;
        mGlide = glide;
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media) {
        final MediaMetadataCompat.Builder b = new MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        Long.toString(media.getId()))
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, media.getData().toString())
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, media.getTitle())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, media.getDuration())
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, media.getArtist())
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM, media.getAlbum())
                .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, media.getTrack());

        final String art = media.getAlbumArt();
        Bitmap artBitmapSmall = null;
        if (!TextUtils.isEmpty(art)) {
            Bitmap artBitmapLarge = null;
            // Load bitmap because of https://code.google.com/p/android/issues/detail?id=194874
            try {
                //noinspection SuspiciousNameCombination
                artBitmapLarge = mGlide.load(art)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        // Optimized for lock screen
                        .into(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels)
                        .get();
            } catch (ExecutionException | InterruptedException e) {
                Log.w(TAG, "Failed loading art image", e);
            }
            // Small bitmap for app widget, if any
            final int dp84 = (int) (84f * mDisplayMetrics.density);

            try {
                artBitmapSmall = mGlide.load(art)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        // Optimized for medium appwidget
                        .into(dp84, dp84)
                        .get();
            } catch (ExecutionException | InterruptedException e) {
                Log.w(TAG, "Failed loading art image", e);
            }
            if (artBitmapLarge != null) {
                b.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, artBitmapLarge);
            }
            b.putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI,
                    new File(media.getAlbumArt()).toURI().toString());
        }

        mAlbumThumbHolder.setAlbumThumb(artBitmapSmall);
        mMediaSession.setMetadata(b.build());
    }

    @Override
    public void reportPlaybackStateChanged(@PlaybackState.State final int state,
            @Nullable final CharSequence errorMessage) {
        @PlaybackStateCompat.State final int playbackState = toPlaybackStateCompat(state);
        final boolean isPlaying = playbackState == PlaybackStateCompat.STATE_PLAYING;
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH)
                .setErrorMessage(errorMessage)
                .setState(playbackState, 0, isPlaying ? 1 : 0)
                .build());
    }

    @PlaybackStateCompat.State
    private static int toPlaybackStateCompat(@PlaybackState.State final int state) {
        switch (state) {
            case PlaybackState.STATE_LOADING:
                return PlaybackStateCompat.STATE_BUFFERING;

            case PlaybackState.STATE_PLAYING:
                return PlaybackStateCompat.STATE_PLAYING;

            case PlaybackState.STATE_PAUSED:
                return PlaybackStateCompat.STATE_PAUSED;

            case PlaybackState.STATE_ERROR:
                return PlaybackStateCompat.STATE_ERROR;

            case PlaybackState.STATE_IDLE:
            default:
                return PlaybackStateCompat.STATE_NONE;
        }
    }
}
