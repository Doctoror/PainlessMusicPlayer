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

import com.doctoror.fuckoffmusicplayer.presentation.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.di.scopes.ActivityScope;
import com.doctoror.fuckoffmusicplayer.presentation.effects.AudioEffectsActivityContributes;
import com.doctoror.fuckoffmusicplayer.presentation.home.HomeActivityContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.artistalbums.ArtistAlbumsActivityContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.genrealbums.GenreAlbumsActivityContributes;
import com.doctoror.fuckoffmusicplayer.presentation.library.recentalbums.RecentAlbumsActivityContributes;
import com.doctoror.fuckoffmusicplayer.presentation.nowplaying.NowPlayingActivityContributes;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivityContributes;
import com.doctoror.fuckoffmusicplayer.settings.SettingsActivityContributes;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {
        ArtistAlbumsActivityContributes.class,
        AudioEffectsActivityContributes.class,
        GenreAlbumsActivityContributes.class,
        HomeActivityContributes.class,
        NowPlayingActivityContributes.class,
        RecentAlbumsActivityContributes.class,
        SettingsActivityContributes.class,
        QueueActivityContributes.class
})
public interface ActivitiesContributes {

    @ActivityScope
    @ContributesAndroidInjector()
    BaseActivity baseActivity();
}
