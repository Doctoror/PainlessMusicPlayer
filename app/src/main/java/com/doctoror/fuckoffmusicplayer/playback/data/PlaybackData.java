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
package com.doctoror.fuckoffmusicplayer.playback.data;

import com.doctoror.fuckoffmusicplayer.playback.PlaybackState;
import com.doctoror.fuckoffmusicplayer.queue.Media;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import io.reactivex.Observable;

/**
 * Current playback data
 */
public interface PlaybackData {

    @NonNull
    Observable<List<Media>> queueObservable();

    @NonNull
    Observable<Integer> queuePositionObservable();

    @NonNull
    Observable<Long> mediaPositionObservable();

    @NonNull
    Observable<Integer> playbackStateObservable();

    @Nullable
    List<Media> getQueue();

    @NonNull
    Integer getQueuePosition();

    @NonNull
    Long getMediaPosition();

    @NonNull
    @PlaybackState.State
    Integer getPlaybackState();

    void setPlaybackState(@PlaybackState.State int state);
    void setPlayQueue(@Nullable List<Media> queue);
    void setPlayQueuePosition(int position);
    void setMediaPosition(long position);

    void persistAsync();

}
