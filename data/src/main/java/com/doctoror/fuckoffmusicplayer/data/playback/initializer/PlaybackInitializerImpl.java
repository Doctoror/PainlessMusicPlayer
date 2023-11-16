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
package com.doctoror.fuckoffmusicplayer.data.playback.initializer;

import androidx.annotation.NonNull;

import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackServiceControl;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;

import java.util.List;

public final class PlaybackInitializerImpl implements PlaybackInitializer {

    private final PlaybackServiceControl control;

    public PlaybackInitializerImpl(@NonNull final PlaybackServiceControl control) {
        this.control = control;
    }

    @Override
    public void setQueueAndPlay(@NonNull final List<Media> queue, final int position) {
        control.play(queue, position);
    }
}
