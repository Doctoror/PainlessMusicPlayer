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

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.di.DaggerHolder;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.artists.ArtistsFragment;
import com.doctoror.fuckoffmusicplayer.library.genres.GenresFragment;
import com.doctoror.fuckoffmusicplayer.library.playlists.PlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksFragment;
import com.doctoror.fuckoffmusicplayer.navigation.NavigationController;
import com.doctoror.fuckoffmusicplayer.playback.data.PlaybackData;
import com.doctoror.fuckoffmusicplayer.queue.Media;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

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
import android.view.View;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * "Library" activity
 */
public final class HomeActivity extends BaseActivity {

    private static final String KEY_INSTANCE_STATE = "INSTANCE_STATE";

    private ActionBarDrawerToggle mActionBarDrawerToggle;

    @SuppressWarnings("FieldCanBeLocal") // Ensure not collected
    private NavigationController mNavigationController;

    @BindView(R.id.playbackStatusCard)
    View mPlaybackStatusCard;

    @BindView(R.id.drawerLayout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.navigationView)
    NavigationView mNavigationView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private int mNavigationItem = R.id.navigationRecentActivity;

    @Nullable
    @InjectExtra
    Integer drawerClosedAction;

    @Inject
    PlaybackData mPlaybackData;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHolder.getInstance(this).mainComponent().inject(this);

        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        initNavigation();

        if (savedInstanceState == null) {
            drawerClosedAction = mNavigationItem;
            performDrawerClosedAction();
        } else {
            restoreInstanceState(savedInstanceState);
        }

        handleIntent();
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent();
    }

    private void handleIntent() {
        Dart.inject(this);
        if (drawerClosedAction != null) {
            performDrawerClosedAction();
        }
    }

    private void initNavigation() {
        mNavigationView.setCheckedItem(mNavigationItem);

        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                R.string.Navigation_drawer, R.string.Navigation_drawer);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);

        mNavigationController = new NavigationController(this, mDrawerLayout);
        mNavigationController.bind();
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

    @Override
    protected void onStart() {
        super.onStart();
        disposeOnStop(mPlaybackData.queuePositionObservable()
                .subscribe(this::onQueuePositionChanged));
    }

    private void onQueuePositionChanged(final int position) {
        final List<Media> queue = mPlaybackData.getQueue();
        runOnUiThread(() -> mPlaybackStatusCard.setVisibility(
                queue != null && position < queue.size() ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
            mDrawerLayout.closeDrawer(mNavigationView);
        } else {
            super.onBackPressed();
        }
    }

    private void performDrawerClosedAction() {
        if (drawerClosedAction != null) {
            switch (drawerClosedAction) {
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
            }
            mNavigationItem = drawerClosedAction;
            drawerClosedAction = null;
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

    @Parcel
    static final class InstanceState {

        int navigationItem;
        String title;
    }
}
