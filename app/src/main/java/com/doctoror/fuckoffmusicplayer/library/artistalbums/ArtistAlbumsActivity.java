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
package com.doctoror.fuckoffmusicplayer.library.artistalbums;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

/**
 * Created by Yaroslav Mytkalyk on 18.10.16.
 */
public final class ArtistAlbumsActivity extends BaseActivity {

    public static final String TRANSITION_NAME_ROOT = "TRANSITION_NAME_ROOT";

    @InjectExtra
    String artist;

    @InjectExtra
    Long artistId;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);

        ViewCompat.setTransitionName(findViewById(android.R.id.content),
                TRANSITION_NAME_ROOT);

        supportPostponeEnterTransition();
        setTitle(artist);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    ArtistAlbumsFragment.instantiate(this, artist, artistId)).commit();
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
    public void onBackPressed() {
        if (!getFragmentManager().popBackStackImmediate()) {
            // Finish without transitions
            finish();
        }
    }
}
