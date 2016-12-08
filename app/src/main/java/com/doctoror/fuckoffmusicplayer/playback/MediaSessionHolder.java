package com.doctoror.fuckoffmusicplayer.playback;

import com.bumptech.glide.Glide;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistHolder;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 08.12.16.
 */

public final class MediaSessionHolder {

    private static final String TAG = "MediaSessionHolder";

    @SuppressLint("StaticFieldLeak") // Is not a leak for application context
    private static MediaSessionHolder sInstance;

    @NonNull
    public static MediaSessionHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (MediaSessionHolder.class) {
                if (sInstance == null) {
                    sInstance = new MediaSessionHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    @NonNull
    private final Context mContext;

    private volatile int mOpenCount;

    private MediaSessionCompat mMediaSession;

    private MediaSessionHolder(@NonNull final Context context) {
        mContext = context;
    }

    public void openSession() {
        synchronized (MediaSessionHolder.class) {
            mOpenCount++;
            if (mOpenCount == 1) {
                doOpenSession();
            }
        }
    }

    public void closeSession() {
        synchronized (MediaSessionHolder.class) {
            mOpenCount--;
            if (mOpenCount == 0) {
                doCloseSession();
            }
        }
    }

    private void doOpenSession() {
        final ComponentName mediaButtonReceiver = new ComponentName(mContext,
                MediaButtonReceiver.class);

        final PendingIntent broadcastIntent = PendingIntent
                .getBroadcast(mContext, 1, new Intent(mContext, MediaButtonReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        final MediaSessionCompat mediaSession = new MediaSessionCompat(mContext, TAG,
                mediaButtonReceiver, broadcastIntent);

        mediaSession.setCallback(new MediaSessionCallback(mContext));
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setActive(true);

        mMediaSession = mediaSession;
        Observable.create(s -> reportMediaAndState(mediaSession)).subscribeOn(Schedulers.io())
                .subscribe();
    }

    private void doCloseSession() {
        if (mMediaSession != null) {
            mMediaSession.setActive(false);
            mMediaSession.release();
            mMediaSession = null;
        }
    }

    @Nullable
    public MediaSessionCompat getMediaSession() {
        return mMediaSession;
    }

    @WorkerThread
    private void reportMediaAndState(@NonNull final MediaSessionCompat mediaSession) {
        final Media current = PlaylistHolder.getInstance(mContext).getMedia();
        if (current != null) {
            MediaSessionReporter.reportTrackChanged(mContext, Glide.with(mContext),
                    mediaSession, current);
        }
        MediaSessionReporter.reportStateChanged(mediaSession,
                PlaybackService.getLastKnownState(), null);
    }
}
