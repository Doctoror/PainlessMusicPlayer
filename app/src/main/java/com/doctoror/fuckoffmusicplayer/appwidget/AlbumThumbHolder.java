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
package com.doctoror.fuckoffmusicplayer.appwidget;

import com.doctoror.fuckoffmusicplayer.util.Log;
import com.doctoror.fuckoffmusicplayer.Handlers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Yaroslav Mytkalyk on 11.11.16.
 *
 * Holds album art thumb for appwidget
 */

public final class AlbumThumbHolder {

    private static final String TAG = "AlbumThumbHolder";

    // Is not a leak since it's an application context
    @SuppressLint("StaticFieldLeak")
    private static volatile AlbumThumbHolder sInstance;

    @NonNull
    public static AlbumThumbHolder getInstance(@NonNull final Context context) {
        if (sInstance == null) {
            synchronized (AlbumThumbHolder.class) {
                if (sInstance == null) {
                    sInstance = new AlbumThumbHolder(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    private static final String FILE_NAME = "art_thumb.png";

    private static final Object THUMB_LOCK = new Object();

    @NonNull
    private final Context mContext;

    @Nullable
    private Bitmap mAlbumThumb;

    private AlbumThumbHolder(@NonNull final Context context) {
        mContext = context;
        read();
    }

    @Nullable
    public Bitmap getAlbumThumb() {
        synchronized (THUMB_LOCK) {
            return mAlbumThumb;
        }
    }

    public void setAlbumThumb(@Nullable final Bitmap albumThumb) {
        synchronized (THUMB_LOCK) {
            mAlbumThumb = albumThumb;
        }
        writeAsync();
    }

    private void read() {
        InputStream is = null;
        try {
            is = mContext.openFileInput(FILE_NAME);
            final Bitmap albumThumb = BitmapFactory.decodeStream(is);
            synchronized (THUMB_LOCK) {
                mAlbumThumb = albumThumb;
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
            bitmap = mAlbumThumb;
        }
        if (bitmap == null) {
            mContext.deleteFile(FILE_NAME);
            return;
        }
        OutputStream os = null;
        try {
            os = mContext.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            final boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            os.flush();
            os.close();
            if (!result) {
                mContext.deleteFile(FILE_NAME);
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
        Handlers.runOnIoThread(this::write);
    }
}
