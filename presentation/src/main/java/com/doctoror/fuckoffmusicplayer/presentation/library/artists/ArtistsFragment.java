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
package com.doctoror.fuckoffmusicplayer.presentation.library.artists;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import com.doctoror.fuckoffmusicplayer.presentation.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.domain.artists.ArtistsProvider;
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.presentation.library.artistalbums.ArtistAlbumsActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.Observable;

/**
 * "Artists" fragment
 */
public final class ArtistsFragment extends LibraryListFragment {

    private ArtistsRecyclerAdapter mAdapter;

    @Inject
    ArtistsProvider mArtistsProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);

        mAdapter = new ArtistsRecyclerAdapter(getActivity());
        mAdapter.setOnArtistClickListener(this::onArtistClick);
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_artists_found));
    }

    @Override
    protected Observable<Cursor> load(@Nullable final String filter) {
        return mArtistsProvider.load(filter);
    }

    @Override
    protected void onDataLoaded(@NonNull final Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    protected void onDataReset() {
        mAdapter.changeCursor(null);
    }

    private void onArtistClick(final int position, final long artistId,
                               @Nullable final String artist) {
        final Activity activity = getActivity();
        final Intent intent = Henson.with(activity).gotoArtistAlbumsActivity()
                .artist(artist)
                .artistId(artistId)
                .build();

        Bundle options = null;
        final View itemView = getItemView(position);
        if (itemView != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, itemView,
                    ArtistAlbumsActivity.TRANSITION_NAME_ROOT).toBundle();
        }

        startActivity(intent, options);
    }
}
