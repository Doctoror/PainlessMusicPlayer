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
package com.doctoror.fuckoffmusicplayer.home;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.artists.ArtistsFragment;
import com.doctoror.fuckoffmusicplayer.library.genres.GenresFragment;
import com.doctoror.fuckoffmusicplayer.library.livelists.LivePlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.library.playlists.PlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksFragment;
import com.doctoror.fuckoffmusicplayer.settings.SettingsActivity;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * "Library" activity
 */
public final class RootActivity extends BaseActivity {

    private static final String KEY_INSTANCE_STATE = "INSTANCE_STATE";

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    @BindView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navigationView)
    NavigationView mNavigationView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private int mNavigationItem = R.id.navigationRecentActivity;

    private Integer mDrawerClosedAction;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(this).mainComponent().inject(this);

        setContentView(R.layout.activity_root);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mNavigationView.setNavigationItemSelectedListener(new NavigationListener());
        mNavigationView.setCheckedItem(mNavigationItem);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.Navigation_drawer, R.string.Navigation_drawer) {
            @Override
            public void onDrawerClosed(final View drawerView) {
                super.onDrawerClosed(drawerView);
                performDrawerClosedAction();
            }
        };
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        if (savedInstanceState == null) {
            mDrawerClosedAction = mNavigationItem;
            performDrawerClosedAction();
        } else {
            restoreInstanceState(savedInstanceState);
        }
    }

    private void restoreInstanceState(final Bundle savedInstanceState) {
        final InstanceState state = Parcels
                .unwrap(savedInstanceState.getParcelable(KEY_INSTANCE_STATE));
        if (state != null) {
            mNavigationItem = state.navigationItem;
            setTitle(state.title);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final InstanceState state = new InstanceState();
        state.navigationItem = mNavigationItem;
        state.title = getTitle().toString();
        outState.putParcelable(KEY_INSTANCE_STATE, Parcels.wrap(state));
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActionBarDrawerToggle.syncState();
    }

    private void performDrawerClosedAction() {
        if (mDrawerClosedAction != null) {
            switch (mDrawerClosedAction) {
                case R.id.navigationRecentActivity:
                    setMainFragment(RecentActivityFragment.class.getCanonicalName());
                    setTitle(R.string.Recent_Activity);
                    break;

                case R.id.navigationPlaylists:
                    setMainFragment(PlaylistsFragment.class.getCanonicalName());
                    setTitle(R.string.Playlists);
                    break;

                case R.id.navigationArtists:
                    setMainFragment(ArtistsFragment.class.getCanonicalName());
                    setTitle(R.string.Artists);
                    break;

                case R.id.navigationAlbums:
                    setMainFragment(AlbumsFragment.class.getCanonicalName());
                    setTitle(R.string.Albums);
                    break;

                case R.id.navigationGenres:
                    setMainFragment(GenresFragment.class.getCanonicalName());
                    setTitle(R.string.Genres);
                    break;

                case R.id.navigationTracks:
                    setMainFragment(TracksFragment.class.getCanonicalName());
                    setTitle(R.string.Tracks);
                    break;

                case R.id.navigationSettings:
                    startActivity(new Intent(this, SettingsActivity.class));
                    break;
            }
            mDrawerClosedAction = null;
        }
    }

    private void setMainFragment(@NonNull final String name) {
        final FragmentManager fm = getFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.content);
        FragmentTransaction ft = null;
        if (fragment == null) {
            ft = fm.beginTransaction();
            ft.add(R.id.content, Fragment.instantiate(this, name));
        } else if (!name.equals(fragment.getClass().getCanonicalName())) {
            ft = fm.beginTransaction();
            ft.replace(R.id.content, Fragment.instantiate(this, name));
        }
        if (ft != null) {
            ft.commit();
        }
    }

    private final class NavigationListener
            implements NavigationView.OnNavigationItemSelectedListener {

        @Override
        public boolean onNavigationItemSelected(@NonNull final MenuItem item) {

            final int id = item.getItemId();
            final boolean result;
            switch (id) {
                case R.id.navigationRecentActivity:
                case R.id.navigationArtists:
                case R.id.navigationAlbums:
                case R.id.navigationGenres:
                case R.id.navigationTracks:
                case R.id.navigationPlaylists:
                    result = true;
                    break;

                default:
                    result = false;
                    break;
            }

            if (result) {
                mNavigationItem = item.getItemId();
            }

            mDrawerClosedAction = id;
            mDrawerLayout.closeDrawers();
            return result;
        }
    }

    @Parcel
    static final class InstanceState {

        int navigationItem;
        String title;
    }
}
