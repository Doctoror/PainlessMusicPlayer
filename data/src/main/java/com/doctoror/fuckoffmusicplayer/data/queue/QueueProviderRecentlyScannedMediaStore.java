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
package com.doctoror.fuckoffmusicplayer.data.queue;

import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.data.tracks.MediaStoreTracksProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueConfig;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;

import java.util.List;

import io.reactivex.Observable;

/**
 * MediaStore {@link QueueProviderRecentlyScanned}
 */
public final class QueueProviderRecentlyScannedMediaStore
        implements QueueProviderRecentlyScanned {

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public QueueProviderRecentlyScannedMediaStore(
            @NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @NonNull
    public Observable<List<Media>> recentlyScannedQueue() {
        return mMediaProvider.load(
                MediaStoreTracksProvider.SELECTION_NON_HIDDEN_MUSIC,
                null,
                MediaStore.Audio.Media.DATE_ADDED + " DESC",
                QueueConfig.MAX_QUEUE_SIZE);
    }
}
