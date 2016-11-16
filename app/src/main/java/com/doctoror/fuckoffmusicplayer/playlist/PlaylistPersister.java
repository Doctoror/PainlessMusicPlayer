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

import com.google.protobuf.nano.CodedOutputByteBufferNano;

import com.doctoror.fuckoffmusicplayer.playlist.nano.PersistablePlaylist;
import com.doctoror.commons.util.ByteStreams;
import com.doctoror.fuckoffmusicplayer.util.Log;
import com.doctoror.fuckoffmusicplayer.util.StringUtils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by Yaroslav Mytkalyk on 24.10.16.
 */

final class PlaylistPersister {

    private static final String TAG = "PlaylistPersister";

    private PlaylistPersister() {

    }

    private static final String FILE_NAME = "playlist";

    static void persistAsync(@NonNull final Context context,
            @NonNull final PlaylistHolder playlist) {
        // Retrieve snapshot now, so that it's immutable when writing
        if (playlist.playlist != null) {
            final PersistablePlaylist.ProtoPlaylist pp = toProtoPlaylist(playlist);
            Observable.create(s -> persist(context, pp))
                    .subscribeOn(Schedulers.io()).subscribe();
        }
    }

    private static void persist(@NonNull final Context context,
            @NonNull final PersistablePlaylist.ProtoPlaylist pp) {
        OutputStream os = null;
        try {
            os = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            final byte[] output = new byte[pp.getCachedSize()];
            pp.writeTo(CodedOutputByteBufferNano.newInstance(output));
            os.write(output);
            os.close();
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }
    }

    static void read(@NonNull final Context context,
            @NonNull final PlaylistHolder playlist) {
        InputStream is = null;
        PersistablePlaylist.ProtoPlaylist protoPlaylist = null;
        try {
            is = context.openFileInput(FILE_NAME);
            final byte[] bytes = ByteStreams.toByteArray(is);
            protoPlaylist = PersistablePlaylist.ProtoPlaylist.parseFrom(bytes);
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.w(TAG, e);
                }
            }
        }

        if (protoPlaylist != null) {
            readFromProtoPlaylist(protoPlaylist, playlist);
        }
    }

    private static void readFromProtoPlaylist(@NonNull final PersistablePlaylist.ProtoPlaylist pp,
            @NonNull final PlaylistHolder playlist) {
        playlist.index = pp.index;
        playlist.position = pp.position;
        if (pp.playlist != null) {
            final int size = pp.playlist.length;
            if (playlist.playlist == null) {
                playlist.playlist = new ArrayList<>(size);
            } else {
                playlist.playlist.clear();
            }
            for (int i = 0; i < size; i++) {
                playlist.playlist.add(toMedia(pp.playlist[i]));
            }
            if (playlist.index < playlist.playlist.size()) {
                playlist.media = playlist.playlist.get(pp.index);
            }
        } else {
            playlist.playlist.clear();
        }
    }

    @NonNull
    private static Media toMedia(@NonNull final PersistablePlaylist.ProtoMedia pm) {
        final Media media = new Media();
        media.id = pm.id;
        media.data = pm.data != null ? Uri.parse(pm.data) : null;
        media.title = pm.title;
        media.duration = pm.duration;
        media.artist = pm.artist;
        media.album = pm.album;
        media.albumArt = pm.albumArt;
        return media;
    }

    @NonNull
    private static PersistablePlaylist.ProtoPlaylist toProtoPlaylist(@NonNull final PlaylistHolder playlist) {
        final PersistablePlaylist.ProtoPlaylist pp = new PersistablePlaylist.ProtoPlaylist();
        pp.playlist = toProtoMediaList(playlist.playlist);
        pp.index = playlist.index;
        pp.position = playlist.position;
        return pp;
    }

    @NonNull
    private static PersistablePlaylist.ProtoMedia toProtoMedia(@NonNull final Media media) {
        final PersistablePlaylist.ProtoMedia pm = new PersistablePlaylist.ProtoMedia();
        pm.id = media.id;
        pm.data = media.data != null ? media.data.toString() : null;
        pm.title = StringUtils.notNullString(media.title);
        pm.duration = media.duration;
        pm.artist = StringUtils.notNullString(media.artist);
        pm.album = StringUtils.notNullString(media.album);
        pm.albumArt = StringUtils.notNullString(media.albumArt);
        return pm;
    }

    @NonNull
    private static PersistablePlaylist.ProtoMedia[] toProtoMediaList(@NonNull final List<Media> media) {
        final PersistablePlaylist.ProtoMedia[] result = new PersistablePlaylist.ProtoMedia[media.size()];
        final int size = media.size();
        for (int i = 0; i < size; i++) {
            result[i] = toProtoMedia(media.get(i));
        }
        return result;
    }

}
