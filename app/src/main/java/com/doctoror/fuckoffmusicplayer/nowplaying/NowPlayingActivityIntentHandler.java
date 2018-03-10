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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.data.concurrent.Handlers;
import com.doctoror.fuckoffmusicplayer.data.util.Log;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderFiles;
import com.doctoror.fuckoffmusicplayer.media.browser.SearchUtils;
import com.doctoror.fuckoffmusicplayer.queue.QueueUtils;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * {@link NowPlayingActivity} intent handler
 */
public final class NowPlayingActivityIntentHandler {

    private static final String TAG = "IntentHandler";

    @Inject
    PlaybackData mPlaybackData;

    @Inject
    PlaybackServiceControl mPlaybackServiceControl;

    @Inject
    QueueProviderFiles mQueueProviderFiles;

    @NonNull
    private final Activity mActivity;

    NowPlayingActivityIntentHandler(@NonNull final Activity activity) {
        DaggerHolder.getInstance(activity).mainComponent().inject(this);
        mActivity = activity;
    }

    void handleIntent(@NonNull final Intent intent) {
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            onActionView(mActivity, mPlaybackData, intent, mQueueProviderFiles);
        } else if (MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH.equals(intent.getAction())) {
            onActionPlayFromSearch(mActivity, intent);
        }
    }

    private void onActionView(@NonNull final Activity activity,
            @NonNull final PlaybackData playbackData,
            @NonNull final Intent intent,
            @NonNull final QueueProviderFiles queueProvider) {
        queueFromActionView(queueProvider, intent)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(q -> onActionViewQueueLoaded(activity, playbackData, q),
                        t -> onActionViewQueueLoadFailed(activity));
    }

    private void onActionViewQueueLoaded(
            @NonNull final Activity activity,
            @NonNull final PlaybackData playbackData,
            @NonNull final List<Media> queue) {
        if (!activity.isFinishing()) {
            if (queue.isEmpty()) {
                Toast.makeText(activity.getApplicationContext(),
                        R.string.Failed_to_start_playback, Toast.LENGTH_LONG)
                        .show();
            } else {
                QueueUtils.play(mPlaybackServiceControl, playbackData, queue);
            }
        }
    }

    private static void onActionViewQueueLoadFailed(@NonNull final Activity activity) {
        if (!activity.isFinishing()) {
            Toast.makeText(activity.getApplicationContext(),
                    R.string.Failed_to_start_playback, Toast.LENGTH_LONG)
                    .show();
        }
    }

    private static void onActionPlayFromSearch(@NonNull final Activity activity,
            @NonNull final Intent intent) {
        Handlers.runOnIoThread(() -> new SearchUtils(activity).onPlayFromSearch(
                intent.getStringExtra(SearchManager.QUERY), intent.getExtras()));
    }

    @NonNull
    private static Observable<List<Media>> queueFromActionView(
            @NonNull final QueueProviderFiles queueProvider,
            @NonNull final Intent intent) {
        return Observable.fromCallable(() -> schemeAndDataFromIntent(intent))
                .flatMap(data -> queueObservableForSchemeAndData(queueProvider, data));
    }

    @NonNull
    private static Pair<Uri, String> schemeAndDataFromIntent(@NonNull final Intent intent)
            throws IOException {
        final Uri data = intent.getData();
        if (data == null) {
            Log.w(TAG, "Intent data is null");
            throw new IOException("Intent data is null");
        }

        final String scheme = data.getScheme();
        if (scheme == null) {
            Log.w(TAG, "Uri scheme is null");
            throw new IOException("Uri scheme is null");
        }

        return new Pair<>(data, scheme);
    }

    @NonNull
    private static Observable<List<Media>> queueObservableForSchemeAndData(
            @NonNull final QueueProviderFiles queueProvider,
            @NonNull final Pair<Uri, String> data) {
        switch (data.second) {
            case "file":
                return queueFromFileActionView(queueProvider, data.first);

            default:
                return Observable.error(new IOException("Unhandled Uri scheme: " + data.second));
        }
    }

    @NonNull
    private static Observable<List<Media>> queueFromFileActionView(
            @NonNull final QueueProviderFiles queueProvider,
            @NonNull final Uri data) {
        return queueProvider.fromFile(data);
    }
}
