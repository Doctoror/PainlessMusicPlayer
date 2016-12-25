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
package com.doctoror.fuckoffmusicplayer.playlist;

import com.doctoror.fuckoffmusicplayer.playback.PlaybackServiceControl;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Playlist utils
 */
public final class PlaylistUtils {

    private PlaylistUtils() {
        throw new UnsupportedOperationException();
    }

    public static void play(@NonNull final Context context,
            @NonNull final List<Media> mediaList) {
        if (mediaList.isEmpty()) {
            throw new IllegalArgumentException("Will not play empty playlist");
        }
        play(context, mediaList, 0);
    }

    public static void play(@NonNull final Context context,
            @NonNull final List<Media> mediaList,
            final int position) {
        play(context, mediaList, mediaList.get(position), position);
    }

    public static void play(@NonNull final Context context,
            @NonNull final List<Media> mediaList,
            @NonNull final Media media,
            final int position) {
        final CurrentPlaylist playlist = CurrentPlaylist.getInstance(context);
        playlist.setPlaylist(mediaList);
        playlist.setMedia(media);
        playlist.setIndex(position);
        playlist.setPosition(0);
        playlist.persistAsync();

        PlaybackServiceControl.play(context);
    }

}
