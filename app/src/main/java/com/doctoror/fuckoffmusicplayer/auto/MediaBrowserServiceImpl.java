/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.doctoror.fuckoffmusicplayer.auto;

import com.doctoror.commons.util.Log;
import com.doctoror.fuckoffmusicplayer.playback.MediaSessionHolder;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.service.media.MediaBrowserService;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * {@link MediaBrowserService} implementation
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public final class MediaBrowserServiceImpl extends MediaBrowserServiceCompat {

    private static final String TAG = "MediaBrowserServiceImpl";

    private MediaSessionHolder mMediaSessionHolder;
    private MediaBrowserImpl mMediaBrowser;

    private PackageValidator mPackageValidator;

    @Override
    public void onCreate() {
        super.onCreate();
        mPackageValidator = new PackageValidator(this);
        mMediaBrowser = new MediaBrowserImpl(this);

        mMediaSessionHolder = MediaSessionHolder.getInstance(this);
        mMediaSessionHolder.openSession();

        final MediaSessionCompat mediaSession = mMediaSessionHolder.getMediaSession();
        if (mediaSession != null) {
            setSessionToken(mediaSession.getSessionToken());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaSessionHolder.closeSession();
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull final String clientPackageName,
            final int clientUid, final Bundle rootHints) {
        if (Log.logDEnabled()) {
            Log.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName
                    + "; clientUid=" + clientUid + " ; rootHints=" + rootHints);
        }
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!mPackageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return null. No further calls will
            // be made to other media browsing methods.
            Log.w(TAG, "OnGetRoot: IGNORING request from untrusted package " + clientPackageName);
            return null;
        }

        return mMediaBrowser.getRoot();
    }

    @Override
    public void onLoadChildren(@NonNull final String parentId,
            @NonNull final Result<List<MediaItem>> result) {
        if (Log.logDEnabled()) {
            Log.d(TAG, "OnLoadChildren: parentMediaId=" + parentId);
        }
        mMediaBrowser.onLoadChildren(parentId, result);
    }

    @Override
    public void onLoadItem(final String itemId, final Result<MediaItem> result) {
        if (Log.logDEnabled()) {
            Log.d(TAG, "OnLoadItem: itemId=" + itemId);
        }
        super.onLoadItem(itemId, result);
    }
}
