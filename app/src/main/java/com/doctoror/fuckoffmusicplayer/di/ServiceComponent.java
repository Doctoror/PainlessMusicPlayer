package com.doctoror.fuckoffmusicplayer.di;

import com.doctoror.fuckoffmusicplayer.di.scopes.ServiceScope;
import com.doctoror.fuckoffmusicplayer.playback.PlaybackAndroidService;

import dagger.Component;

@ServiceScope
@Component(
        dependencies = {MainComponent.class},
        modules = {
                PlaybackServiceModule.class,
                ServiceModule.class
        })
public interface ServiceComponent {

    void inject(PlaybackAndroidService target);
}
