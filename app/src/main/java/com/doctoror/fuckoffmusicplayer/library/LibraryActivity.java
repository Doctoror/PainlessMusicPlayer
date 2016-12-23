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

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.CurrentPlaylist;
import com.doctoror.fuckoffmusicplayer.settings.SettingsActivity;
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.tbruyelle.rxpermissions.RxPermissions;

import org.parceler.Parcel;
import org.parceler.Parcels;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewAnimator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * "Library" activity
 */
public final class LibraryActivity extends BaseActivity {

    private static final String KEY_INSTANCE_STATE = "INSTANCE_STATE";

    private static final int ANIMATOR_CHILD_PERMISSION_DENIED = 1;
    private static final int ANIMATOR_CHILD_CONTENT = 2;

    // Request once per-app instance
    private static boolean sPermissionRequested;

    private final SearchSubject mSearchSubject = SearchSubject.getInstance();
    private LibraryPrefs mPrefs;

    private boolean mHasPermissions;
    private boolean mSearchIconified;
    private boolean mPermissionRequested = sPermissionRequested;

    private RxPermissions mRxPermissions;

    @BindView(R.id.animator)
    ViewAnimator mViewAnimator;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.viewPager)
    ViewPager mViewPager;

    @BindView(R.id.tabLayout)
    TabLayout mTabLayout;

    private int mPagerItem;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        mPrefs = LibraryPrefs.with(this);
        if (savedInstanceState == null) {
            mPagerItem = mPrefs.getTab();
        }

        mSearchIconified = true;
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        requestPermissionIfNeeded();
    }

    private void restoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final InstanceState state = Parcels
                .unwrap(savedInstanceState.getParcelable(KEY_INSTANCE_STATE));
        if (state != null) {
            mPermissionRequested = state.permissionRequested || sPermissionRequested;
            mSearchIconified = state.searchIconified;
            mSearchSubject.onNext(state.searchQuery);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final InstanceState state = new InstanceState();
        state.permissionRequested = mPermissionRequested;
        state.searchIconified = mSearchIconified;
        state.searchQuery = mSearchSubject.getValue();
        outState.putParcelable(KEY_INSTANCE_STATE, Parcels.wrap(state));
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_library, menu);
        if (mHasPermissions) {
            final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            searchView.setQuery(mSearchSubject.getValue(), false);
            searchView.setOnCloseListener(() -> {
                mSearchIconified = true;
                return false;
            });
            searchView.setOnSearchClickListener((v) -> mSearchIconified = false);
            searchView.setIconified(mSearchIconified);
            RxSearchView
                    .queryTextChanges(searchView)
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .subscribe(t -> mSearchSubject.onNext(t.toString()));

            menu.findItem(R.id.actionNowPlaying).setVisible(hasPlaylist());
        } else {
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.actionNowPlaying).setVisible(false);
        }
        return true;
    }

    private boolean hasPlaylist() {
        final List<Media> playlist = CurrentPlaylist.getInstance(this).getPlaylist();
        return playlist != null && !playlist.isEmpty();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionNowPlaying:
                startActivity(Henson.with(this)
                        .gotoNowPlayingActivity()
                        .hasCoverTransition(false)
                        .hasListViewTransition(false)
                        .build());
                return true;

            case R.id.actionSettings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPrefs.setTab(mViewPager.getCurrentItem());
    }

    @OnClick(R.id.btnRequest)
    void onRequestPermissionClick() {
        requestPermission();
    }

    @NonNull
    private RxPermissions getRxPermissions() {
        if (mRxPermissions == null) {
            mRxPermissions = new RxPermissions(this);
        }
        return mRxPermissions;
    }

    private void requestPermissionIfNeeded() {
        mHasPermissions = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (mHasPermissions) {
            onPermissionGranted();
        } else if (mPermissionRequested) {
            onPermissionDenied();
        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        sPermissionRequested = mPermissionRequested = true;
        getRxPermissions().request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    mHasPermissions = granted;
                    if (granted) {
                        onPermissionGranted();
                    } else {
                        onPermissionDenied();
                    }
                });
    }

    private void onPermissionGranted() {
        mViewPager.setAdapter(new LibraryActivityPagerAdapter(getFragmentManager(), this));
        mTabLayout.setVisibility(View.VISIBLE);
        mTabLayout.setupWithViewPager(mViewPager, false);
        if (mPagerItem >= 0 && mPagerItem < mViewPager.getAdapter().getCount()) {
            mViewPager.setCurrentItem(mPagerItem);
        }
        mViewAnimator.setDisplayedChild(ANIMATOR_CHILD_CONTENT);
        invalidateOptionsMenu();
    }

    private void onPermissionDenied() {
        mViewAnimator.setDisplayedChild(ANIMATOR_CHILD_PERMISSION_DENIED);
    }

    @Parcel
    static final class InstanceState {

        String searchQuery;
        boolean searchIconified;
        boolean permissionRequested;
    }
}
