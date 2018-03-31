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
package com.doctoror.fuckoffmusicplayer.presentation.home;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ObservableField;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBarDrawerToggle;

import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.databinding.ActivityHomeBinding;
import com.doctoror.fuckoffmusicplayer.presentation.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.presentation.library.artists.ArtistsFragment;
import com.doctoror.fuckoffmusicplayer.presentation.library.genres.GenresFragment;
import com.doctoror.fuckoffmusicplayer.presentation.library.playlists.PlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.presentation.library.tracks.TracksFragment;
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationController;
import com.doctoror.fuckoffmusicplayer.presentation.navigation.NavigationItem;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import org.parceler.Parcels;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * "Library" activity
 */
public final class HomeActivity extends BaseActivity {

    private static final String KEY_INSTANCE_STATE = "INSTANCE_STATE";

    private final OnNavigationChangeCallback onNavigationChangeCallback
            = new OnNavigationChangeCallback();

    private ActivityHomeBinding binding;

    private ActionBarDrawerToggle actionBarDrawerToggle;

    @SuppressWarnings("FieldCanBeLocal") // Ensure not collected
    private NavigationController navigationController;

    private NavigationView navigationView;

    @Nullable
    @InjectExtra
    NavigationItem navigationAction;

    @Inject
    HomePresenter presenter;

    @Inject
    HomeViewModel viewModel;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidInjection.inject(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        binding.setModel(viewModel);

        navigationView = findViewById(R.id.navigationView);

        setSupportActionBar(binding.toolbar);
        initNavigation();

        getLifecycle().addObserver(presenter);

        handleIntent(savedInstanceState == null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getLifecycle().removeObserver(presenter);
    }

    @Override
    protected void onNewIntent(@NonNull final Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(false);
    }

    private void handleIntent(final boolean navigateToDefaultWhenNoAction) {
        Dart.inject(this);
        if (navigationAction != null) {
            presenter.navigateTo(navigationAction);
            navigationAction = null;
        } else {
            presenter.navigateTo(NavigationItem.RECENT_ACTIVITY);
        }
    }

    private void initNavigation() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                binding.drawerLayout,
                binding.toolbar,
                R.string.Navigation_drawer,
                R.string.Navigation_drawer);

        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle);

        navigationController = new NavigationController(this, binding.drawerLayout);
        navigationController.bind();
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.navigationModel.navigationItem
                .addOnPropertyChangedCallback(onNavigationChangeCallback);

        if (viewModel.navigationModel.navigationItem.get() != null) {
            onNavigationChangeCallback.onPropertyChanged(
                    viewModel.navigationModel.navigationItem, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.navigationModel.navigationItem
                .removeOnPropertyChangedCallback(onNavigationChangeCallback);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_INSTANCE_STATE, Parcels.wrap(viewModel));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final HomeViewModel state = Parcels.unwrap(
                savedInstanceState.getParcelable(KEY_INSTANCE_STATE));
        if (state != null) {
            viewModel.applyFrom(state);
        }
    }

    @Override
    protected void onPostCreate(@Nullable final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(navigationView)) {
            binding.drawerLayout.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }

    private void navigateTo(@NonNull final NavigationItem navigationitem) {
        final String target = resolveNavigationTarget(navigationitem);
        if (target != null) {
            setMainFragment(target);
        }
    }

    @Nullable
    private String resolveNavigationTarget(@NonNull final NavigationItem navigationitem) {
        switch (navigationitem) {
            case RECENT_ACTIVITY:
                return RecentActivityFragment.class.getCanonicalName();

            case PLAYLISTS:
                return PlaylistsFragment.class.getCanonicalName();

            case ARTISTS:
                return ArtistsFragment.class.getCanonicalName();

            case ALBUMS:
                return AlbumsFragment.class.getCanonicalName();

            case GENRES:
                return GenresFragment.class.getCanonicalName();

            case TRACKS:
                return TracksFragment.class.getCanonicalName();

            default:
                return null;
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

    private final class OnNavigationChangeCallback
            extends Observable.OnPropertyChangedCallback {

        @Override
        public void onPropertyChanged(@NonNull final Observable sender, final int propertyId) {
            //noinspection unchecked
            final ObservableField<NavigationItem> o = (ObservableField<NavigationItem>) sender;
            final NavigationItem value = o.get();
            if (value != null) {
                navigateTo(value);
            }
        }
    }
}
