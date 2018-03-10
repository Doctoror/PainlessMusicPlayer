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
package com.doctoror.fuckoffmusicplayer.domain.playback;

import android.support.annotation.NonNull;

/**
 * Playback service control
 */
public interface PlaybackServiceControl {

    void resendState();

    void playPause();

    void play();

    void playAnything();

    void pause();

    void stop();

    void stopWithError(@NonNull CharSequence errorMessage);

    void prev();

    void next();

    void seek(float positionPercent);

    void playMediaFromQueue(long mediaId);
}
