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
package com.doctoror.fuckoffmusicplayer.library.albums;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.base.BaseFragment;
import com.doctoror.fuckoffmusicplayer.db.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;
import com.doctoror.fuckoffmusicplayer.util.ObserverAdapter;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Handles album click from adapter view
 */
public final class AlbumClickHandler {

    private AlbumClickHandler() {
        throw new UnsupportedOperationException();
    }

    /**
     * Used to provide item view for position. This is required so that item view is retreived from
     * adapter view after background work is finished.
     */
    public interface ItemViewProvider {

        @Nullable
        @UiThread
        View provideItemView();
    }

    public static void onAlbumClick(@NonNull final BaseFragment host,
            @NonNull final QueueProviderAlbums queueProvider,
            final long albumId,
            @Nullable final String albumName,
            @Nullable final ItemViewProvider itemViewProvider) {
        host.registerOnStartSubscription(queueProvider.fromAlbum(albumId)
                .take(1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ObserverAdapter<List<Media>>() {
                    @Override
                    public void onNext(final List<Media> medias) {
                        if (host.isAdded()) {
                            onAlbumQueueLoaded(host, medias, albumName, itemViewProvider);
                        }
                    }

                    @Override
                    public void onError(final Throwable e) {
                        if (host.isAdded()) {
                            onAlbumQueueEmpty(host);
                        }
                    }
                }));
    }

    private static void onAlbumQueueLoaded(@NonNull final Fragment host,
            @NonNull final List<Media> queue,
            @Nullable final String albumName,
            @Nullable final ItemViewProvider itemViewProvider) {
        if (queue.isEmpty()) {
            onAlbumQueueEmpty(host);
        } else {
            final Activity activity = host.getActivity();
            final Intent intent = Henson.with(activity).gotoQueueActivity()
                    .hasCoverTransition(true)
                    .hasItemViewTransition(false)
                    .isNowPlayingQueue(false)
                    .queue(queue)
                    .title(albumName)
                    .build();

            Bundle options = null;
            if (itemViewProvider != null) {
                final View itemView = itemViewProvider.provideItemView();
                if (itemView != null) {
                    options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, itemView,
                            QueueActivity.TRANSITION_NAME_ALBUM_ART).toBundle();
                }
            }

            host.startActivity(intent, options);
        }
    }

    private static void onAlbumQueueEmpty(@NonNull final Fragment host) {
        Toast.makeText(host.getActivity(), R.string.The_queue_is_empty, Toast.LENGTH_SHORT).show();
    }
}
