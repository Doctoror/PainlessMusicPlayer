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
package com.doctoror.fuckoffmusicplayer.nowplaying;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.Henson;
import com.doctoror.fuckoffmusicplayer.R;
import com.doctoror.fuckoffmusicplayer.effects.AudioEffectsActivity;
import com.doctoror.fuckoffmusicplayer.library.LibraryActivity;
import com.doctoror.fuckoffmusicplayer.playlist.Playlist;
import com.f2prateek.dart.Dart;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by Yaroslav Mytkalyk on 21.10.16.
 */
public final class NowPlayingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);

        if (!RxPermissions.getInstance(this).isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            final Intent intent = Intent.makeMainActivity(
                    new ComponentName(this, LibraryActivity.class));
            startActivity(intent);
            return;
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    new NowPlayingFragment()).commit();
        }
    }

    @Override
    public void setSupportActionBar(@Nullable final Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE
                    | ActionBar.DISPLAY_SHOW_HOME
                    | ActionBar.DISPLAY_HOME_AS_UP);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_nowplaying, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionEffects:
                startActivity(new Intent(this, AudioEffectsActivity.class));
                return true;

            case R.id.actionPlaylist:
                final Intent playlistActivity = Henson.with(this)
                        .gotoPlaylistActivity()
                        .isNowPlayingPlaylist(Boolean.TRUE)
                        .playlist(Playlist.getInstance(this).getPlaylist())
                        .build();
                playlistActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(playlistActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
