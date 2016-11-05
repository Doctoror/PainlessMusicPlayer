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
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.playlist.Media;
import com.doctoror.fuckoffmusicplayer.playlist.Playlist;
import com.doctoror.fuckoffmusicplayer.settings.SettingsActivity;
import com.jakewharton.rxbinding.support.v7.widget.RxSearchView;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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
 * Created by Yaroslav Mytkalyk on 17.10.16.
 */
public final class LibraryActivity extends BaseActivity {

    private static final int ANIMATOR_CHILD_PROGRESS = 0;
    private static final int ANIMATOR_CHILD_PERMISSION_DENIED = 1;
    private static final int ANIMATOR_CHILD_CONTENT = 2;

    private final SearchManager mSearchManager = SearchManager.getInstance();
    private LibraryPrefs mPrefs;

    private boolean mHasPermissions;

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

        requestPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_library, menu);
        if (mHasPermissions) {
            final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
            RxSearchView
                    .queryTextChanges(searchView)
                    .debounce(600, TimeUnit.MILLISECONDS)
                    .subscribe(t -> mSearchManager.updateQuery(t.toString()));

            menu.findItem(R.id.actionNowPlaying).setVisible(hasPlaylist());
        } else {
            menu.findItem(R.id.search).setVisible(false);
            menu.findItem(R.id.actionNowPlaying).setVisible(false);
        }
        return true;
    }

    private boolean hasPlaylist() {
        final List<Media> playlist = Playlist.getInstance(this).getPlaylist();
        return playlist != null && !playlist.isEmpty();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionNowPlaying:
                startActivity(new Intent(this, NowPlayingActivity.class));
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

    private void requestPermission() {
        RxPermissions.getInstance(this).request(Manifest.permission.READ_EXTERNAL_STORAGE)
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
}
