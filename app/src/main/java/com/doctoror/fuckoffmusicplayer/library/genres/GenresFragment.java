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
package com.doctoror.fuckoffmusicplayer.library.genres;

import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.db.genres.GenresProvider;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.LibraryListFragment;
import com.doctoror.fuckoffmusicplayer.library.genrealbums.GenreAlbumsActivity;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.View;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * "Genres" fragment
 */
public final class GenresFragment extends LibraryListFragment {

    private GenresRecyclerAdapter mAdapter;

    @Inject
    GenresProvider mGenresProvider;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(getActivity()).mainComponent().inject(this);

        mAdapter = new GenresRecyclerAdapter(getActivity());
        mAdapter.setOnGenreClickListener(this::onGenreClick);
        setRecyclerAdapter(mAdapter);
        setEmptyMessage(getText(R.string.No_genres_found));
    }

    @Override
    protected Observable<Cursor> load(@Nullable final String filter) {
        return mGenresProvider.load(filter);
    }

    @Override
    protected void onDataLoaded(@NonNull final Cursor data) {
        mAdapter.changeCursor(data);
    }

    @Override
    protected void onDataReset() {
        mAdapter.changeCursor(null);
    }

    private void onGenreClick(final int position, final long genreId,
            @Nullable final String genre) {
        final Intent intent = Henson.with(getActivity()).gotoGenreAlbumsActivity()
                .genre(genre)
                .genreId(genreId)
                .build();

        Bundle options = null;
        final View itemView = getItemView(position);
        if (itemView != null) {
            options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), itemView,
                    GenreAlbumsActivity.TRANSITION_NAME_ROOT).toBundle();
        }

        startActivity(intent, options);
    }
}
