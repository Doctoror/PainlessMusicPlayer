/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.doctoror.fuckoffmusicplayer.data.reporter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporter;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * {@link PlaybackReporter} that reports to {@link MediaSessionCompat}
 */
public final class MediaSessionPlaybackReporter implements PlaybackReporter {

    private static final String TAG = "MediaSessionPlaybackReporter";

    private final DisplayMetrics mDisplayMetrics;
    private final AlbumThumbHolder mAlbumThumbHolder;
    private final MediaSessionCompat mMediaSession;
    private final RequestManager mGlide;

    private final RequestOptions requestOptions = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop();

    MediaSessionPlaybackReporter(
            @NonNull final Context context,
            @NonNull final AlbumThumbHolder albumThumbHolder,
            @NonNull final MediaSessionCompat mediaSession) {
        mDisplayMetrics = context.getResources().getDisplayMetrics();
        mAlbumThumbHolder = albumThumbHolder;
        mMediaSession = mediaSession;
        mGlide = Glide.with(context);
    }

    @Override
    public void reportTrackChanged(@NonNull final Media media, final int positionInQueue) {
        final Uri data = media.getData();
        final MediaMetadataCompat.Builder b = new MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                        Long.toString(media.getId()))
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                        data != null ? data.toString() : null)
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
                artBitmapLarge = mGlide
                        .asBitmap()
                        .apply(requestOptions)
                        .load(art)
                        // Optimized for lock screen
                        .submit(mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels)
                        .get();
            } catch (ExecutionException | InterruptedException e) {
                Log.w(TAG, "Failed loading art image", e);
            }
            // Small bitmap for app widget, if any
            final int dp84 = (int) (84f * mDisplayMetrics.density);

            try {
                artBitmapSmall = mGlide
                        .asBitmap()
                        .apply(requestOptions)
                        .load(art)
                        // Optimized for medium appwidget
                        .submit(dp84, dp84)
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
    public void reportPlaybackStateChanged(
            @NonNull final PlaybackState state,
            @Nullable final CharSequence errorMessage) {
        @PlaybackStateCompat.State final int playbackState = toPlaybackStateCompat(state);
        final boolean isPlaying = playbackState == PlaybackStateCompat.STATE_PLAYING;
        final PlaybackStateCompat.Builder builder = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH)
                .setState(playbackState, 0, isPlaying ? 1 : 0);

        if (errorMessage != null) {
            builder.setErrorMessage(PlaybackStateCompat.ERROR_CODE_APP_ERROR, errorMessage);
        }

        mMediaSession.setPlaybackState(builder.build());
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

    @PlaybackStateCompat.State
    private static int toPlaybackStateCompat(@NonNull final PlaybackState state) {
        switch (state) {
            case STATE_LOADING:
                return PlaybackStateCompat.STATE_BUFFERING;

            case STATE_PLAYING:
                return PlaybackStateCompat.STATE_PLAYING;

            case STATE_PAUSED:
                return PlaybackStateCompat.STATE_PAUSED;

            case STATE_ERROR:
                return PlaybackStateCompat.STATE_ERROR;

            case STATE_IDLE:
            default:
                return PlaybackStateCompat.STATE_NONE;
        }
    }
}
