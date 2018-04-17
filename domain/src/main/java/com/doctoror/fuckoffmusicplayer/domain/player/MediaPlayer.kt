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
package com.doctoror.fuckoffmusicplayer.domain.player

import android.content.Context
import android.net.Uri

const val SESSION_ID_NOT_SET = 0

interface MediaPlayer {

    fun getLoadedMediaUri(): Uri?
    fun getCurrentPosition(): Long

    fun init(context: Context)
    fun load(data: Uri)
    fun play()
    fun pause()
    fun seekTo(millis: Long)
    fun stop()
    fun release()
    fun setListener(listener: MediaPlayerListener?)
}
