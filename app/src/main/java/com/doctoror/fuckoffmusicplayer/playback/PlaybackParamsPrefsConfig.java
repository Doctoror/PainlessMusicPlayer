package com.doctoror.fuckoffmusicplayer.playback;

import ds.gendalf.PrefsConfig;

/**
 * Playback params
 */
@PrefsConfig("PlaybackParamsPrefs")
interface PlaybackParamsPrefsConfig {

    boolean shuffleEnabled = false;
    int repeatMode = PlaybackParams.REPEAT_MODE_PLAYLIST;
}
