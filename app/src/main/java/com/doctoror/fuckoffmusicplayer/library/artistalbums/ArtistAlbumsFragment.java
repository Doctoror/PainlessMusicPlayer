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

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.doctoror.fuckoffmusicplayer.domain.albums.AlbumsProvider;
import com.doctoror.fuckoffmusicplayer.domain.queue.Media;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderAlbums;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;

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
    QueueProviderAlbums queueProvider;

    @Inject
    AlbumsProvider mAlbumsProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        artistId = getArguments().getLong(EXTRA_ARTIST_ID);
        AndroidInjection.inject(this);
    }

    @NonNull
    @Override
    protected Observable<List<Media>> queueFromAlbum(final long albumId) {
        return queueProvider.fromAlbums(new long[]{albumId}, artistId);
    }

    @NonNull
    @Override
    protected Observable<List<Media>> queueFromAlbums(@NonNull final long[] albumIds) {
        return queueProvider.fromAlbums(albumIds, artistId);
    }

    @Override
    protected Observable<Cursor> load() {
        return mAlbumsProvider.loadForArtist(artistId);
    }
}
