/*
 * Copyright (C) 2017 Yaroslav Mytkalyk
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
package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.App;
import com.doctoror.fuckoffmusicplayer.appwidget.SingleRowAppWidgetProvider;
import com.doctoror.fuckoffmusicplayer.base.BaseActivity;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.effects.EffectsFragment;
import com.doctoror.fuckoffmusicplayer.effects.EqualizerView;
import com.doctoror.fuckoffmusicplayer.formatter.FormatterModule;
import com.doctoror.fuckoffmusicplayer.home.HomeActivity;
import com.doctoror.fuckoffmusicplayer.home.PlaybackStatusFragment;
import com.doctoror.fuckoffmusicplayer.home.RecentActivityFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.AlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.albums.conditional.ConditionalAlbumListFragment;
import com.doctoror.fuckoffmusicplayer.library.artistalbums.ArtistAlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.artists.ArtistsFragment;
import com.doctoror.fuckoffmusicplayer.library.genrealbums.GenreAlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.genres.GenresFragment;
import com.doctoror.fuckoffmusicplayer.library.playlists.PlaylistsFragment;
import com.doctoror.fuckoffmusicplayer.library.recentalbums.RecentAlbumsFragment;
import com.doctoror.fuckoffmusicplayer.library.tracks.TracksFragment;
import com.doctoror.fuckoffmusicplayer.media.MediaManagerService;
import com.doctoror.fuckoffmusicplayer.media.browser.MediaBrowserImpl;
import com.doctoror.fuckoffmusicplayer.media.browser.MediaBrowserServiceImpl;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivity;
import com.doctoror.fuckoffmusicplayer.nowplaying.NowPlayingActivityIntentHandler;
import com.doctoror.fuckoffmusicplayer.queue.QueueActivity;
import com.doctoror.fuckoffmusicplayer.settings.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Main Component
 */
@Singleton
@Component(modules = {
        AppContextModule.class,
        FormatterModule.class,
        EngineModule.class,
        MediaModule.class,
        MediaSessionModule.class,
        MediaStoreProvidersModule.class,
        PlaybackModule.class,
        QueueModule.class,
        QueueProvidersModule.class
})
public interface MainComponent {

    AudioEffects exposeAudioEffects();

    AlbumThumbHolder exposeAlbumThumbHolder();

    MediaPlayerFactory exposeMediaPlayerFactory();

    MediaSessionHolder exposeMediaSessionHolder();

    PlaybackData exposePlaybackData();

    PlaybackInitializer exposePlaybackInitializer();

    PlaybackNotificationFactory exposePlaybackNotificationFactory();

    PlaybackParams exposePlaybackParams();

    PlaybackReporterFactory exposePlaybackReporterFactory();

    QueueProviderRecentlyScanned exposeQueueProviderRecentlyScanned();

    void inject(App target);

    void inject(BaseActivity target);

    void inject(EffectsFragment target);

    void inject(EqualizerView target);

    void inject(HomeActivity target);

    void inject(QueueActivity target);

    void inject(RecentActivityFragment target);

    void inject(NowPlayingActivity target);

    void inject(NowPlayingActivityIntentHandler target);

    void inject(ArtistsFragment target);

    void inject(AlbumsFragment target);

    void inject(GenresFragment target);

    void inject(TracksFragment target);

    void inject(PlaylistsFragment target);

    void inject(PlaybackStatusFragment target);

    void inject(ConditionalAlbumListFragment target);

    void inject(ArtistAlbumsFragment target);

    void inject(GenreAlbumsFragment target);

    void inject(RecentAlbumsFragment target);

    void inject(MediaBrowserImpl target);
    
    void inject(MediaBrowserServiceImpl target);

    void inject(MediaManagerService target);

    void inject(SingleRowAppWidgetProvider target);

    void inject(SettingsActivity target);
}
