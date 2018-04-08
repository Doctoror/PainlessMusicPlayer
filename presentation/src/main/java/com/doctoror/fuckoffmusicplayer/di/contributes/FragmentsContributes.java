/*
 * Copyright (C) 2018 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.di.contributes;

import com.doctoror.fuckoffmusicplayer.presentation.effects.EffectsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.home.HomeFragmentsContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.LibraryListFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.AlbumsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.albums.conditional.ConditionalAlbumListFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.artistalbums.ArtistAlbumsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.artists.ArtistsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.genrealbums.GenreAlbumsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.genres.GenresFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.playlists.PlaylistsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.recentalbums.RecentAlbumsFragmentContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.tracks.TracksFragmentContributes;

import dagger.Module;

@Module(includes = {
        AlbumsFragmentContributes.class,
        ArtistAlbumsFragmentContributes.class,
        ArtistsFragmentContributes.class,
        ConditionalAlbumListFragmentContributes.class,
        EffectsFragmentContributes.class,
        GenreAlbumsFragmentContributes.class,
        GenresFragmentContributes.class,
        HomeFragmentsContributes.class,
        LibraryListFragmentContributes.class,
        PlaylistsFragmentContributes.class,
        RecentAlbumsFragmentContributes.class,
        TracksFragmentContributes.class
})
public interface FragmentsContributes {
}
