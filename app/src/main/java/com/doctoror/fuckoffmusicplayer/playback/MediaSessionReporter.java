/*
 * Copyright (C) 2016 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.playback;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.doctoror.fuckoffmusicplayer.appwidget.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.Log;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.io.File;
import java.util.concurrent.ExecutionException;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 24.10.16.
 */

final class MediaSessionReporter {

    private static final String ACTION_PLAYSTATE_CHANGED = "com.android.music.playstatechanged";

    private static final String TAG = "MediaSessionReporter";

    private MediaSessionReporter() {

    }

    static void reportStateChanged(@NonNull final Context context,
            @NonNull final MediaSessionCompat mediaSession,
            @NonNull final Media media,
            final int state,
            @Nullable final CharSequence errorMessage) {
        final int playbackState;
        switch (state) {
            case PlaybackService.STATE_IDLE:
                playbackState = PlaybackStateCompat.STATE_NONE;
                break;

            case PlaybackService.STATE_LOADING:
                playbackState = PlaybackStateCompat.STATE_BUFFERING;
                break;

            case PlaybackService.STATE_PLAYING:
                playbackState = PlaybackStateCompat.STATE_PLAYING;
                break;

            case PlaybackService.STATE_PAUSED:
                playbackState = PlaybackStateCompat.STATE_PAUSED;
                break;

            case PlaybackService.STATE_ERROR:
                playbackState = PlaybackStateCompat.STATE_ERROR;
                break;

            default:
                return;
        }

        final boolean isPlaying = playbackState == PlaybackStateCompat.STATE_PLAYING;
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_STOP
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SEEK_TO)
                .setErrorMessage(errorMessage)
                .setState(playbackState, 0, isPlaying ? 1 : 0)
                .build());

        final Intent i = androidMusicMediaIntent(ACTION_PLAYSTATE_CHANGED, media, isPlaying);
        sendAndroidMusicPlayerBroadcast(context, i);
    }

    @WorkerThread
    static void reportTrackChanged(@NonNull final Context context,
            @NonNull final RequestManager glide,
            @NonNull final MediaSessionCompat mediaSession,
            @NonNull final Media media) {
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
            final DisplayMetrics dm = context.getResources().getDisplayMetrics();
            // Load bitmap because of https://code.google.com/p/android/issues/detail?id=194874
            try {
                //noinspection SuspiciousNameCombination
                artBitmapLarge = glide.load(art)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        // Optimized for lock screen
                        .into(dm.widthPixels, dm.widthPixels)
                        .get();
            } catch (ExecutionException | InterruptedException e) {
                Log.w(TAG, "Failed loading art image", e);
            }
            // Small bitmap for app widget, if any
            final int dp84 = (int) (84f * dm.density);

            try {
                artBitmapSmall = glide.load(art)
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

        AlbumThumbHolder.getInstance(context).setAlbumThumb(artBitmapSmall);
        mediaSession.setMetadata(b.build());

        final Intent intent = androidMusicMediaIntent("com.android.music.metachanged",
                media, false);
        sendAndroidMusicPlayerBroadcast(context, intent);
    }

    @NonNull
    private static Intent androidMusicMediaIntent(@NonNull final String action,
            @NonNull final Media media,
            final boolean playing) {
        final Intent i = new Intent(action);
        i.putExtra("id", Long.valueOf(media.getId()));
        i.putExtra("artist", media.getArtist());
        i.putExtra("album", media.getAlbum());
        i.putExtra("track", media.getTitle());
        i.putExtra("playing", playing);
        return i;
    }

    private static void sendAndroidMusicPlayerBroadcast(@NonNull final Context context,
            @NonNull final Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //noinspection deprecation
            context.sendStickyBroadcast(intent);
        } else {
            context.sendBroadcast(intent);
        }
    }
}
