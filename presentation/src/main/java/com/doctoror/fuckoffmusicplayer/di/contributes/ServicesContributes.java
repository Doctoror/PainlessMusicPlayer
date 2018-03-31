package com.doctoror.fuckoffmusicplayer.di.contributes;

import com.doctoror.fuckoffmusicplayer.presentation.playback.PlaybackAndroidServiceContributes;

import dagger.Module;

@Module(includes = {PlaybackAndroidServiceContributes.class})
public interface ServicesContributes {
}
