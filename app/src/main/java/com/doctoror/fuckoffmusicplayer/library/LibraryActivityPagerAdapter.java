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
package com.doctoror.fuckoffmusicplayer.library;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.artists.ArtistsFragment;
import com.doctoror.fuckoffmusicplayer.library.genres.GenresFragment;
import com.doctoror.fuckoffmusicplayer.library.livelists.LivePlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksFragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

/**
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
final class LibraryActivityPagerAdapter extends FragmentStatePagerAdapter {

    private static final String[] FRAGMENTS = {
            ArtistsFragment.class.getCanonicalName(),
            AlbumsFragment.class.getCanonicalName(),
            GenresFragment.class.getCanonicalName(),
            TracksFragment.class.getCanonicalName(),
            LivePlaylistsFragment.class.getCanonicalName()
    };

    @NonNull
    private final Context mContext;

    private final CharSequence[] mTitles;

    LibraryActivityPagerAdapter(@NonNull final FragmentManager fm,
            @NonNull final Context context) {
        super(fm);
        mContext = context;
        mTitles = context.getResources().getTextArray(R.array.activity_library_tabs);
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        return mTitles[position];
    }

    @Override
    public Fragment getItem(final int position) {
        return Fragment.instantiate(mContext, FRAGMENTS[position]);
    }

    @Override
    public int getCount() {
        return FRAGMENTS.length;
    }
}
