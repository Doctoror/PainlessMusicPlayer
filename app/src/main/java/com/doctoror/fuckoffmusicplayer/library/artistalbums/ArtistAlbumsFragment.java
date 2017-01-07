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

import com.doctoror.fuckoffmusicplayer.db.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.db.playlist.PlaylistProviderAlbums;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.doctoror.fuckoffmusicplayer.playlist.Media;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;

/**
 * Shows album for artists
 */
public final class ArtistAlbumsFragment extends ConditionalAlbumListFragment {

    private static final String EXTRA_ARTIST_ID = "EXTRA_ARTIST_ID";

    @NonNull
    public static ArtistAlbumsFragment instantiate(final long artistId) {
        final ArtistAlbumsFragment fragment = new ArtistAlbumsFragment();
        final Bundle extras = new Bundle();
        extras.putLong(EXTRA_ARTIST_ID, artistId);
        fragment.setArguments(extras);
        return fragment;
    }

    private long artistId;

    @Inject
    PlaylistProviderAlbums mPlaylistFactory;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistId = getArguments().getLong(EXTRA_ARTIST_ID);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);
    }

    @Nullable
    @Override
    protected List<Media> playlistFromAlbums(@NonNull final long[] albumIds) {
        return mPlaylistFactory.fromAlbums(albumIds, artistId);
    }

    @Override
    protected Observable<Cursor> load() {
        return mAlbumsProvider.loadForArtist(artistId);
    }
}
