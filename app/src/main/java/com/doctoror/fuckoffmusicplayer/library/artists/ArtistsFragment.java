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
package com.doctoror.fuckoffmusicplayer.library.artists;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.rxcursorloader.RxCursorLoader;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class ArtistsFragment extends LibraryListFragment {

    private ArtistsRecyclerAdapter mAdapter;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new ArtistsRecyclerAdapter(getActivity());
        mAdapter.setOnArtistClickListener(this::openArtist);
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_artists_found));
    }

    @Override
    protected RxCursorLoader.Query newQuery(@Nullable final String filter) {
        return ArtistsQuery.newParams(filter);
    }

    @Override
    protected void onDataLoaded(@Nullable final Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    protected void onDataReset() {
        mAdapter.swapCursor(null);
    }

    private void openArtist(final long artistId, @NonNull final String artist) {
        startActivity(Henson.with(getActivity()).gotoArtistAlbumsActivity()
                .artist(artist)
                .artistId(artistId)
                .build());
    }
}
