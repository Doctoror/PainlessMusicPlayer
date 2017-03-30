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
package com.doctoror.fuckoffmusicplayer.player;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 23.10.16.
 */

public interface MediaPlayer {

    int SESSION_ID_NOT_SET = 0;

    void init(@NonNull Context context);
    void load(@NonNull Uri data);
    void play();
    void pause();
    void seekTo(long millis);
    void stop();
    void release();
    void setListener(@Nullable MediaPlayerListener listener);
    @Nullable Uri getLoadedMediaUri();
    long getCurrentPosition();
}
