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
package com.doctoror.fuckoffmusicplayer.library.genrealbums;

import com.doctoror.fuckoffmusicplayer.BaseActivity;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListQuery;
import com.doctoror.rxcursorloader.RxCursorLoader;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

/**
 * Created by Yaroslav Mytkalyk on 18.10.16.
 */
public final class GenreAlbumsActivity extends BaseActivity {

    @InjectExtra
    String genre;

    @InjectExtra
    Long genreId;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);
        setTitle(genre);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    ConditionalAlbumListFragment.instantiate(this, genre, newParams())).commit();
        }
    }

    @NonNull
    private RxCursorLoader.Query newParams() {
        return ConditionalAlbumListQuery.newParams(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                "album_info._id IN (SELECT audio_meta.album_id FROM audio_meta, audio_genres_map "
                        + "WHERE audio_genres_map.audio_id=audio_meta._id AND audio_genres_map.genre_id="
                        + genreId + ')');
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
}
