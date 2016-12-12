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
package com.doctoror.fuckoffmusicplayer.library.artistalbums;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListQuery;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.PlaylistFactory;
import com.doctoror.rxcursorloader.RxCursorLoader;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Yaroslav Mytkalyk on 18.10.16.
 */

public final class ArtistAlbumsFragment extends ConditionalAlbumListFragment {

    @NonNull
    public static ArtistAlbumsFragment instantiate(@NonNull final Context context,
            @NonNull final String artist,
            @NonNull final Long artistId) {
        final ArtistAlbumsFragment fragment = new ArtistAlbumsFragment();
        final RxCursorLoader.Query params = ConditionalAlbumListQuery.newParams(
                MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                MediaStore.Audio.Media.IS_MUSIC + "!=0");
        final Bundle extras = Henson.with(context).gotoArtistAlbumsFragment()
                .artistId(artistId)
                .loaderParams(params)
                .build()
                .getExtras();
        fragment.setArguments(extras);
        return fragment;
    }

    @InjectExtra
    Long artistId;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this, getArguments());
    }

    @Nullable
    @Override
    protected List<Media> playlistFromAlbums(@NonNull final long[] albumIds,
            @NonNull final String[] arts) {
        return PlaylistFactory.fromAlbums(getActivity().getContentResolver(),
                albumIds, arts, artistId);
    }
}
