package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.wear.WearableListenerServiceImpl;
import com.doctoror.fuckoffmusicplayer.wear.WearableMediaPlaybackReporter;
import com.doctoror.fuckoffmusicplayer.wear.WearableSearchProviderService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Wear Component
 */
@Singleton
@Component(modules = {
        AppContextModule.class,
        MediaStoreProvidersModule.class,
        PlaylistsModule.class
})
public interface WearComponent {

    void inject(WearableListenerServiceImpl target);

    void inject(WearableSearchProviderService target);

    void inject(WearableMediaPlaybackReporter target);

}
