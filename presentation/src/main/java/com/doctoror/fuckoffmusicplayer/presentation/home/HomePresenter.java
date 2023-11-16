/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import com.doctoror.fuckoffmusicplayer.di.scopes.ActivityScope;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.presentation.base.BasePresenter;
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationItem;

import java.util.List;

import javax.inject.Inject;

@ActivityScope
final class HomePresenter extends BasePresenter {

    private final PlaybackData playbackData;
    private final HomeViewModel viewModel;

    @Inject
    HomePresenter(
            @NonNull final PlaybackData playbackData,
            @NonNull final HomeViewModel viewModel) {
        this.playbackData = playbackData;
        this.viewModel = viewModel;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        disposeOnStop(playbackData.queuePositionObservable()
                .subscribe(this::onQueuePositionChanged));
    }

    private void onQueuePositionChanged(final int position) {
        final List<Media> queue = playbackData.getQueue();
        viewModel.playbackStatusCardVisibility.set(
                queue != null && position < queue.size() ? View.VISIBLE : View.GONE);
    }

    void navigateTo(@NonNull final NavigationItem item) {
        if (shouldSetAsCurrentNavigationItem(item)) {
            viewModel.title.set(item.title);
        }
        viewModel.navigationModel.navigationItem.set(item);
    }

    private boolean shouldSetAsCurrentNavigationItem(@NonNull final NavigationItem item) {
        return item != NavigationItem.SETTINGS;
    }
}
