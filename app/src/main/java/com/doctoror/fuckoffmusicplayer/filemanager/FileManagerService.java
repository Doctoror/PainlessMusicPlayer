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
package com.doctoror.fuckoffmusicplayer.filemanager;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbums;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.util.FileUtils;

import org.parceler.Parcels;

import android.Manifest;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Service for managing files
 */
public final class FileManagerService extends IntentService {

    private static final String TAG = "FileManagerService";

    private static final String ACTION_MEDIA_DELETE = "ACTION_MEDIA_DELETE";
    private static final String EXTRA_MEDIA = "EXTRA_MEDIA";

    private static final String ACTION_ALBUM_DELETE = "ACTION_ALBUM_DELETE";
    private static final String EXTRA_TARGET_ID = "EXTRA_TARGET_ID";

    private static final String ACTION_PLAYLIST_DELETE = "ACTION_PLAYLIST_DELETE";

    public static void deleteMedia(@NonNull final Context context,
            @NonNull final Media media) {
        final Intent intent = new Intent(context, FileManagerService.class);
        intent.setAction(ACTION_MEDIA_DELETE);
        intent.putExtra(EXTRA_MEDIA, Parcels.wrap(media));
        context.startService(intent);
    }

    public static void deleteAlbum(@NonNull final Context context,
            final long albumId) {
        final Intent intent = new Intent(context, FileManagerService.class);
        intent.setAction(ACTION_ALBUM_DELETE);
        intent.putExtra(EXTRA_TARGET_ID, albumId);
        context.startService(intent);
    }

    public static void deletePlaylist(@NonNull final Context context,
            final long playlistId) {
        final Intent intent = new Intent(context, FileManagerService.class);
        intent.setAction(ACTION_PLAYLIST_DELETE);
        intent.putExtra(EXTRA_TARGET_ID, playlistId);
        context.startService(intent);
    }

    @Inject
    PlaylistProviderAlbums mPlaylistProviderAlbums;

    public FileManagerService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerHolder.getInstance(this).mainComponent().inject(this);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        switch (intent.getAction()) {
            case ACTION_MEDIA_DELETE:
                onActionMediaDelete(intent);
                break;

            case ACTION_ALBUM_DELETE:
                onActionAlbumDelete(intent);
                break;

            case ACTION_PLAYLIST_DELETE:
                onActionPlaylistDelete(intent);
                break;
        }
    }

    private long getTargetId(@NonNull final Intent intent) {
        if (!intent.hasExtra(EXTRA_TARGET_ID)) {
            throw new IllegalArgumentException("EXTRA_TARGET_ID is not passed");
        }

        return intent.getLongExtra(EXTRA_TARGET_ID, 0L);
    }

    private void onActionMediaDelete(@NonNull final Intent intent) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission not granted");
            return;
        }

        final Media media = Parcels.unwrap(intent.getParcelableExtra(EXTRA_MEDIA));
        if (media == null) {
            throw new IllegalArgumentException("EXTRA_MEDIA must not be null");
        }
        try {
            FileUtils.deleteMedia(getContentResolver(), media);
        } catch (Exception e) {
            Log.w(TAG, e);

            final Context context = getApplicationContext();
            Observable.create(s -> Toast.makeText(context,
                    context.getString(R.string.Failed_to_delete_s, media.getTitle()),
                    Toast.LENGTH_LONG).show())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    private void onActionAlbumDelete(@NonNull final Intent intent) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission not granted");
            return;
        }

        final long albumId = getTargetId(intent);
        try {
            FileUtils.deleteAlbum(getContentResolver(), mPlaylistProviderAlbums, albumId);
        } catch (Exception e) {
            Log.w(TAG, e);

            final Context context = getApplicationContext();
            Observable.create(s -> Toast.makeText(context,
                    R.string.Failed_to_delete_album, Toast.LENGTH_LONG).show())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }

    private void onActionPlaylistDelete(@NonNull final Intent intent) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission not granted");
            return;
        }

        final long targetId = getTargetId(intent);
        try {
            FileUtils.deletePlaylist(getContentResolver(), targetId);
        } catch (Exception e) {
            Log.w(TAG, e);

            final Context context = getApplicationContext();
            Observable.create(s -> Toast.makeText(context, R.string.Failed_to_delete_playlist,
                    Toast.LENGTH_LONG).show())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }
}
