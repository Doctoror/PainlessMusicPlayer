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

import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderFiles;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.data.media.MediaStoreMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;

/**
 * MediaStore {@link QueueProviderFiles}
 */
public final class QueueProviderFilesMediaStore implements QueueProviderFiles {

    private static final String TAG = "QueueProviderFilesMediaStore";

    @NonNull
    private final MediaStoreMediaProvider mMediaProvider;

    public QueueProviderFilesMediaStore(@NonNull final MediaStoreMediaProvider mediaProvider) {
        mMediaProvider = mediaProvider;
    }

    @NonNull
    @Override
    public Observable<List<Media>> fromFile(@NonNull final Uri uri) {
        final Observable<List<Media>> fromProvider = mMediaProvider.load(
                MediaStore.Audio.Media.DATA.concat("=?"),
                new String[]{uri.getPath()},
                null,
                null);

        return fromProvider.flatMap(queue -> queue.isEmpty()
                    ? Observable.fromCallable(() -> queueFromFile(uri))
                    : Observable.just(queue));
    }

    @NonNull
    private static List<Media> queueFromFile(@NonNull final Uri uri) {
        return Collections.singletonList(mediaFromFile(uri));
    }

    @NonNull
    private static Media mediaFromFile(@NonNull final Uri uri) {
        final MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(uri.getPath());

            final Media media = new Media();
            media.setTitle(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE));
            media.setArtist(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST));
            media.setAlbum(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM));
            media.setData(uri);

            final String duration = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (!TextUtils.isEmpty(duration)) {
                try {
                    media.setDuration(Long.parseLong(duration));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mediaFromFile() duration is not a number: " + duration);
                }
            }

            final String trackNumber = r
                    .extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
            if (!TextUtils.isEmpty(trackNumber)) {
                try {
                    media.setTrack(Integer.parseInt(duration));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "mediaFromFile() track number is not a number: " + duration);
                }
            }

            return media;
        } finally {
            r.release();
        }
    }
}
