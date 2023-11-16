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
package com.doctoror.fuckoffmusicplayer.data.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.doctoror.commons.reactivex.SchedulersProvider;
import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.Completable;

/**
 * Holds album art thumb.
 */
public final class AlbumThumbHolderImpl implements AlbumThumbHolder {

    private static final String TAG = "AlbumThumbHolder";

    private static final String FILE_NAME = "art_thumb.png";

    private static final Object THUMB_LOCK = new Object();

    @NonNull
    private final Context context;

    @NonNull
    private final SchedulersProvider schedulersProvider;

    @Nullable
    private Bitmap albumThumb;

    public AlbumThumbHolderImpl(
            @NonNull final Context context,
            @NonNull final SchedulersProvider schedulersProvider) {
        this.context = context;
        this.schedulersProvider = schedulersProvider;
        read();
    }

    @Nullable
    @Override
    public Bitmap getAlbumThumb() {
        synchronized (THUMB_LOCK) {
            return albumThumb;
        }
    }

    @Override
    public void setAlbumThumb(@Nullable final Bitmap albumThumb) {
        synchronized (THUMB_LOCK) {
            this.albumThumb = albumThumb;
        }
        writeAsync();
    }

    private void read() {
        InputStream is = null;
        try {
            is = context.openFileInput(FILE_NAME);
            final Bitmap albumThumb = BitmapFactory.decodeStream(is);
            synchronized (THUMB_LOCK) {
                this.albumThumb = albumThumb;
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    private void write() {
        final Bitmap bitmap;
        synchronized (THUMB_LOCK) {
            bitmap = albumThumb;
        }
        if (bitmap == null) {
            context.deleteFile(FILE_NAME);
            return;
        }
        OutputStream os = null;
        try {
            os = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            final boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            if (!result) {
                context.deleteFile(FILE_NAME);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    private void writeAsync() {
        Completable.fromAction(this::write)
                .subscribeOn(schedulersProvider.io())
                .subscribe();
    }
}
