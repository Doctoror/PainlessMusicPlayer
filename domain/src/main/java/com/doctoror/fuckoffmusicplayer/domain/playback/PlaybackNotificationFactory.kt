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
package com.doctoror.fuckoffmusicplayer.domain.playback

import android.app.Notification
import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumArtFetcher
import com.doctoror.fuckoffmusicplayer.domain.queue.Media

const val CHANNEL_ID_PLAYBACK_STATUS = "playback_status"

interface PlaybackNotificationFactory {

    fun create(
        context: Context,
        albumArtFetcher: AlbumArtFetcher,
        media: Media,
        state: PlaybackState,
        mediaSession: MediaSessionCompat
    ): Notification
}
