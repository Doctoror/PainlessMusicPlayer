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
import com.doctoror.fuckoffmusicplayer.di.contributes.ActivitiesContributes;
import com.doctoror.fuckoffmusicplayer.di.contributes.FragmentsContributes;
import com.doctoror.fuckoffmusicplayer.domain.effects.AudioEffects;
import com.doctoror.fuckoffmusicplayer.domain.media.AlbumThumbHolder;
import com.doctoror.fuckoffmusicplayer.domain.media.CurrentMediaProvider;
import com.doctoror.fuckoffmusicplayer.domain.media.MediaSessionHolder;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackData;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackNotificationFactory;
import com.doctoror.fuckoffmusicplayer.domain.playback.PlaybackParams;
import com.doctoror.fuckoffmusicplayer.domain.playback.initializer.PlaybackInitializer;
import com.doctoror.fuckoffmusicplayer.domain.player.MediaPlayerFactory;
import com.doctoror.fuckoffmusicplayer.domain.queue.QueueProviderRecentlyScanned;
import com.doctoror.fuckoffmusicplayer.domain.reporter.PlaybackReporterFactory;
import com.doctoror.fuckoffmusicplayer.presentation.appwidget.SingleRowAppWidgetProvider;
import com.doctoror.fuckoffmusicplayer.presentation.effects.EqualizerView;
import com.doctoror.fuckoffmusicplayer.presentation.formatter.FormatterModule;
import com.doctoror.fuckoffmusicplayer.presentation.media.MediaManagerService;
import com.doctoror.fuckoffmusicplayer.presentation.media.browser.MediaBrowserImpl;
import com.doctoror.fuckoffmusicplayer.presentation.media.browser.MediaBrowserServiceImpl;
import com.doctoror.fuckoffmusicplayer.presentation.nowplaying.NowPlayingActivityIntentHandler;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;

/**
 * Main Component
 */
@Singleton
@Component(modules = {
        ActivitiesContributes.class,
        AndroidInjectionModule.class,
        AppModule.class,
        FormatterModule.class,
        FragmentsContributes.class,
        EngineModule.class,
        MediaModule.class,
        MediaSessionModule.class,
        MediaStoreProvidersModule.class,
        PlaybackModule.class,
        QueueModule.class,
        QueueProvidersModule.class
})
public interface MainComponent extends AndroidInjector<App> {

    AudioEffects exposeAudioEffects();

    AlbumThumbHolder exposeAlbumThumbHolder();

    CurrentMediaProvider exposeCurrentMediaProvider();

    MediaPlayerFactory exposeMediaPlayerFactory();

    MediaSessionHolder exposeMediaSessionHolder();

    PlaybackData exposePlaybackData();

    PlaybackInitializer exposePlaybackInitializer();

    PlaybackNotificationFactory exposePlaybackNotificationFactory();

    PlaybackParams exposePlaybackParams();

    PlaybackReporterFactory exposePlaybackReporterFactory();

    QueueProviderRecentlyScanned exposeQueueProviderRecentlyScanned();

    void inject(EqualizerView target);

    void inject(NowPlayingActivityIntentHandler target);

    void inject(MediaBrowserImpl target);

    void inject(MediaBrowserServiceImpl target);

    void inject(MediaManagerService target);

    void inject(SingleRowAppWidgetProvider target);
}
