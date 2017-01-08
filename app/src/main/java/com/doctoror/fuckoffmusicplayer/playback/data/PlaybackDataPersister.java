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

import com.doctoror.commons.util.ProtoUtils;
import com.doctoror.fuckoffmusicplayer.playback.data.nano.PlaybackDataProto;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Used for persisting and restoring {@link PlaybackData} from file
 */
final class PlaybackDataPersister {

    private PlaybackDataPersister() {
        throw new UnsupportedOperationException();
    }

    private static final String FILE_NAME = "playback_data";

    private static final Object LOCK = new Object();

    static void persistAsync(@NonNull final Context context,
            @NonNull final PlaybackData target) {
        final PlaybackDataProto.PlaybackData data = toProtoPlaybackData(target);
        Observable.create(s -> persist(context, data))
                .subscribeOn(Schedulers.io()).subscribe();
    }

    private static void persist(@NonNull final Context context,
            @NonNull final PlaybackDataProto.PlaybackData pp) {
        synchronized (LOCK) {
            ProtoUtils.writeToFile(context, FILE_NAME, pp);
        }
    }

    static void restoreFromFile(@NonNull final Context context,
            @NonNull final PlaybackData target) {
        final PlaybackDataProto.PlaybackData proto;
        synchronized (LOCK) {
            proto = ProtoUtils.readFromFile(context, FILE_NAME,
                    new PlaybackDataProto.PlaybackData());
        }
        if (proto != null) {
            restoreFromProto(proto, target);
        }
    }

    private static void restoreFromProto(
            @NonNull final PlaybackDataProto.PlaybackData proto,
            @NonNull final PlaybackData target) {
        target.setPlaylist(toMediaList(proto.playlist));
        target.setPlaylistPosition(proto.playlistPosition);
        target.setMediaPosition(proto.mediaPosition);
    }

    @NonNull
    private static Media toMedia(@NonNull final PlaybackDataProto.Media pm) {
        final Media media = new Media();
        media.setId(pm.id);
        media.setData(pm.data != null ? Uri.parse(pm.data) : null);
        media.setTitle(pm.title);
        media.setDuration(pm.duration);
        media.setArtist(pm.artist);
        media.setAlbumId(pm.albumId);
        media.setAlbum(pm.album);
        media.setAlbumArt(pm.albumArt);
        return media;
    }

    @NonNull
    private static List<Media> toMediaList(@Nullable final PlaybackDataProto.Media[] pl) {
        if (pl == null || pl.length == 0) {
            return new ArrayList<>();
        }
        final List<Media> result = new ArrayList<>(pl.length);
        for (final PlaybackDataProto.Media item : pl) {
            result.add(toMedia(item));
        }
        return result;
    }

    @NonNull
    private static PlaybackDataProto.PlaybackData toProtoPlaybackData(
            @NonNull final PlaybackData playbackData) {
        final PlaybackDataProto.PlaybackData pp = new PlaybackDataProto.PlaybackData();
        pp.playlist = toProtoMediaList(playbackData.getPlaylist());
        pp.playlistPosition = playbackData.getPlaylistPosition();
        pp.mediaPosition = playbackData.getMediaPosition();
        return pp;
    }

    @NonNull
    private static PlaybackDataProto.Media toProtoMedia(@NonNull final Media media) {
        final PlaybackDataProto.Media pm = new PlaybackDataProto.Media();
        pm.id = media.getId();
        pm.data = media.getData() != null ? media.getData().toString() : null;
        pm.title = StringUtils.notNullString(media.getTitle());
        pm.duration = media.getDuration();
        pm.artist = StringUtils.notNullString(media.getArtist());
        pm.albumId = media.getAlbumId();
        pm.album = StringUtils.notNullString(media.getAlbum());
        pm.albumArt = StringUtils.notNullString(media.getAlbumArt());
        return pm;
    }

    @NonNull
    private static PlaybackDataProto.Media[] toProtoMediaList(
            @Nullable final List<Media> media) {
        if (media == null || media.isEmpty()) {
            return new PlaybackDataProto.Media[0];
        }
        final int size = media.size();
        final PlaybackDataProto.Media[] result = new PlaybackDataProto.Media[size];
        for (int i = 0; i < size; i++) {
            result[i] = toProtoMedia(media.get(i));
        }
        return result;
    }

}
